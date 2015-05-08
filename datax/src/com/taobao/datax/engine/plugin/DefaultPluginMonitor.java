/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.plugin;

import org.apache.log4j.Logger;

import com.taobao.datax.common.plugin.Pluginable;
import com.taobao.datax.common.plugin.PluginMonitor;
import com.taobao.datax.common.plugin.PluginStatus;


/**
 * Default implement to {@link PluginMonitor} which monitors {@link Pluginable} status, 
 * record number of lines, failed number of lines, etc.
 * 
 * */
public class DefaultPluginMonitor implements PluginMonitor {

	private Logger logger = Logger.getLogger(DefaultPluginMonitor.class);

	private long successedLines;

	private long failedLines;

	private PluginStatus status;

	private String targetName;

	private int targetId;

	/**
	 * Construct a {@link DefaultPluginMonitor} and initialize its status.
	 */
	public DefaultPluginMonitor() {
		successedLines = 0;
		failedLines = 0;
		targetId = 0;
		this.status = PluginStatus.WAITING;
	}

	/**
	 * Get the monitored {@link Pluginable} name .
	 * 
	 * @return
	 * 			name of the monitored {@link Pluginable}.
	 * 
	 * */
	@Override
	public String getTargetName() {
		return targetName;
	}

	/**
	 *  Set the monitored {@link Pluginable} name.
	 *  
	 *  @param	targetName
	 * 			name of the monitored {@link Pluginable}.
	 * 
	 * */
	@Override
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	/**
	 * Get the monitored {@link Pluginable} id.
	 * 
	 * */
	@Override
	public int getTargetId() {
		return targetId;
	}

	/**
	 * Set the monitored {@link Pluginable} id.
	 * 
	 * @param targetId
	 * 			id of the monitored {@link Pluginable}.
	 * 
	 */
	@Override
	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	/**
	 * Get status of monitored {@link Pluginable}.
	 * 
	 * @return
	 * 			{@link PluginStatus} which contains more details of the monitored plugin.
	 * 		
	 * */
	@Override
	public PluginStatus getStatus() {
		return status;
	}

	/**
	 * Set status of {@link Pluginable} .
	 * 
	 * @param	status
	 * 			see {@link PluginStatus}
	 * 
	 * */
	@Override
	public void setStatus(PluginStatus status) {
		this.status = status;
	}

	/**
	 * Get number of failed lines.
	 * 
	 * @return
	 * 			number of failed lines
	 * 
	 * */
	@Override
	public long getFailedLines() {
		return failedLines;
	}

	/**
	 * Get number of successful lines.
	 * 
	 * @return
	 * 			number of successful lines.
	 * 
	 * */
	@Override
	public long getSuccessedLines() {
		return successedLines;
	}

	/**
	 * Increase the number of failed lines by 1 .
	 * 
	 * @param	info
	 * 			failure information.
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * */
	@Override
	public int lineFail(String info) {
		failedLines++;
		return 0;
	}

	/**
	 * Increase the number of successful lines by step of one.
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * */
	@Override
	public int lineSuccess() {
		successedLines++;
		return 0;
	}

	/**
	 * Set number of failed lines .
	 * 
	 * @param	num	
	 * 			number of failed lines.
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 */
	@Override
	public int setFailedLines(long i) {
		failedLines = i;
		return 0;
	}

	/**
	 * Set number of successful lines .
	 * 
	 * @param	num	
	 * 			number of successful lines
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 */
	@Override
	public int setSuccessedLines(long i) {
		successedLines = i;
		return 0;
	}

}
