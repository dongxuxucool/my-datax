/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.PluginParam;

public class SplitUtils {
	private static Pattern pattern = Pattern
			.compile("(\\w+)\\[(\\d+)-(\\d+)\\](.*)");

	private static Logger logger = Logger.getLogger(SplitUtils.class);

	private SplitUtils() {
	}
	
	/**
	 * Split the table string(Usually contains names of some tables) to a List
	 * that is formated.
	 * example:
	 * tbl[0-32] will be splitted into tbl0, tbl1, tbl2, ... ,tbl32 in {@link List}
 	 * 
	 * @param	tables
	 *          a string contains table name(one or many).
	 * 
	 * @return a split result of table name.
	 * 
	 * @throws DataExchangeException
	 * 
	 */
	public static List<String> splitTables(String tables) {
		List<String> tableIds = new ArrayList<String>();

		String[] tableArrays = tables.split(",");
        for (String tableArray : tableArrays) {
            Matcher matcher = pattern.matcher(tableArray.trim());
            if (!matcher.matches()) {
                tableIds.add(tableArray.trim());
            } else {
                String start = matcher.group(2).trim();
                String end = matcher.group(3).trim();
                String tmp = "";
                if (Integer.valueOf(start) > Integer.valueOf(end)) {
                    tmp = start;
                    start = end;
                    end = tmp;
                }
                int len = start.length();
                for (int k = Integer.valueOf(start); k <= Integer.valueOf(end); k++) {
                    if (start.startsWith("0")) {
                        logger.debug(matcher.group(1).trim()
                                + String.format("%0" + len + "d", k)
                                + matcher.group(4).trim());
                        tableIds.add(matcher.group(1).trim()
                                + String.format("%0" + len + "d", k)
                                + matcher.group(4).trim());
                    } else
                        tableIds.add(matcher.group(1).trim()
                                + String.format("%d", k)
                                + matcher.group(4).trim());
                }
            }
        }
		return tableIds;
	}

	/**
	 * Copy param whose type is {@link PluginParam}.
	 * 
	 * @param	iParam
	 * 			param(PluginParam type) needs to copy.
	 * 
	 * @return
	 * 			a copy result.
	 * 
	 */
	public static PluginParam copyParam(PluginParam iParam) {
		return iParam.clone();
	}
	
}
