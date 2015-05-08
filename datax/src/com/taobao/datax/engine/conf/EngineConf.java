/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.conf;

import com.taobao.datax.engine.schedule.Engine;

/**
 * Configuration of Engine. Include the core information of Engine. <br>
 * Such as storageLineLimit, storageByteLimit, storageBufferSize, pluginRootPath.
 * 
 * @see	{@link Engine}
 * 
 */
public class EngineConf {
	private String storageClassName;

	private int version;

	private int storageLineLimit;

	private int storageByteLimit;

	private int storageBufferSize;

	private String pluginRootPath;
	
	static private EngineConf instance;
	
	private EngineConf(){
	}
	
	/**
	 * The unique way to construct a EnginConf instance.
	 *
        * @return   an instance of engine configuration
        */
	static public EngineConf getInstance() {
		if (null == instance) {
			instance = new EngineConf();
		}
		return instance;
	}
	
	/**
	 * Get the name of storageclass.
	 * 
	 * @return	StoreageClassName
	 * 			name of storageclass.
	 * 
	 */
	public String getStorageClassName() {
		return storageClassName;
	}

	/**
	 * Set the name of storageclass.
	 * 
	 * @param	storageClassName
	 * 			name of storageclass.
	 * 
	 */
	public void setStorageClassName(String storageClassName) {
		this.storageClassName = storageClassName;
	}

	/**
	 * Get version if DataX.
	 * 
	 * @return	version
	 * 			version of DataX.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Set version of DataX.
	 * 
	 * @param	version
	 * 			version of DataX.
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Get limit of one storeageline.
	 * 
	 * @return	storageLineLimit
	 * 			limit of one storageLineLimit.
	 */
	public int getStorageLineLimit() {
		return storageLineLimit;
	}

	/**
	 * Set limit of one storeageline.
	 * 
	 * @param	storageLineLimit
	 * 			limit of one storeageline.
	 * 
	 */
	public void setStorageLineLimit(int storageLineLimit) {
		this.storageLineLimit = storageLineLimit;
	}
 
	/**
	 * Get limit of storeagebyte.
	 * 
	 * @return	storageLineLimit
	 * 			limit of storeagebyte.
	 * 
	 */
	public int getStorageByteLimit() {
		return storageByteLimit;
	}

	/**
	 * Set limit of one storeagebyte.
	 * 
	 * @param	storageByteLimit
	 * 			limit of one storeagebyte.
	 * 
	 */
	public void setStorageByteLimit(int storageByteLimit) {
		this.storageByteLimit = storageByteLimit;
	}

	/**
	 * Get rootpath of the plugin.
	 * 
	 * @return	pluginRootPath
	 * 			rootpath of the plugin.
	 * 
	 */
	public String getPluginRootPath() {
		return pluginRootPath;
	}

	/**
	 * Set rootpath of the plugin.
	 * 
	 * @param	pluginRootPath
	 * 			rootpath of plugin.
	 * 
	 */
	public void setPluginRootPath(String pluginRootPath) {
		this.pluginRootPath = pluginRootPath;
	}

	/**
	 * Get size of storagebuffer.
	 * 
	 * @return	storageBufferSize
	 * 			size of storagebuffer.
	 * 
	 */
	public int getStorageBufferSize() {
		return storageBufferSize;
	}

	/**
	 * Set size of storagebuffer.
	 * 
	 * @param	storageBufferSize
	 * 			size of storagebuffer.
	 * 
	 */
	public void setStorageBufferSize(int storageBufferSize) {
		this.storageBufferSize = storageBufferSize;
	}

	 /**
     * @return
     * 			the basic info of engine.
     */
	public String toString() {
         return String
                 .format("DataX engine [version=%d] storage[%s size %d/%d buffer %d] pluginpath[%s]",
                         this.getVersion(), this.getStorageClassName(),
                         this.getStorageLineLimit(), this.getStorageByteLimit(),
                         this.getStorageBufferSize(), this.getPluginRootPath());
	}

}
