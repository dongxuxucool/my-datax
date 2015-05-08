/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.plugins.writer.hdfswriter;

import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Splitter;
import com.taobao.datax.common.util.SplitUtils;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class HdfsFileSplitter extends Splitter {
	private Logger logger = Logger.getLogger(HdfsFileSplitter.class);

	private Path p = null;

	private String prefix = "prefix";

	private int concurrency;
	
	private static final int RECOMMEND_CONCURRENCY = 5;

	@Override
	public int init() {
		String dir = param.getValue(ParamKey.dir);

		if (dir.endsWith("*")) {
			dir = dir.substring(0, dir.lastIndexOf("*"));
		}
		if (dir.endsWith("/")) {
			dir = dir.substring(0, dir.lastIndexOf("/"));
		}

		p = new Path(dir);

        prefix = param.getValue(ParamKey.prefixname, prefix);

		concurrency = param.getIntValue(ParamKey.concurrency,
				RECOMMEND_CONCURRENCY);

		return PluginStatus.SUCCESS.value();
	}

	@Override
	public List<PluginParam> split() {
		List<PluginParam> v = new ArrayList<PluginParam>();

		if (concurrency == 1) {
			logger.info(String
					.format("HdfsWriter set no splitting, Use %s as absolute filename .",
							p.toString() + "/" + prefix));

			param.putValue(ParamKey.dir, p.toString() + "/"
					+ prefix);
			return super.split(param);
		}

		logger.info(String.format("HdfsWriter splits file to %d sub-files .",
				concurrency));
		for (int i = 0; i < concurrency; i++) {
			String file = p.toString() + "/" + prefix + "-" + i;
			PluginParam oParams = SplitUtils.copyParam(param);
			oParams.putValue(ParamKey.dir, file);
			v.add(oParams);
		}

		return v;
	}

}
