/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.plugins.common;

import com.taobao.datax.common.plugin.MetaData;
import com.taobao.datax.common.plugin.Reader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DBUtils {
	private DBUtils() {
	}
	
	/**
	 * a wrapped method to execute select-like sql statement .
	 * 
	 * @param		conn
	 * 					Database connection .
	 * 
	 * @param		sql
	 * 					sql statement to be executed
	 * 
	 * @return
	 * 					a {@link ResultSet}
	 * 
	 * @throws	SQLException
	 * 					if occurs SQLException.
	 * 
	 * */
	public static ResultSet query(Connection conn, String sql) throws SQLException {
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		return query(stmt, sql);
	}
	
	
	/**
	 * a wrapped method to execute select-like sql statement .
	 * 
	 * @param	stmt
	 * 			{@link Statement}
	 * 
	 * @param	sql
	 * 			sql statement to be executed
	 * 
	 * @return
	 * 			a {@link ResultSet}
	 * 
	 * @throws	SQLException 
	 * 			if occurs SQLException.
	 * 
	 * */
	public static ResultSet query(Statement stmt, String sql) throws SQLException {
		return stmt.executeQuery(sql);
	}
	
	/**
	 * Close {@link ResultSet}, {@link Statement} referenced by this {@link ResultSet}
	 * 
	 * @param	rs
	 * 			{@link ResultSet} to be closed
	 * 
	 * @throws	IllegalArgumentException
	 * 
	 * */
	public static void closeResultSet(ResultSet rs) {
		try {
			if (null != rs) {
				Statement stmt = rs.getStatement();
				if (null != stmt) {
					stmt.close();
					stmt = null;
				}
				rs.close();
			}
			rs = null;
		} catch (SQLException e) {
			throw new IllegalStateException(e.getCause());
		}
	}
	
	/**
	 * a utility to help generate {@link MetaData}
	 * 
	 * @param	conn
	 * 			{@link Connection} for this {@link Reader}
	 * 
	 * @param	sql
	 * 			sql to select columns
	 * 
	 * @return				
	 * 			{@link MetaData} 
	 * @throws	SQLException 
	 * 			if occurs SQLException.
	 * 
	 * */
	public static MetaData genMetaData(Connection conn, String sql)
			throws SQLException {
		MetaData meta = new MetaData();
		List<MetaData.Column> columns = new ArrayList<MetaData.Column>();

		ResultSet resultSet = null;
		try {
			resultSet = query(conn, sql);
			int columnCount = resultSet.getMetaData().getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				MetaData.Column col = meta.new Column();
				col.setColName(resultSet.getMetaData().getColumnName(i)
						.toLowerCase().trim());
				col.setDataType(resultSet.getMetaData().getColumnTypeName(i)
						.toLowerCase().trim());
				columns.add(col);
			}
			meta.setColInfo(columns);
			meta.setTableName(resultSet.getMetaData().getTableName(1).toLowerCase());
		} finally {
			closeResultSet(resultSet);
		}
		
		return meta;
	}
	
}