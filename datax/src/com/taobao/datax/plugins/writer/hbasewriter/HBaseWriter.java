/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.plugins.writer.hbasewriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.Line;
import com.taobao.datax.common.plugin.LineReceiver;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Writer;

public class HBaseWriter extends Writer {

	private static final int DEFAULT_BUFFER_SIZE = 16 * 1024 * 1024;

	private String tablename;

	private String encode = "utf-8";

	private HBaseProxy proxy;

	private int delMode;

	private boolean autoflush;

	private String hbase_conf;

	private String[] columnNames;

	private int bufferSize;

	private byte[][] families = null;

	private byte[][] qualifiers = null;

	private int[] columnIndexes;

	private int rowkeyIndex;

	private Logger logger = Logger.getLogger(HBaseWriter.class);

	@Override
	public List<PluginParam> split(PluginParam param) {
		HBaseWriterSplitter spliter = new HBaseWriterSplitter();
		spliter.setParam(param);
		spliter.init();
		return spliter.split();
	}

	@Override
	public int prepare(PluginParam param) {
		this.logger.info("DataX HBaseWriter do prepare work .");

		try {
			switch (this.delMode) {
			case 0:
				this.logger.info("HBaseWriter reserves old data .");
				break;
			case 1:
				truncateTable();
				break;
			case 2:
				deleteTables();
				break;
			default:
				String msg = "HBaseWriter delmode is not correct .";
				this.logger.error(msg);
				throw new IllegalArgumentException(msg);
			}
		} catch (IOException e) {
			try {
				proxy.close();
			} catch (IOException e1) {
			}
		}

		return this.finish();
	}

	@Override
	public int post(PluginParam param) {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int init() {
		tablename = param.getValue(ParamKey.htable);
		hbase_conf = param.getValue(ParamKey.hbase_conf);
		encode = param.getValue(ParamKey.encoding, "UTF-8");
		delMode = param.getIntValue(ParamKey.delMode, 1);
		autoflush = param.getBoolValue(ParamKey.autoFlush, false);
		bufferSize = param
				.getIntValue(ParamKey.bufferSize, DEFAULT_BUFFER_SIZE);

		/* if user does not set rowkey index, use 0 for default. */
		rowkeyIndex = param.getIntValue(ParamKey.rowkey_index, 0, 0, Integer.MAX_VALUE);

		columnNames = param.getValue(ParamKey.column_name).split(",");

		if (bufferSize < 0 || bufferSize >= 32 * 1024 * 1024) {
			throw new IllegalArgumentException("buffer size must be [0M-32M] .");
		}

		families = new byte[columnNames.length][];
		qualifiers = new byte[columnNames.length][];
		for (int i = 0; i < columnNames.length; i++) {
			if (!columnNames[i].contains(":")) {
				throw new IllegalArgumentException(String.format(
						"Column %s must be like 'family:qualifier'",
						columnNames[i]));
			}
			String[] tmps = columnNames[i].split(":");
			try {
				families[i] = tmps[0].trim().getBytes(encode);
				qualifiers[i] = tmps[1].trim().getBytes(encode);
			} catch (UnsupportedEncodingException e) {
				throw new DataExchangeException(e.getCause());
			}

		}

		/*
		 * test if user has set column_value_index . if set, use the value set
		 * by user if not set, use the default value, [1, length of column_name]
		 */
		if (param.hasValue(ParamKey.column_value_index)) {
			String[] indexes = param.getValue(ParamKey.column_value_index)
					.split(",");
			if (indexes.length != columnNames.length) {
				String msg = String
						.format("HBase column index is different form column name: \nColumnName %s\nColumnIndex %s\n",
								param.getValue(ParamKey.column_name),
								param.getValue(ParamKey.column_value_index));
				logger.error(msg);
				throw new IllegalArgumentException(msg);
			}

			columnIndexes = new int[indexes.length];
			for (int i = 0; i < indexes.length; i++) {
				columnIndexes[i] = Integer.valueOf(indexes[i]);
			}
		} else {
			columnIndexes = new int[columnNames.length];
			for (int i = 0; i < rowkeyIndex; i++) {
				columnIndexes[i] = i;
			}
			for (int i = rowkeyIndex; i < columnIndexes.length; i++) {
				columnIndexes[i] = i + 1;
			}
		}

		try {
			this.proxy = HBaseProxy.newProxy(hbase_conf, tablename);
			this.proxy.setAutoFlush(autoflush);
			this.proxy.setBufferSize(bufferSize);

			if (null == this.proxy || !this.proxy.check()) {
				throw new DataExchangeException("HBase Client initilize failed .");
			}

			return PluginStatus.SUCCESS.value();
		} catch (IOException e) {
			if (null != proxy) {
				try {
					proxy.close();
				} catch (IOException e1) {
				}
			}
			throw new DataExchangeException(e.getCause());
		}
	}

	@Override
	public int connect() {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int startWrite(LineReceiver receiver) {

		Line line;

		int fieldNum;

		/*
		 * NOTE: field numbers in each line may be different
		 */
		while ((line = receiver.getFromReader()) != null) {
			try {
				fieldNum = line.getFieldNum();

				if (null == line.checkAndGetField(rowkeyIndex)) {
					throw new IOException("rowkey is missing .");
				}

				if (0 == fieldNum || 1 == fieldNum) {
					logger.warn("HBaseWriter meets an empty line, ignore it .");
					continue;
				}
				
				proxy.prepare(line.getField(rowkeyIndex).getBytes(encode));

				for (int i = 0; i < columnIndexes.length; i++) {
					if (null == line.checkAndGetField(columnIndexes[i])) {
						continue;
					}

					proxy.add(families[i], qualifiers[i],
							line.getField(columnIndexes[i]).getBytes(encode));
				}
				proxy.insert();
				this.monitor.lineSuccess();
			} catch (IOException e) {
				this.getMonitor().lineFail(e.getMessage());
			}
		}

		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int commit() {
		this.logger.info("HBaseWriter starts to commit records .");
		try {
			proxy.flush();
			return PluginStatus.SUCCESS.value();
		} catch (IOException e) {
			try {
				proxy.close();
			} catch (IOException e1) {
			}
			throw new DataExchangeException(e.getCause());
		}
	}

	@Override
	public int finish() {
		try {
			proxy.close();
			return PluginStatus.SUCCESS.value();
		} catch (IOException e) {
			throw new DataExchangeException(e.getCause());
		}
	}

	private void deleteTables() throws IOException {
		this.logger.info(String.format(
				"HBasWriter begins to delete table %s .", this.tablename));
		proxy.deleteTable();
	}

	private void truncateTable() throws IOException {
		this.logger.info(String.format(
				"HBasWriter begins to truncate table %s .", this.tablename));
		proxy.truncateTable();
	}
}
