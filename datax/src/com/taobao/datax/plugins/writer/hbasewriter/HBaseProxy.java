/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.plugins.writer.hbasewriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class HBaseProxy {
	private Configuration config;

	private HTable htable;

	private HBaseAdmin admin;

	private HTableDescriptor descriptor;
	
	private static final int BUFFER_LINE = 1024;
	
	private List<Put> buffer = new ArrayList<Put>(BUFFER_LINE);

	private Put p;

	public static HBaseProxy newProxy(String hbase_conf, String table) 
			throws IOException {
		return new HBaseProxy(hbase_conf, table);
	}
	
	private HBaseProxy(String hbase_conf, String tableName)
			throws IOException {
		Configuration conf = new Configuration();
		conf.addResource(new Path(hbase_conf));
		config = new Configuration(conf);
		htable = new HTable(config, tableName);
		admin = new HBaseAdmin(config);
		descriptor = htable.getTableDescriptor();
	}

	public void setBufferSize(int bufferSize) throws IOException {
		this.htable.setWriteBufferSize(bufferSize);
	}
	
	public void setHTable(String tableName) throws IOException {
		this.htable = new HTable(config, tableName);
	}

	public void setAutoFlush(boolean autoflush) {
		this.htable.setAutoFlush(autoflush);
	}

	public boolean check() throws IOException {
		if (!admin.isMasterRunning()) {
			throw new IllegalStateException("hbase master is not running!");
		}
		if (!admin.tableExists(htable.getTableName())) {
			throw new IllegalStateException("hbase table " + Bytes.toString(htable.getTableName())
					+ " is not existed!");
		}
		if (!admin.isTableAvailable(htable.getTableName())) {
			throw new IllegalStateException("hbase table " + Bytes.toString(htable.getTableName())
					+ " is not available!");
		}
		if (!admin.isTableEnabled(htable.getTableName())) {
			throw new IllegalStateException("hbase table " + Bytes.toString(htable.getTableName())
					+ " is disable!");
		}

		return true;
	}

	public void close() throws IOException {
		htable.close();
	}

	public void deleteTable() throws IOException {
		Scan s = new Scan();
		ResultScanner scanner = htable.getScanner(s);
		for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {
			htable.delete(new Delete(rr.getRow()));
		}
		scanner.close();
	}

	public void truncateTable() throws IOException {
		admin.disableTable(htable.getTableName());
		admin.deleteTable(htable.getTableName());
		admin.createTable(descriptor);
	}

	public void flush() throws IOException {
		if (!buffer.isEmpty()) {
			htable.put(buffer);
			buffer.clear();
		}
		htable.flushCommits();
	}

	public void prepare(byte[] rowKey) {
		this.p = new Put(rowKey);
    }
	
	public Put add(byte[] family, byte[] qualifier, byte[] value) {
		return this.p.add(family, qualifier, value);
	}
	
	public void insert() throws IOException {
		buffer.add(this.p);
		if (buffer.size() >= BUFFER_LINE) {
			htable.put(buffer);
			buffer.clear();
		}
	}
}
