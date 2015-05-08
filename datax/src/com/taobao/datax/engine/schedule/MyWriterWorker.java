/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.schedule;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.taobao.datax.common.constants.ExitStatus;
import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.exception.ExceptionTracker;
import com.taobao.datax.common.plugin.LineReceiver;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.engine.conf.PluginConf;

/**
 * Represents executor of a {@link Writer}.</br>
 * 
 * <p>{@link Engine} use {@link MyWriterWorker} to dump data.</p>
 * 
 * @see ReaderWorker
 * 
 * */
public class MyWriterWorker extends PluginWorker implements Runnable {
	private LineReceiver receiver;

	private Method init;

	private Method connect;

	private Method startWrite;

	private Method commit;

	private Method finish;

	private static int globalIndex = 0;

	private static final Logger logger = Logger.getLogger(MyWriterWorker.class);

	/**
	 * Construct a {@link MyWriterWorker}.
	 * 
	 * @param	pluginConf
	 * 			PluginConf of {@link Writer}.
	 * 
	 * @param myClass
	 * 
	 * @throws DataExchangeException
	 * 
	 */
	public MyWriterWorker(PluginConf pluginConf, Class<?> myClass)  {
		super(pluginConf, myClass);
		try {
			init = myClass.getMethod("init", new Class[] {});
			connect = myClass.getMethod("connect", new Class[] {});
			startWrite = myClass
					.getMethod("startWrite", new Class[] { Class
							.forName("com.taobao.datax.common.plugin.LineReceiver") });
			commit = myClass.getMethod("commit", new Class[] {});
			finish = myClass.getMethod("finish", new Class[] {});
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
		this.setMyIndex(globalIndex++);
	}

	public void setLineReceiver(LineReceiver receiver) {
		this.receiver = receiver;
	}

	/**
	 * Write data, main execute logic code of {@link Writer} <br>
	 * NOTE: When catches exception, {@link MyWriterWorker} exit process immediately
	 * 
	 * */
	@Override
	public void run() {
		try {
			int iRetcode = (Integer) init.invoke(myObject, new Object[] {});
			if (iRetcode != 0) {
				logger.error("DataX Initialize failed.");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) connect.invoke(myObject, new Object[] {});
			if (iRetcode != 0) {
				logger.error("DataX connect to DataSink failed.");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) startWrite.invoke(myObject,
					new Object[] { receiver });
			if (iRetcode != 0) {
				logger.error("DataX starts writing data failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) commit.invoke(myObject, new Object[] {});
			if (iRetcode != 0) {
				logger.error("DataX commits transaction failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) finish.invoke(myObject, new Object[] {});
			if (iRetcode != 0) {
				logger.error("DataX do finish job failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			System.exit(ExitStatus.FAILED.value());
		}
	}

}
