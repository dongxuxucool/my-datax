/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.schedule;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.PluginMonitor;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.engine.plugin.DefaultPluginMonitor;
import com.taobao.datax.engine.storage.Storage;
import org.apache.log4j.Logger;

import javax.management.monitor.Monitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection of {@link Monitor},
 * 
 * */
public class MonitorPool {
	private static final Logger logger = Logger.getLogger(MonitorPool.class);

	private List<PluginMonitor> monitors;

	/**
	 * Constructor
	 * */
	public MonitorPool() {
		monitors = new ArrayList<PluginMonitor>();
	}

	/**
	 * Add a {@link PluginWorker} to monitor pool.
	 * 
	 * @param	worker
	 * 			{@link PluginWorker}
	 * 
	 * @throws	DataExchangeException
	 * 
	 * */
	public void monitor(PluginWorker worker) {
		PluginMonitor monitor = new DefaultPluginMonitor();
		monitor.setTargetName(worker.getPluginName());
		monitor.setTargetId(worker.getMyIndex());
		worker.setMonitor(monitor);
		monitors.add(monitor);
	}

	/**
	 * Print statistics of {@link Storage}.
	 * 
	 * */
	public void stat() {
		long successLine = 0;
		long failedLine = 0;
		Map<String, Integer> statusCnt = new HashMap<String, Integer>();
		for (PluginMonitor m : monitors) {
			successLine += m.getSuccessedLines();
			failedLine += m.getFailedLines();
			Integer cnt = statusCnt.get(m.getStatus().toString());
			if (cnt != null)
				cnt += 1;
			else
				statusCnt.put(m.getStatus().toString(), 1);
		}
		logger.info(String.format("Success line %d, Failed line %d",
				successLine, failedLine));
		for (PluginStatus m : PluginStatus.values()) {
			if (statusCnt.get(m.toString()) != null)
				logger.info(String.format("Status %s Cnt %d", m.toString(),
						statusCnt.get(m.toString())));
		}
	}

	/**
	 * Get line discarded by DataX, e.g. dirty data can cause DataX discard this record.
	 * 
	 * @return		
	 * 			number of line discarded.
	 * 
	 * */
	public long getDiscardLine() {
		long discardLine = 0;
		for (PluginMonitor m : monitors) {
			discardLine += m.getFailedLines();
		}
		return discardLine;
	}
}
