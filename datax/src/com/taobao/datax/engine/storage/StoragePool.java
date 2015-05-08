/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.engine.conf.EngineConf;
import com.taobao.datax.engine.conf.JobConf;
import com.taobao.datax.engine.conf.JobPluginConf;
import com.taobao.datax.engine.schedule.Engine;

/**
 * {@link StoragePool} manages multiple storages.
 * 
 * */
public class StoragePool {

	private Map<String, Storage> storageMap = new HashMap<String, Storage>();

	/**
	 * Constructor for {@link StoragePool}.
	 * Product one {@link Storage} for each {@link Writer}.
	 * 
	 * @param	jobConf	
	 * 			Configuration for job
	 * 
	 * @param 	engineConf	
	 * 			Configuration for {@link Engine}
	 * 
	 * @param 	period
	 * 			time period for {@link Storage} to report
	 * 
	 * */
	public StoragePool(JobConf jobConf, EngineConf engineConf, int period) {
		
		String storageClassName = engineConf.getStorageClassName();
        int lineLimit = engineConf.getStorageLineLimit();
		int byteLimit = engineConf.getStorageByteLimit();
		
		for (JobPluginConf jpc : jobConf.getWriterConfs()) {
			/*
             * Set storage class,lineLimit and byteLimit values using plugin params supported.
             * shenggong.wang@aliyun-inc.com
             * Oct 10,2011
             */
            String cStorageClassName = storageClassName;
            int cLineLimit = lineLimit;
            int cByteLimit = byteLimit;
            int destructLimit = jpc.getDestructLimit();
            
            try {
                cStorageClassName = jpc.getPluginParams().getValue("storageClassName", storageClassName);
                cLineLimit = jpc.getPluginParams().getIntValue("lineLimit", lineLimit);
                cByteLimit = jpc.getPluginParams().getIntValue("byteLimit", byteLimit);
                
            } catch (Exception e) {
                throw new DataExchangeException(e.getCause());
            }
            
			Storage s = StorageFactory.product(cStorageClassName);
			s.init(jpc.getId(), cLineLimit, cByteLimit, destructLimit);
			s.getStat().setPeriodInSeconds(period);
			storageMap.put(jpc.getId(), s);
		}
	}

	/**
	 * Return all {@link Storage}.
	 * 
	 * @return
	 * 			A list contains all {@link Storage}.
	 * 
	 * */
	public List<Storage> getStorageForReader() {
		List<Storage> ret = new ArrayList<Storage>();
		for (Storage s : storageMap.values()) {
			ret.add(s);
		}
		return ret;
	}

	/**
	 * Return {@link Storage} indexed by the id.
	 * 
	 * @param	id	
	 * 			{@link Writer} id
	 * 
	 * @return
	 * 			{@link Storage} for the {@link Writer}.
	 * 
	 * */
	public Storage getStorageForWriter(String id) {
		return storageMap.get(id);
	}

	/**
	 * Close channel which all the storages load data from data source.
	 * 
	 */
	public void closeInput() {
		for (String k : storageMap.keySet()) {
			storageMap.get(k).setPushClosed(true);
		}
	}

	/**
	 * Get the state of storage space during a period.
	 * 
	 * @return
	 * 			String of State during a period.
	 */
	public String getPeriodState() {
		StringBuilder sb = new StringBuilder(100);
		for (String k : storageMap.keySet()) {
			sb.append(storageMap.get(k).getStat().getPeriodState());
			storageMap.get(k).getStat().periodPass();
		}
		return sb.toString();
	}

	/**
	 * Get all the state of storage space.
	 * 
	 * @return
	 * 			String of all the State. 
	 * 
	 */
	public String getTotalStat() {
		StringBuilder sb = new StringBuilder(100);
		for (String k : storageMap.keySet()) {
			sb.append(storageMap.get(k).getStat().getTotalStat());
		}
		return sb.toString();
	}

}
