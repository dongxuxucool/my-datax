/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionTracker {
	public static final int STRING_BUFFER = 1024;

	public static String trace(Exception ex) {
		StringWriter sw = new StringWriter(STRING_BUFFER);
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		return sw.toString();
	}
}
