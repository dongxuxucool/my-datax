/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.common.util;

import java.io.IOException;
import java.lang.reflect.Field;

import com.taobao.datax.common.constants.Constants;

/**
 * A tool to add or get environment variable
 * 
 */
public final class EnvUtils {
	static {
		System.load(Constants.DATAX_LOCATION + "/common/libcommon.so");
	}

	private EnvUtils() {

	}

	/**
	 * Get environment variable specified by key
	 * 
	 * @param key
	 *            environment variable key
	 * 
	 * @return environment variable value
	 * */
	public native static String getEnv(String key);

	/**
	 * Set environment variable specified by key and value
	 * 
	 * @param key
	 *            environment variable key
	 * 
	 * 
	 * @param value
	 *            environment variable value
	 * 
	 * @return 0 for OK, others for failed
	 * */
	public native static int putEnv(String key, String value);

	
	/**
	 * Add library path to java.library.path
	 * 
	 * @param		s
	 * 					library path you want to add
	 * 
	 * @return		int
	 * 					0 for OK
	 * 
	 * @throws	IOException
	 * 					
	 * */
	public static synchronized int addLibraryPath(String s) throws IOException {
		if (null == s) {
			throw new IllegalArgumentException("Path cannot be null .");
		}

		try {
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
            for (String path : paths) {
                if (s.equals(path)) {
                    return 0;
                }
            }
			String[] tmp = new String[paths.length + 1];
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = s;
			field.set(null, tmp);
		
		} catch (IllegalAccessException e) {
			throw new IOException(
					"Failed to get permissions to set library path");
		} catch (NoSuchFieldException e) {
			throw new IOException(
					"Failed to get field handle to set library path");
		}

		return 0;
	}

}
