/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.plugins.reader.hbasereader;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.Line;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HBaseProxy {

	private Configuration config;

	private HTable htable;

	private HBaseAdmin admin;

	private String encode = "UTF-8";

	private byte[] startKey = null;

	private byte[] endKey = null;

	private Scan scan = null;

	private byte[][] families = null;

	private byte[][] columns = null;

	private ResultScanner rs = null;

	private static final int SCAN_CACHE = 256;

	private Logger logger = Logger.getLogger(HBaseProxy.class);

	public static HBaseProxy newProxy(String hbase_conf, String tableName)
			throws IOException {
		return new HBaseProxy(hbase_conf, tableName);
	}

	private HBaseProxy(String hbase_conf, String tableName) throws IOException {
		Configuration conf = new Configuration();
		conf.addResource(new Path(hbase_conf));
		config = new Configuration(conf);
		htable = new HTable(config, tableName);
		admin = new HBaseAdmin(config);

		if (!this.check()) {
			throw new IllegalStateException(
					"DataX try to build HBaseProxy failed !");
		}
	}

	public Pair<byte[][], byte[][]> getStartEndKeys() throws IOException {
		return this.htable.getStartEndKeys();
	}

	public void setEncode(String encode) {
		this.encode = encode;
		return;
	}

	public void setStartRange(byte[] startKey) {
		this.startKey = startKey;
		return;
	}

	public void setEndRange(byte[] endKey) {
		this.endKey = endKey;
		return;
	}

	public void setStartEndRange(byte[] startKey, byte[] endKey) {
		this.startKey = startKey;
		this.endKey = endKey;
		return;
	}

	/*
	 * Must be sure that column is in format like 'family: column'
	 */
	public void prepare(String[] columns) throws IOException {
		this.scan = new Scan();
		
		if (this.startKey != null) {
			logger.info(String.format("HBaseReader set startkey to %s .",
					Bytes.toString(this.startKey)));
			scan.setStartRow(startKey);
		}
		if (this.endKey != null) {
			logger.info(String.format("HBaseReader set endkey to %s .",
					Bytes.toString(this.endKey)));
			scan.setStopRow(endKey);
		}

		this.families = new byte[columns.length][];
		this.columns = new byte[columns.length][];
	
		int idx = 0;
		for (String column : columns) {
			this.families[idx] = column.split(":")[0].trim().getBytes();
			this.columns[idx] = column.split(":")[1].trim().getBytes();
			scan.addColumn(this.families[idx], this.columns[idx]);
			idx++;
		}

		htable.setScannerCaching(SCAN_CACHE);
		this.rs = htable.getScanner(this.scan);
	}

	public boolean fetchLine(Line line) throws IOException {
		if (null == this.rs) {
			throw new IllegalStateException(
					"HBase Client try to fetch data failed .");
		}

		Result result = this.rs.next();
		if (null == result) {
			return false;
		}

		for (int i = 0; i < this.families.length; i++) {
			byte[] value = result.getValue(this.families[i], this.columns[i]);
			if (null == value) {
				line.addField(null);
			} else {
				line.addField(new String(value, encode));
			}
		}

		return true;
	}

	public void close() throws IOException {
		if (null != rs) {
			rs.close();
		}
		if (null != htable) {
			htable.close();
		}
	}

	private boolean check() throws DataExchangeException, IOException {
		if (!admin.isMasterRunning()) {
			throw new IllegalStateException("HBase master is not running!");
		}
		if (!admin.tableExists(htable.getTableName())) {
			throw new IllegalStateException("HBase table "
					+ Bytes.toString(htable.getTableName())
					+ " is not existed!");
		}
		if (!admin.isTableAvailable(htable.getTableName())) {
			throw new IllegalStateException("HBase table "
					+ Bytes.toString(htable.getTableName())
					+ " is not available!");
		}
		if (!admin.isTableEnabled(htable.getTableName())) {
			throw new IllegalStateException("HBase table "
					+ Bytes.toString(htable.getTableName()) + " is disable!");
		}

		return true;
	}

}
