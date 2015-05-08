/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.common.plugin;

import java.util.List;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.plugins.writer.mysqlwriter.MysqlWriter;


/**
 * Define basic methods which concrete plugins should implement. This interface represents a plug_in
 * (When it runs, it represents a job).<br> 
 * Both {@link Reader} and {@link Writer} are plugins of DataX.<br>
 * You can extend {@link Reader} or {@link Writer} to implement your own {@link Pluginable}.
 * 
 * */
public interface Pluginable {
	/**
	 * Get job param related with this {@link Pluginable}.
	 * 
	 * @return
	 * 			{@link PluginParam} related with this job.
	 * 
	 * */
	public PluginParam getParam();
	
	/**
	 * Set job param related with this {@link Pluginable}.
	 * 
	 * @param	oParam
	 * 			{@link PluginParam} related with this job.
	 * 
	 * */
	public void setParam(PluginParam oParam);
	
	/**
	 * Get {@link PluginMonitor} of this job.
	 * 
	 * @return	
	 * 			{@link PluginMonitor} monitoring this {@link Pluginable}.
	 * 
	 * */
	public PluginMonitor getMonitor();
	
	/**
	 * Set {@link PluginMonitor} of this job.
	 * 
	 * @param	monitor
	 * 			{@link PluginMonitor} which will monitor this job.
	 * 
	 * */
	public void setMonitor(PluginMonitor monitor);
	
	/**
	 * Get name of the {@link Pluginable}.
	 * 
	 * @return 	
	 * 			name of the {@link Pluginable}.
	 * 
	 * */
	public String getPluginName();
	
	/**
	 * Set name of the {@link Pluginable}.
	 * 
	 * @param 	pluginName	
	 * 			name of the {@link Pluginable}.`
	 * 
	 * */
	public void setPluginName(String pluginName);
	
	/**
	 * Get version of the {@link Pluginable}.
	 * 
	 * @return
	 * 			version of the {@link Pluginable}.
	 * */
	public String getPluginVersion();
	
	/**
	 * Set version of the {@link Pluginable}.
	 * 
	 * @param	pluginVersion	
	 * 			version of the {@link Pluginable}.
	 * */
	public void setPluginVersion(String pluginVersion);	
	
	/**
	 * Split job into sub-jobs.
	 * 
	 * @param	param
	 * 			{@link PluginParam} of the job.
	 * 
	 * @return
	 * 			a list of sub-jobs' {@link PluginParam}.
	 * 
	 * @throws
	 * 			DataExchangeException, 
	 * 			
	 * */
	public List<PluginParam> split(PluginParam param);
	
	/**
	 * Prepare work in the job. e.g. when write data into mysql using {@link MysqlWriter}, 
	 * we can use prepare method to clean the table.
	 * 
	 * @param	param
	 *			{@link PluginParam} of this job.
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * @throws
	 * 			DataExchangeException, 
	 * */
	public int prepare(PluginParam param);
	
	/**
	 * Do post work in one job. e.g.  when write data into mysql using {@link MysqlWriter}, 
	 * we can use post method to mark this job ended to notify others.
	 * 
	 * @param	param	
	 * 			{@link PluginParam} of this job
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * @throws
	 * 			DataExchangeException, 
	 * */
	public int post(PluginParam param);
	
	/**
	 * Do clean work. e.g. disconnect database connection.
	 * 
	 * @return
	 *			0 for OK, others for failure.
	 * */
	public int cleanup();
	
	/**
	 * Get current meta data.
	 * 
	 * @return
	 * 			get current meta data.
	 * 
	 * */
	public MetaData getMyMetaData();

	/**
	 * Register Current meta data.
	 * 
	 * @param	md
	 * 			current meta data to be registered.
	 * 
	 * */
	public void setMyMetaData(MetaData md);

	/**
	 * Get opposite meta data.
	 * 
	 * @return
	 * 			get opposite meta data.
	 * 
	 * */
	public MetaData getOppositeMetaData();

	/**
	 * Set opposite meta data.
	 * NOTE: {@link Reader} is opposite to a {@link Writer}.
	 * 
	 * @param	md
	 * 			opposite meta data to be registered.
	 * 
	 * */
	public void setOppositeMetaData(MetaData md);
	
}
