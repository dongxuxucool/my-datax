/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.conf;

import com.taobao.datax.common.plugin.PluginParam;

/**
 * Describes a plugin's configuration used by job.
 * 
 * @see JobConf
 * 
 * */
public class JobPluginConf {	
	private String id;

	private String name;

	private PluginParam pluginParams;

	private int destructLimit = 0; 

	private static final int THREAD_MIN = 0;
	
	private static final int THREAD_MAX = 64;
	
	/**
	 * Get job id
	 * 
	 * @return
	 * 			job id.
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set job id.
	 * 
	 * @param	id
	 * 			job id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get job name.
	 * 
	 * @return
	 * 			job name.
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set job name.
	 * 
	 * @param		name
	 * 			job name.
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get core amount of threads.
	 * 
	 * @return
	 * 			core amount of threads.
	 * 
	 */
	public int getConcurrency() {
		return this.pluginParams.getIntValue("concurrency");
	}

	/**
	 * Set core amount of threads.
	 * 
	 * @param	 concurrency
	 * 			core amount of threads.
	 * 
	 */
	public void setConcurrency(int concurrency) {
		this.pluginParams.putValue("concurrency", String.valueOf(concurrency));
	}

	/**
	 * Get {@link PluginParam} of job.
	 * 
	 * @return
	 * 			{@link PluginParam} of job.
	 *
	 */
	public PluginParam getPluginParams() {
		return pluginParams;
	}

	/**
	 * Set {@link PluginParam} of job.
	 * 
	 * @param	plugParams
	 * 			{@link PluginParam} of job.
	 */
	public void setPluginParams(PluginParam plugParams) {
		this.pluginParams = plugParams;
	}

	/**
	 * Get destruct limit.(The method Nearly not use.)
	 * 
	 * @return
	 * 			destruct limit.
	 * 
	 */
	public int getDestructLimit() {
		return destructLimit;
	}

	/**
	 * Set destruct limit.(The method Nearly not use.)
	 * 
	 * @param destructLimit
	 * 			destruct limit.
	 */
	public void setDestructLimit(int destructLimit) {
		this.destructLimit = destructLimit;
	}

	/**
	 * Check whether the configuration content is right or not.
	 * 
	 * @return
	 * 			check result. True or false.
	 * 
	 */
	boolean validate() {
		int concurrency = this.getConcurrency();
        return !(concurrency < THREAD_MIN || concurrency > THREAD_MAX);
    }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(String.format("\nname:%s id %s, pool(%d) destruct(%d)",
				this.getName(), this.getId(), 
				this.getConcurrency(), this.getDestructLimit()));
		sb.append(String.format("\nparams:%s", this.getPluginParams()
				.toString()));
		return sb.toString();
	}
}
