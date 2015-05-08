/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.conf;

import java.util.List;

/**
 * DataX job configuration class.
 * NOTE: DataX can dump data into multiple destination.
 * 
 * @see Reader
 * @see Writer
 * 
 * */
public class JobConf {

	private String id;

	private JobPluginConf readerConf;

	private List<JobPluginConf> writerConfs;

	/**
	 * Get reader plugin's configuration.
	 * 
	 * @return
	 * 			a {@link JobPluginConf}.
	 * 
	 */
	public JobPluginConf getReaderConf() {
		return readerConf;
	}

	/**
	 * Set reader plugin's configuration.
	 * 
	 * @param	readerConf
	 *			a {@link JobPluginConf}.
	 *
	 */
	public void setReaderConf(JobPluginConf readerConf) {
		this.readerConf = readerConf;
	}

	/**
	 * Get writer plugin's configuration. The return value is a list, 
	 * this is designed to suit multiple data destination.
	 * 
	 * @return
	 * 			a list of {@link JobPluginConf}.
	 * 
	 */
	public List<JobPluginConf> getWriterConfs() {
		return this.writerConfs;
	}

	/**
	 * Set writer plugin's configuration.
	 * 
	 * @param	writerConfs
	 * 			a list of {@link JobPluginConf}.
	 * 
	 */
	public void setWriterConfs(List<JobPluginConf> writerConfs) {
		this.writerConfs = writerConfs;
	}

	/**
	 * Get number of writers.
	 * 
	 * @return
	 * 			number of writers.
	 *  
	 */
	public int getWriterNum() {
		return this.writerConfs.size();
	}

	/**
	 * Set job id.
	 * 
	 * @param	id
	 * 			job id.
	 * 
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get job id.
	 * 
	 * @return
	 * 			job id.
	 * 
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Check whether the job configuration content is right or not.
	 * 
	 * @return
	 * 			check result. True or false.
	 * 
	 */
	public boolean validate() {
		if (id.length() == 0) {
			return false;
		}
		if (!this.readerConf.validate()) {
			return false;
		}
		for (JobPluginConf dpc : this.writerConfs) {
			if (!dpc.validate()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Information of job configuration.
	 * 
	 * @return
	 * 			string of job configuration information.
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(300);
		sb.append(String.format("\njob:%s", this.getId()));
		sb.append("\nReader conf:");
		sb.append(this.readerConf.toString());
		sb.append(String.format("\n\nWriter conf [num %d]:", this.writerConfs.size()));
		for (JobPluginConf dpc : this.writerConfs) {
			sb.append(dpc.toString());
		}
		return sb.toString();
	}

}
