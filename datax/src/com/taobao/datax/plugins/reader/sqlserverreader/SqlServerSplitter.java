/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.plugins.reader.sqlserverreader;

import java.util.ArrayList;
import java.util.List;

import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Splitter;
import com.taobao.datax.common.util.SplitUtils;


/**
 * 根据配置的tables数量分割查询
 */
public class SqlServerSplitter extends Splitter{
	
	public SqlServerSplitter(PluginParam iParam) {
		param = iParam;
	}

	public List<PluginParam> split() {
		List<PluginParam> paramList = new ArrayList<PluginParam>();
		
		List<String> sqls = SqlServerSqlGenerator.instance(param).generate();
		for (String sql : sqls) {
			PluginParam iParam = SplitUtils.copyParam(param);
			iParam.putValue(ParamKey.sql, sql);
			paramList.add(iParam);
		}
		return paramList;
	}

	@Override
	public int init() {
		return PluginStatus.SUCCESS.value();
	}
}
