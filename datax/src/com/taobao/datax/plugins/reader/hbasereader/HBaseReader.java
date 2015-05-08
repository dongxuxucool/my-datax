package com.taobao.datax.plugins.reader.hbasereader;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.taobao.datax.common.exception.ExceptionTracker;
import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.Line;
import com.taobao.datax.common.plugin.LineSender;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Reader;

public class HBaseReader extends Reader {
	private Logger logger = Logger.getLogger(this.getClass());

	private String tableName = null;

	private String columns = null;

	private String hbaseConf = null;

	private String rowkeyRange = null;

	private HBaseProxy proxy = null;

	@Override
	public List<PluginParam> split(PluginParam param) {
		HBaseReaderSplitter splitter = new HBaseReaderSplitter();
		splitter.setParam(param);
		splitter.init();
		return splitter.split();
	}

	@Override
	public int init() {
		this.tableName = this.param.getValue(ParamKey.htable);
		this.columns = this.param.getValue(ParamKey.columns_key);
		this.hbaseConf = this.param.getValue(ParamKey.hbase_conf);

		try {
			proxy = HBaseProxy.newProxy(hbaseConf, tableName);
		} catch (IOException e) {
			try {
				if (null != proxy) {
					proxy.close();
				}
			} catch (IOException e1) {
			}
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e.getCause());
		}


		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int connect() {
		this.logger.info("HBaseReader start to connect to HBase .");
		this.rowkeyRange = this.param.getValue(ParamKey.rowkey_range, "");
		if (StringUtils.isBlank(rowkeyRange)) {
			this.logger.info("HBaseReader prepare to query all records . ");
			proxy.setStartEndRange(null, null);
		} else {
			rowkeyRange = " " + rowkeyRange + " ";
			String[] pair = rowkeyRange.split(",");
			if (null == pair || 0 == pair.length) {
				this.logger.info("HBaseReader prepare to query all records . ");
				proxy.setStartEndRange(null, null);
			} else {
				String start = StringUtils.isBlank(pair[0].trim()) ? null
						: pair[0].trim();
				String end = StringUtils.isBlank(pair[1].trim()) ? null
						: pair[1].trim();
				this.logger.info(String.format(
						"HBaseReader prepare to query records [%s, %s) .",
						(start == null ? "-infinite" : start), (end == null ? "+infinite"
								: end)));
				proxy.setStartEndRange((start == null ? null : start.getBytes()),
						(end == null ? null : end.getBytes()));
			}
		}
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int startRead(LineSender sender) {
		try {
			proxy.prepare(columns.split(","));
			Line line = sender.createLine();
			while (proxy.fetchLine(line)) {
				sender.sendToWriter(line);
				monitor.lineSuccess();
				line = sender.createLine();
			}
			sender.flush();
		} catch (IOException e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e.getCause());
		} finally {
			try {
				proxy.close();
			} catch (IOException e) {
			}
		}

		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int finish() {
		return PluginStatus.SUCCESS.value();
	}

}
