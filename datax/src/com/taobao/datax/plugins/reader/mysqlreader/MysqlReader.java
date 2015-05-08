/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.plugins.reader.mysqlreader;

import com.taobao.datax.common.exception.ExceptionTracker;
import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.*;
import com.taobao.datax.plugins.common.DBResultSetSender;
import com.taobao.datax.plugins.common.DBSource;
import com.taobao.datax.plugins.common.DBUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


public class MysqlReader extends Reader {
	private Connection conn;

	/* below for job-xml variant */
	private String encode = null;

	private String username = "";

	private String password = "";

	private String ip = "";

	private String port = "3306";

	private String dbname = null;

	private int concurrency = -1;

	private String mysqlParams;
	
	private String sql = null;

	private static final Set<String> supportEncode = new HashSet<String>() {
		{
			add("utf-8");
			add("gbk");
		}
	};

	private Logger logger = Logger.getLogger(MysqlReader.class);

	@Override
	public int init() {
		/* for database connection */
		this.username = param.getValue(ParamKey.username, this.username);
		this.password = param.getValue(ParamKey.password, this.password);
		this.ip = param.getValue(ParamKey.ip);
		this.port = param.getValue(ParamKey.port, this.port);
		this.dbname = param.getValue(ParamKey.dbname);
		this.encode = param.getValue(ParamKey.encoding, "");
		this.mysqlParams = param.getValue(ParamKey.mysqlParams,"");
		
		/* set nullChar to replace null in query-null-value */
		this.sql = param.getValue(ParamKey.sql, "").trim();
		/* for connection session */
		this.concurrency = param.getIntValue(ParamKey.concurrency, 1);

		return PluginStatus.SUCCESS.value();
	}

	public int secondInit(){
		/* for database connection */
		this.username = "dreambox";
		this.password = "dream613";
		this.ip = "localhost";
		this.port = "3306";
		this.dbname = "mojingtest";
		this.encode = "utf-8";
		this.concurrency = 1;
		this.sql="";
//		this.mysqlParams = param.getValue(ParamKey.mysqlParams,"");
//		
//		/* set nullChar to replace null in query-null-value */
//		this.sql = param.getValue(ParamKey.sql, "").trim();
//		/* for connection session */
//		this.concurrency = param.getIntValue(ParamKey.concurrency, 1);

		return PluginStatus.SUCCESS.value();
	}
	
	@Override
	public int prepare(PluginParam param) {
		this.secondInit();
		Properties p = createProperties();
		DBSource.register(this.getClass(), this.ip, this.port, this.dbname, p);
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int connect() {
		conn = DBSource.getConnection(this.getClass(), ip, port, dbname);
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int startRead(LineSender lineSender){
		DBResultSetSender proxy = DBResultSetSender.newSender(lineSender);
		proxy.setMonitor(getMonitor());
		proxy.setDateFormatMap(genDateFormatMap());
		
		String sql = param.getValue(ParamKey.sql);
//		String sql = "select * from mj_object";
		this.logger.info(String.format("MysqlReader start to query %s .", sql));
		ResultSet rs = null;
		try {
			rs = DBUtils.query(conn, sql);
//			System.out.println("res = " + rs.getRow());
			proxy.sendToWriter(rs);
			proxy.flush();
//			getMonitor().setStatus(PluginStatus.READ_OVER);
			
			return PluginStatus.SUCCESS.value();
		} catch (SQLException e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		} finally {
            if (null != rs) {
			    DBUtils.closeResultSet(rs);
            }
		}
	
	}

	@Override
	public int finish(){
		try {
			if (conn != null) {
				conn.close();
			}
			conn = null;
		} catch (SQLException e) {
		}
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public List<PluginParam> split(PluginParam param){
		List<PluginParam> sqlList;
		
		if (StringUtils.isBlank(this.sql)) {
			/* non-user-defined sql */
			MysqlReaderSplitter spliter = new MysqlReaderSplitter(param);
			spliter.init();
			sqlList = spliter.split();
		} else {
			/* user-define sql */
			sqlList = super.split(param);
		}
		
		String sql = sqlList.get(0).getValue(ParamKey.sql);
		MetaData m = null;
		try {
			conn = DBSource.getConnection(this.getClass(), ip, port, dbname);
			m = DBUtils.genMetaData(conn, sql);
			param.setMyMetaData(m);
		} catch (SQLException e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
		
		return sqlList;
	}

	private Map<String, SimpleDateFormat> genDateFormatMap() {
		Map<String, SimpleDateFormat> mapDateFormat = new HashMap<String, SimpleDateFormat>();
		mapDateFormat.clear();
		mapDateFormat.put("datetime", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss"));
		mapDateFormat.put("timestamp", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss"));
		mapDateFormat.put("time", new SimpleDateFormat("HH:mm:ss"));
		return mapDateFormat;
	}

	private boolean isSupportEncode(String encode) {
		if (supportEncode.contains(encode.toLowerCase())) {
			return  true;
		}
		return false;
	}

	private Properties createProperties() {
		Properties p = new Properties();
		
		String encodeDetail = "";
		
		if(!StringUtils.isBlank(this.encode)){
			encodeDetail = "useUnicode=true&characterEncoding="	+ this.encode + "&";
		}
		String url = "jdbc:mysql://" + this.ip + ":" + this.port + "/"
				+ this.dbname + "?" + encodeDetail 
				+ "yearIsDateType=false&zeroDateTimeBehavior=convertToNull"
				+ "&defaultFetchSize=" + String.valueOf(Integer.MIN_VALUE);
		
		if (!StringUtils.isBlank(this.mysqlParams)) {
			url = url + "&" + this.mysqlParams;
		}
		
		p.setProperty("driverClassName", "com.mysql.jdbc.Driver");
		p.setProperty("url", url);
		p.setProperty("username", username);
		p.setProperty("password", password);
		p.setProperty("maxActive", String.valueOf(concurrency + 2));
		p.setProperty("initialSize", String.valueOf(concurrency + 2));
		p.setProperty("maxIdle", "1");
		p.setProperty("maxWait", "1000");
		p.setProperty("defaultReadOnly", "true");
		p.setProperty("testOnBorrow", "true");
		p.setProperty("validationQuery", "select 1 from dual");

		this.logger.info(String.format("MysqlReader try connection: %s .", url));
		return p;
	}

}
