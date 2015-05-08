/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.plugins.writer.mysqlwriter;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.LineReceiver;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.plugins.common.DBSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MysqlWriter extends Writer {
	private static List<String> encodingConfigs = null;

	static {
		encodingConfigs = new ArrayList<String>();
		encodingConfigs.add("character_set_client");
		encodingConfigs.add("character_set_connection");
		encodingConfigs.add("character_set_database");
		encodingConfigs.add("character_set_results");
		encodingConfigs.add("character_set_server");
	}

	private static Map<String, String> encodingMaps = null;
	static {
		encodingMaps = new HashMap<String, String>();
		encodingMaps.put("utf-8", "UTF8");
	}

	private static final int MAX_ERROR_COUNT = 65535;

	private String username = null;

	private String password = null;

	private String host = null;

	private String port = null;

	private String dbname = null;

	private String table = null;

	private String colorder = null;

	private String pre = null;

	private String post = null;

	private String encoding = null;

	private char sep = '\001';

	private String set = "";

	private String replace = "IGNORE";

	private double limit = 0;

	private int lineCounter = 0;

	/* since load-data mechanisms only allowes one thread to load data */
	private int concurrency = 1;

	private String sourceUniqKey = "";

	private static String DRIVER_NAME = "com.mysql.jdbc.Driver";

	private Connection connection = null;

	private Logger logger = Logger.getLogger(MysqlWriter.class);

	@Override
	public int init() {
		this.username = param.getValue(ParamKey.username, "");
		this.password = param.getValue(ParamKey.password, "");
		this.host = param.getValue(ParamKey.ip);
		this.port = param.getValue(ParamKey.port, "3306");
		this.dbname = param.getValue(ParamKey.dbname);
		this.table = param.getValue(ParamKey.table);
		this.colorder = param.getValue(ParamKey.colorder, "");
		this.pre = param.getValue(ParamKey.pre, "");
		this.post = param.getValue(ParamKey.post, "");
		this.encoding = param.getValue(ParamKey.encoding, "UTF8")
				.toLowerCase();
		this.limit = param.getDoubleValue(ParamKey.limit, 0);
		this.set = param.getValue(ParamKey.set, "");
		this.replace = param.getBoolValue(ParamKey.replace, false) ? "REPLACE"
				: "IGNORE";

		this.sourceUniqKey = DBSource.genKey(this.getClass(), host, port,
				dbname);

		if (!StringUtils.isBlank(this.set)) {
			this.set = "set " + this.set;
		}

		if (encodingMaps.containsKey(this.encoding)) {
			this.encoding = encodingMaps.get(this.encoding);
		}

		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int prepare(PluginParam param) {
		this.setParam(param);

		DBSource.register(this.sourceUniqKey, this.genProperties());

		if (StringUtils.isBlank(this.pre))
			return PluginStatus.SUCCESS.value();

		Statement stmt = null;
		try {
			this.connection = DBSource.getConnection(this.sourceUniqKey);

			stmt = this.connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);

			for (String subSql : this.pre.split(";")) {
				this.logger.info(String.format("Excute prepare sql %s .",
						subSql));
				stmt.execute(subSql);
			}

			return PluginStatus.SUCCESS.value();
		} catch (Exception e) {
			throw new DataExchangeException(e.getCause());
		} finally {
			try {
				if (null != stmt) {
					stmt.close();
				}
				if (null != this.connection) {
					this.connection.close();
					this.connection = null;
				}
			} catch (SQLException e) {
			}
		}
	}

	@Override
	public int post(PluginParam param) {
		if (StringUtils.isBlank(this.post))
			return PluginStatus.SUCCESS.value();

		/*
		 * add by bazhen.csy if (null == this.connection) { throw new
		 * DataExchangeException(String.format(
		 * "MysqlWriter connect %s failed in post work .", this.host)); }
		 */

		Statement stmt = null;
		try {
			this.connection = DBSource.getConnection(this.sourceUniqKey);

			stmt = this.connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);

			for (String subSql : this.post.split(";")) {
				this.logger.info(String.format("Excute prepare sql %s .",
						subSql));
				stmt.execute(subSql);
			}

			return PluginStatus.SUCCESS.value();
		} catch (Exception e) {
			throw new DataExchangeException(e.getCause());
		} finally {
			try {
				if (null != stmt) {
					stmt.close();
				}
				if (null != this.connection) {
					this.connection.close();
					this.connection = null;
				}
			} catch (Exception e2) {
			}

		}
	}

	@Override
	public int connect() {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int startWrite(LineReceiver receiver) {
		com.mysql.jdbc.Statement stmt = null;
		try {

			this.connection = DBSource.getConnection(this.sourceUniqKey);
			stmt = (com.mysql.jdbc.Statement) ((org.apache.commons.dbcp.DelegatingConnection) this.connection)
					.getInnermostDelegate().createStatement(
							ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_UPDATABLE);

			/* set max count */
			this.logger.info(String.format(
					"Config max_error_count: set max_error_count=%d",
					MAX_ERROR_COUNT));
			stmt.executeUpdate(String.format("set max_error_count=%d;",
					MAX_ERROR_COUNT));

			/* set connect encoding */
			this.logger.info(String.format("Config encoding %s .",
					this.encoding));
			for (String sql : this.makeLoadEncoding(encoding))
				stmt.execute(sql);

			/* load data begin */
			String loadSql = this.makeLoadSql();
			this.logger
					.info(String.format("Load sql: %s.", visualSql(loadSql)));

			MysqlWriterInputStreamAdapter localInputStream = new MysqlWriterInputStreamAdapter(
					receiver, this);
			stmt.setLocalInfileInputStream(localInputStream);
			stmt.executeUpdate(visualSql(loadSql));
			this.lineCounter = localInputStream.getLineNumber();

			this.logger.info("DataX write to mysql ends .");

			return PluginStatus.SUCCESS.value();
		} catch (Exception e2) {
			if (null != this.connection) {
				try {
					this.connection.close();
				} catch (SQLException e) {
				}
			}
			throw new DataExchangeException(e2.getCause());
		} finally {
			if (null != stmt)
				try {
					stmt.close();
				} catch (SQLException e3) {
				}
		}
	}

	private String quoteData(String data) {
		if (data == null || data.trim().startsWith("@")
				|| data.trim().startsWith("`"))
			return data;
		return ('`' + data + '`');
	}

	private String visualSql(String sql) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("\n", "\\n");
		map.put("\t", "\\t");
		map.put("\r", "\\r");
		map.put("\\", "\\\\");

		for (String s : map.keySet()) {
			sql = sql.replace(s, map.get(s));
		}
		return sql;
	}

	// colorder can not be null
	private String splitColumns(String colorder) {
		String[] columns = colorder.split(",");
		StringBuilder sb = new StringBuilder();
		for (String column : columns) {
			sb.append(quoteData(column.trim()) + ",");
		}
		return sb.substring(0, sb.lastIndexOf(","));
	}

	private String makeLoadSql() {
		String sql = "LOAD DATA LOCAL INFILE '`bazhen.csy.hedgehog`' "
				+ this.replace + " INTO TABLE ";
		// fetch table
		sql += this.quoteData(this.table);
		// fetch charset
		sql += " CHARACTER SET " + this.encoding;
		// fetch records
		sql += String.format(" FIELDS TERMINATED BY '\001' ESCAPED BY '\\' ");
		// sql += String.format(" FIELDS TERMINATED BY '%c' ", this.sep);
		// fetch lines
		sql += String.format(" LINES TERMINATED BY '\002' ");
		// fetch colorder
		if (this.colorder != null && !this.colorder.trim().isEmpty()) {
			sql += "(" + splitColumns(this.colorder) + ")";
		}
		// add set statement
		sql += this.set;
		sql += ";";
		return sql;
	}

	private List<String> makeLoadEncoding(String encoding) {
		List<String> ret = new ArrayList<String>();

		String configSql = "SET %s=%s; ";
		for (String config : encodingConfigs) {
			this.logger.info(String.format(configSql, config, encoding));
			ret.add(String.format(configSql, config, encoding));
		}

		return ret;
	}

	@Override
	public int commit() {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int finish() {
		Statement stmt = null;
		try {
			StringBuilder sb = new StringBuilder();

			String PATTERN = "row \\d+";
			Pattern p = Pattern.compile(PATTERN);
			Set<String> rowCounter = new HashSet<String>();

			stmt = this.connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs;

			sb.setLength(0);
			sb.append('\n');

			int warnCnts = 0;
			rs = stmt.executeQuery("SHOW WARNINGS;");
			while (rs.next()) {
				if (warnCnts++ < 32) {
					sb.append(rs.getString(1)).append(" ").append(rs.getInt(2))
							.append(" ").append(rs.getString(3)).append("\n");
				}

				Matcher matcher = p.matcher(rs.getString(3));
				if (matcher.find()) {
					rowCounter.add(matcher.group());
				}
			}

			if (!StringUtils.isBlank(sb.toString())) {

				if (rowCounter.size() > 32) {
					sb.append("More error messages hidden ...");
				}
				this.logger.warn(sb);

				if (this.limit >= 1 && rowCounter.size() >= this.limit) {
					this.logger.error(String.format(
							"%d rows data failed in loading.",
							rowCounter.size()));
					return PluginStatus.FAILURE.value();
				} else if (this.limit > 0 && this.limit < 1
						&& this.lineCounter > 0) {
					double rate = (double) rowCounter.size()
							/ (double) this.lineCounter;
					if (rate >= this.limit) {
						this.logger.error(String.format(
								"%.1f%% data failed in loading.", rate * 100));
						return PluginStatus.FAILURE.value();
					}
				} else {
					this.logger.warn(String.format(
							"MysqlWriter found %d rows data format error .",
							rowCounter.size()));
					// this.getMonitor().setFailedLines(rowCounter.size());
				}
			}
		} catch (SQLException e) {
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (null != this.connection)
					this.connection.close();
			} catch (Exception e) {
			}
		}
		return PluginStatus.SUCCESS.value();
	}

	private Properties genProperties() {
		Properties p = new Properties();
		p.setProperty("driverClassName", this.DRIVER_NAME);
		p.setProperty("url", String.format("jdbc:mysql://%s:%s/%s", this.host,
				this.port, this.dbname));
		p.setProperty("username", this.username);
		p.setProperty("password", this.password);
		p.setProperty("maxActive", String.valueOf(this.concurrency + 2));

		return p;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getColorder() {
		return colorder;
	}

	public void setColorder(String colorder) {
		this.colorder = colorder;
	}

	public String getPre() {
		return pre;
	}

	public void setPre(String pre) {
		this.pre = pre;
	}

	public String getPost() {
		return post;
	}

	public void setPost(String post) {
		this.post = post;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public double getLimit() {
		return limit;
	}

	public void setLimit(double limit) {
		this.limit = limit;
	}

	public char getSep() {
		return sep;
	}

	public void setSep(char sep) {
		this.sep = sep;
	}
}
