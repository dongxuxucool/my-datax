/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.common.plugin;

import java.util.ArrayList;
import java.util.List;

import com.taobao.datax.common.plugin.MetaData;
import com.taobao.datax.common.plugin.Pluginable;
import com.taobao.datax.common.plugin.PluginMonitor;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.Reader;
import com.taobao.datax.common.plugin.Writer;

/**
 * Default implementation of {@link Pluginable}.
 * 
 * @see Pluginable
 * 
 * */
public class AbstractPlugin implements Pluginable {
	
	protected PluginParam param;

	protected PluginMonitor monitor;

	private String pluginName;

	private String pluginVersion;
	
	private MetaData myMetaData;

	private MetaData oppositeMetaData;

	/**
	 * Get job param related with this {@link AbstractPlugin}.
	 * 
	 * @return
	 * 			{@link PluginParam} related with this job.
	 * 
	 * */
	@Override
	public PluginParam getParam() {
		return param;
	}

	
	/**
	 * Set job param related with this {@link AbstractPlugin} .
	 * 
	 * @param	oParam
	 * 			{@link PluginParam} related with this job.
	 * 
	 * */
	@Override
	public void setParam(PluginParam param) {
		this.param = param;
	}

	/**
	 * Get name of the {@link AbstractPlugin} .
	 * 
	 * @return 	
	 * 			name of the {@link AbstractPlugin} .
	 * 
	 * */
	@Override
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * Set name of the {@link AbstractPlugin} .
	 * 
	 * @param 	pluginName	
	 * 			name of the {@link AbstractPlugin} .
	 * 
	 * */
	@Override
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	/**
	 * Get version of the {@link AbstractPlugin}.
	 * 
	 * @return
	 * 			version of the {@link AbstractPlugin}.
	 * 
	 * */
	@Override
	public String getPluginVersion() {
		return pluginVersion;
	}

	
	/**
	 * Set version of the {@link AbstractPlugin}.
	 * 
	 * @param	pluginVersion	
	 * 			version of the {@link AbstractPlugin}.
	 * 
	 * */
	@Override
	public void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	/**
	 * Get {@link PluginMonitor} of this job.
	 * 
	 * @return
	 * 			{@link PluginMonitor} monitoring this {@link Pluginable}
	 * 
	 * */
	@Override
	public PluginMonitor getMonitor() {
		return monitor;
	}

	/**
	 * Set {@link PluginMonitor} of this job.
	 * 
	 * @param	monitor
	 * 			{@link PluginMonitor} which will monitor this job
	 * 
	 * */
	@Override
	public void setMonitor(PluginMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Do clean work. e.g. disconnect database connection .
	 * 
	 * @return
	 *			0 for OK, others for failed .
	 *
	 * */
	@Override
	public int cleanup() {
		return 0;
	}

	/**
	 * Prepare work in the job.
	 * e.g. when write data into mysql using {@link MysqlWriter}, 
	 * we can use prepare method to clean the table.
	 * 
	 * @param			param
	 *						{@link PluginParam} of this job.
	 * 
	 * @return
	 * 						0 for OK, others for failure.
	 * 
	 * */
	@Override
	public int prepare(PluginParam param) {
		return 0;
	}

	/**
	 * Do post work in one job.
	 * e.g.when write data into mysql using {@link MysqlWriter}, 
	 * we can use post method to mark this job ended to notify others.
	 * 
	 * @param	param	
	 * 			{@link PluginParam} of this job
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * */
	@Override
	public int post(PluginParam param) {
		return 0;
	}

	/**
	 * Split job into sub-jobs.
	 * 
	 * @param	param
	 * 			{@link PluginParam} of the job.
	 * 
	 * @return
	 * 			a list of sub-jobs' {@link PluginParam}.
	 * 
	 * */
	@Override
	public List<PluginParam> split(PluginParam param) {
		List<PluginParam> paramList = new ArrayList<PluginParam>();
		paramList.add(param);
		return paramList;
	}
	
	/**
	 * Get current meta data .
	 * 
	 * @return
	 * 			Get current meta data .
	 * 
	 * */
	@Override
	public MetaData getMyMetaData() {
		return this.myMetaData;
	}

	/**
	 * Register Current meta data .
	 * 
	 * @param	md
	 * 			Current meta data to be registered.
	 * 
	 * */
	@Override
	public void setMyMetaData(MetaData md) {
		this.myMetaData = md;
	}

	/**
	 * Get opposite meta data .
	 * 
	 * @return
	 * 			Get opposite meta data.
	 * 
	 * */
	@Override
	public MetaData getOppositeMetaData() {
		return this.oppositeMetaData;
	}

	/**
	 * Set opposite meta data .
	 * NOTE: {@link Reader} is opposite to a {@link Writer}
	 * 
	 * @param	md
	 * 			Opposite meta data to be registered.
	 * 
	 * */
	@Override
	public void setOppositeMetaData(MetaData md) {
		this.oppositeMetaData = md;
	}

}
