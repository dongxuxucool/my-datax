/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.engine.schedule;

import com.taobao.datax.common.constants.ExitStatus;
import com.taobao.datax.common.exception.ExceptionTracker;
import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.Pluginable;
import com.taobao.datax.common.plugin.Reader;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.engine.conf.*;
import com.taobao.datax.engine.plugin.BufferedLineExchanger;
import com.taobao.datax.engine.storage.Storage;
import com.taobao.datax.engine.storage.StoragePool;
import com.taobao.datax.engine.tools.JobConfGenDriver;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Core class of DataX, schedule {@link Reader} & {@link Writer}.
 * 
 * */
public class MyEngine {
	private static final Logger logger = Logger.getLogger(MyEngine.class);

	private static final int PERIOD = 10;

	private static final int MAX_CONCURRENCY = 64;

	private EngineConf engineConf;

	private Map<String, PluginConf> pluginReg;

	private MonitorPool readerMonitorPool;

	private MonitorPool writerMonitorPool;

	/**
	 * Constructor for {@link MyEngine}
	 * 
	 * @param engineConf
	 *            Configuration for {@link MyEngine}
	 * 
	 * @param pluginReg
	 *            Configurations for {@link Pluginable}
	 * 
	 * */
	public MyEngine(EngineConf engineConf, Map<String, PluginConf> pluginReg) {
		this.engineConf = engineConf;
		this.pluginReg = pluginReg;

		this.writerMonitorPool = new MonitorPool();
		this.readerMonitorPool = new MonitorPool();

	}

	/**
	 * Start a DataX job.
	 *
	 * @param jobConf
	 *            Configuration for the DataX Job.
	 * 
	 * @return 0 for success, others for failure.
	 *
	 * @throws Exception
       *
       */

 	public int start(JobConf jobConf) throws Exception {
		logger.info('\n' + engineConf.toString() + '\n');
		logger.info('\n' + jobConf.toString() + '\n');
		logger.info("DataX startups .");

		StoragePool storagePool = new StoragePool(jobConf, engineConf, PERIOD);
		NamedThreadPoolExecutor readerPool = initReaderPool(jobConf,
				storagePool);
		List<NamedThreadPoolExecutor> writerPool = initWriterPool(jobConf,
				storagePool);

		logger.info("DataX starts to exchange data .");
		readerPool.shutdown();
		for (NamedThreadPoolExecutor dp : writerPool) {
			dp.shutdown();
		}
		
		int sleepCnt = 0;
		int retcode = 0;

		while (true) {
			/* check reader finish? */
			boolean readerFinish = readerPool.isTerminated();
			if (readerFinish) {
				storagePool.closeInput();
			}

			boolean writerAllFinish = true;

			NamedThreadPoolExecutor[] dps = writerPool
					.toArray(new NamedThreadPoolExecutor[0]);
			/* check each DumpPool */
			for (NamedThreadPoolExecutor dp : dps) {
				if (!readerFinish && dp.isTerminated()) {
					logger.error(String.format("DataX Writer %s failed .",
							dp.getName()));
					writerPool.remove(dp);
				} else if (!dp.isTerminated()) {
					writerAllFinish = false;
				}
			}

			if (readerFinish && writerAllFinish) {
				logger.info("DataX Reader post work begins .");
				readerPool.doPost();
				logger.info("DataX Reader post work ends .");

				logger.info("DataX Writers post work begins .");
				for (NamedThreadPoolExecutor dp : writerPool) {
					dp.getParam().setOppositeMetaData(
							readerPool.getParam().getMyMetaData());
					dp.doPost();
				}
				logger.info("DataX Writers post work ends .");

				logger.info("DataX job succeed .");
				break;
			} else if (!readerFinish && writerAllFinish) {
				logger.error("DataX Writers finished before reader finished.");
				logger.error("DataX job failed.");
				readerPool.shutdownNow();
				readerPool.awaitTermination(3, TimeUnit.SECONDS);
				break;
			}

			Thread.sleep(1000);
			sleepCnt++;

			if (sleepCnt % PERIOD == 0) {
				/* reader&writer count num of thread */
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("ReaderPool %s: Active Threads %d .",
						readerPool.getName(), readerPool.getActiveCount()));
				logger.info(sb.toString());

				sb.setLength(0);
				for (NamedThreadPoolExecutor perWriterPool : writerPool) {
					sb.append(String.format(
							"WriterPool %s: Active Threads %d .",
							perWriterPool.getName(),
							perWriterPool.getActiveCount()));
					logger.info(sb.toString());
					sb.setLength(0);
				}
				logger.info(storagePool.getPeriodState());
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append(storagePool.getTotalStat());
		long discardLine = this.writerMonitorPool.getDiscardLine();
		sb.append(String.format("%-26s: %19d\n", "Total discarded records",
				discardLine));

		logger.info(sb.toString());

		Reporter.stat.put("DISCARD_RECORDS", String.valueOf(discardLine));
		Reporter reporter = Reporter.instance();
		reporter.report(jobConf);

		long total = -1;
		boolean writePartlyFailed = false;
		for (Storage s : storagePool.getStorageForReader()) {
			String[] lineCounts = s.info().split(":");
			long lineTx = Long.parseLong(lineCounts[1]);
			if (total != -1 && total != lineTx) {
				writePartlyFailed = true;
				logger.error("Writer partly failed, for " + total + "!="
						+ lineTx);
			}
			total = lineTx;
		}
		return writePartlyFailed ? 200 : retcode;
	}

	/**
	 * configure log4j environment.
	 * 
	 * @param jobId
	 *            DataX job id.
	 * 
	 * */
	public static void confLog(String jobId) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		java.text.SimpleDateFormat f = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		String logDir = "logs/" + f.format(c.getTime());
		System.setProperty("log.dir", logDir);
		f = new java.text.SimpleDateFormat("HHmmss");
		String logFile = jobId + "." + f.format(c.getTime()) + ".log";
		System.setProperty("log.file", logFile);
		PropertyConfigurator.configure("conf/log4j.properties");
	}

	private NamedThreadPoolExecutor initReaderPool(JobConf jobConf,
			StoragePool sp) throws Exception {

		JobPluginConf readerJobConf = jobConf.getReaderConf();
		PluginConf readerConf = pluginReg.get(readerJobConf.getName());

		if (readerConf.getPath() == null) {
			readerConf.setPath(engineConf.getPluginRootPath() + "reader/"
					+ readerConf.getName());
		}

		logger.info(String.format("DataX Reader %s try to load path %s .",
				readerConf.getName(), readerConf.getPath()));
		JarLoader jarLoader = new JarLoader(
				new String[] { readerConf.getPath() });
		Class<?> myClass = jarLoader.loadClass(readerConf.getClassName());

		ReaderWorker readerWorkerForPreAndPost = new ReaderWorker(readerConf,
				myClass);
		PluginParam sparam = jobConf.getReaderConf().getPluginParams();

		readerWorkerForPreAndPost.setParam(sparam);
		readerWorkerForPreAndPost.init();

		logger.info("DataX Reader prepare work begins .");
		int code = readerWorkerForPreAndPost.prepare(sparam);
		if (code != 0) {
			throw new DataExchangeException("DataX Reader prepare work failed!");
		}
		logger.info("DataX Reader prepare work ends .");

		logger.info("DataX Reader split work begins .");
		List<PluginParam> readerSplitParams = readerWorkerForPreAndPost
				.doSplit(sparam);
		logger.info(String.format(
				"DataX Reader splits this job into %d sub-jobs",
				readerSplitParams.size()));
		logger.info("DataX Reader split work ends .");

		int concurrency = readerJobConf.getConcurrency();
		if (concurrency <= 0 || concurrency > MAX_CONCURRENCY) {
			throw new IllegalArgumentException(String.format(
					"Reader concurrency set to be %d, make sure it must be between [%d, %d] .",
					concurrency, 1, MAX_CONCURRENCY));
		}

		concurrency = Math.min(concurrency,
				readerSplitParams.size());
		if (concurrency <= 0) {
			concurrency = 1;
		}
		readerJobConf.setConcurrency(concurrency);

		NamedThreadPoolExecutor readerPool = new NamedThreadPoolExecutor(
				readerJobConf.getId(), readerJobConf.getConcurrency(),
				readerJobConf.getConcurrency(), 1L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());

		readerPool.setPostWorker(readerWorkerForPreAndPost);
		readerPool.setParam(sparam);

		readerPool.prestartAllCoreThreads();

		logger.info("DataX Reader starts to read data .");
		for (PluginParam param : readerSplitParams) {
			ReaderWorker readerWorker = new ReaderWorker(readerConf, myClass);
			readerWorker.setParam(param);
			readerWorker.setLineSender(new BufferedLineExchanger(null, sp
					.getStorageForReader(), this.engineConf
					.getStorageBufferSize()));
			readerPool.execute(readerWorker);
			readerMonitorPool.monitor(readerWorker);
		}

		return readerPool;
	}

	private List<NamedThreadPoolExecutor> initWriterPool(JobConf jobConf,
			StoragePool sp) throws Exception {
		List<NamedThreadPoolExecutor> writerPoolList = new ArrayList<NamedThreadPoolExecutor>();
		List<JobPluginConf> writerJobConfs = jobConf.getWriterConfs();
		for (JobPluginConf dpjc : writerJobConfs) {
			PluginConf writerConf = pluginReg.get(dpjc.getName());
			if (writerConf.getPath() == null) {
				writerConf.setPath(engineConf.getPluginRootPath() + "writer/"
						+ writerConf.getName());
			}

			logger.info(String.format(
					"DataX Writer %s try to load path %s .",
					writerConf.getName(), writerConf.getPath()));
			JarLoader jarLoader = new JarLoader(
					new String[] { writerConf.getPath() });
			Class<?> myClass = jarLoader.loadClass(writerConf.getClassName());

			WriterWorker writerWorkerForPreAndPost = new WriterWorker(
					writerConf, myClass);

			PluginParam writerParam = dpjc.getPluginParams();
			writerWorkerForPreAndPost.setParam(writerParam);
			writerWorkerForPreAndPost.init();

			logger.info("DataX Writer prepare work begins .");
			int code = writerWorkerForPreAndPost.prepare(writerParam);
			if (code != 0) {
				throw new DataExchangeException(
						"DataX Writer prepare work failed!");
			}
			logger.info("DataX Writer prepare work ends .");

			logger.info("DataX Writer split work begins .");
			List<PluginParam> writerSplitParams = writerWorkerForPreAndPost
					.doSplit(writerParam);
			logger.info(String.format(
					"DataX Writer splits this job into %d sub-jobs .",
					writerSplitParams.size()));
			logger.info("DataX Writer split work ends .");

			int concurrency = dpjc.getConcurrency();
			if (concurrency <= 0 || concurrency > MAX_CONCURRENCY) {
				throw new IllegalArgumentException(String.format(
						"Writer concurrency set to be %d, make sure it must be between [%d, %d] .",
						concurrency, 1, MAX_CONCURRENCY));
			}
	
			concurrency = Math.min(dpjc.getConcurrency(),
					writerSplitParams.size());
			if (concurrency <= 0) {
				concurrency = 1;
			}
			dpjc.setConcurrency(concurrency);

			NamedThreadPoolExecutor writerPool = new NamedThreadPoolExecutor(
					dpjc.getName() + "-" + dpjc.getId(), dpjc.getConcurrency(),
					dpjc.getConcurrency(), 1L, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());

			writerPool.setPostWorker(writerWorkerForPreAndPost);
			writerPool.setParam(writerParam);

			writerPool.prestartAllCoreThreads();
			writerPoolList.add(writerPool);
			logger.info("DataX Writer starts to write data .");

			for (PluginParam pp : writerSplitParams) {
				WriterWorker writerWorker = new WriterWorker(writerConf,
						myClass);
				writerWorker.setParam(pp);
				writerWorker.setLineReceiver(new BufferedLineExchanger(sp
						.getStorageForWriter(dpjc.getId()), null,
						this.engineConf.getStorageBufferSize()));
				writerPool.execute(writerWorker);
				writerMonitorPool.monitor(writerWorker);
			}
		}
		return writerPoolList;
	}

	/**
	 * Program entry </br>> NOTE: The DataX Process exists code </br> exit with
	 * 0: Job succeed </br> exit with 1: Job failed </br> exit with 2: Job
	 * failed, e.g. connetion interrupted, if we try to rerun it in a few
	 * seconds, it may succeed.
	 * 
	 *
     * @param args  cmd arguments
     *
     * @throws Exception*/
	public static void main(String[] args) throws Exception {
		String jobDescFile = null;
		if (args.length < 1) {
			System.exit(JobConfGenDriver.produceXmlConf());
		} else if (args.length == 1) {
			jobDescFile = args[0];
		} else {
			System.out.printf("Usage: java -jar engine.jar job.xml .");
			System.exit(ExitStatus.FAILED.value());
		}

		confLog("BEFORE_CHRIST");
		JobConf jobConf = ParseXMLUtil.loadJobConfig(jobDescFile);
		confLog(jobConf.getId());
		EngineConf engineConf = ParseXMLUtil.loadEngineConfig();
		Map<String, PluginConf> pluginConfs = ParseXMLUtil.loadPluginConfig();

		MyEngine engine = new MyEngine(engineConf, pluginConfs);

		int retcode = 0;
		try {
			retcode = engine.start(jobConf);
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			System.exit(ExitStatus.FAILED.value());
		}
		System.exit(retcode);
	}
	
	private int engineStart(){
		confLog("0000");
		
		
		
		return 0;
	}

}
