/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.plugins.common;

import com.taobao.datax.common.plugin.*;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static java.text.MessageFormat.format;


/**
 * A proxy which provides to database {@link Writer} plugin. Usually this proxy is held by a 
 * Writer plugin. The proxy depends on jdbc service and write data to data destinamtion.
 * 
 * @see Reader
 * @see DBResultSetReceiver
 * 
 */
public class DBResultSetReceiver {
	
	private LineReceiver receiver;

	protected PluginMonitor monitor;
	
	private int columnCount;

	private static final Logger logger = Logger.getLogger(DBResultSetSender.class);
	
	/**
	 * A static factory method which provides {@link DBResultSetReceiver}.
	 * 
	 * @param	receiver
	 * 			a LineReceiver.
	 * 
	 * @return
	 * 			a DBResultSetReceiver instance.
	 * 
	 */
	public static DBResultSetReceiver newProxy(LineReceiver receiver) {
		return new DBResultSetReceiver(receiver);
	}

	/**
	 * A normal constructor of {@link DBResultSetReceiver}.
	 * 
	 * @param	receiver
	 * 			a LineReceiver.
	 * 
	 * @see DBResultSetReceiver#newProxy(LineSender)
	 * 
	 */
	public DBResultSetReceiver(LineReceiver receiver) {
		this.receiver = receiver;
	}
	
	/**
	 * Set Monitor.
	 * 
	 * @param	iMonitor
	 * 			a PluginMonitor.
	 * 
	 */
	public void setMonitor(PluginMonitor iMonitor) {
		this.monitor = iMonitor;
	}
	
	private void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}
	/**
	 * Get data from {@link Reader}. This method makeups sql sentence throws a String Pattern , 
	 * {@linkResultSetMetaData} and data in type of {@link Line} from {@link LineReceiver}, then 
	 * execute sql sentence by param {@link Statement}.
	 * 
	 * @param	receiver
	 * 			a {@link LineReceiver}.
	 * 
	 * @param	INSERT_PATTERN
	 * 			format of sql sentence.
	 * 
	 * @param	stmt
	 * 			a {@link Statement}.
	 * 
	 * @param meta
	 * 			a {@linkResultSetMetaData}.
	 * 
	 * @param	b
	 * 			True if sql pattern like: insert into tableName values {0}.<br>
	 * 			False if sql pattern like: insert into tableName {0} values {1}.
	 * 
	 * @throws	SQLException
	 * 			if occurs SQLException.
	 * 
	 */
	public void receiverFromReader(LineReceiver receiver, Connection conn, MetaData meta) throws SQLException {
		//PreparedStatement ps = conn.prepareStatement("");
	}
	
	public void getFromReader(LineReceiver receiver,String INSERT_PATTERN,Statement stmt,ResultSetMetaData meta,boolean b) throws SQLException{
		this.setColumnCount(meta.getColumnCount());	
		if(b){
			//表示insert语句模式为：insert into tableName values {0}
			Line line = null;
			String sql = "";
			while ((line = receiver.getFromReader()) != null) {
				StringBuilder valuseAfter = new StringBuilder('(');
				for(int i=1;i<columnCount;i++){
					valuseAfter.append("'").append(line.getField(i)).append("'");
					if(i!=columnCount-1){
						valuseAfter.append(",");
					}
				}
				valuseAfter.append(')');
				sql = format(INSERT_PATTERN,valuseAfter.toString());
				stmt.executeUpdate(sql);
			}
		}else{
			//表示insert语句模式为：insert into tableName {0} values {1}
			StringBuilder valuseBefore = new StringBuilder("(");
			for(int i=0;i<columnCount;i++){
				valuseBefore.append("'").append(meta.getColumnName(i)).append("'");
				if(i!=columnCount-1){
					valuseBefore.append(",");
				}
			}
			valuseBefore.append(")");
			
			Line line = null;
			String sql = "";
			while ((line = receiver.getFromReader()) != null) {
				StringBuilder valuseAfter = new StringBuilder('(');
				for(int i=1;i<columnCount;i++){
					valuseAfter.append("'").append(line.getField(i)).append("'");
					if(i!=columnCount-1){
						valuseAfter.append(",");
					}
				}
				valuseAfter.append(')');
				
				sql = format(INSERT_PATTERN,valuseBefore.toString(),valuseAfter.toString());
				stmt.executeUpdate(sql);
			}
		}
	}
	
}
