/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.plugins.writer.oraclewriter;

import java.util.ArrayList;
import java.util.List;

import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Splitter;
import com.taobao.datax.common.util.SplitUtils;


public class OracleWriterSplitter extends Splitter {
	private int concurrency = 1;

	@Override
	public int init() {
		concurrency = param.getIntValue(ParamKey.concurrency, 1);
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public List<PluginParam> split() {
		List<PluginParam> v = new ArrayList<PluginParam>();
		for (int i = 0; i < concurrency; i++) {
			PluginParam oParams = SplitUtils.copyParam(this.getParam());
			v.add(oParams);
		}
		return v;
	}

}
