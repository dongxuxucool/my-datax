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

/**
 * A tool class used to do some operation on Array. 
 * The function of this Tool class is indicated by the name of its methods.
 * 
 */
public abstract class ArrayUtils {
	
	/**
	 * As its name, copy an int array. In fact, 
	 * method System.arraycopy(src, srcPos, dest, destPos, length) is invoked by this method.
	 * 
	 * @param iarr
	 * 			array(int type) needs copy.
	 * @return
	 * 			a copy result.
	 * 
	 * @see ArrayUtils#copy(String[])
	 * 
	 */
	public static int[] copy(int[] iarr) {
		int[] oarr = null;
		if (iarr != null) {
			oarr = new int[iarr.length];
			System.arraycopy(iarr, 0, oarr, 0, oarr.length);
		}
		return oarr;
	}
	
	/**
	 * @see ArrayUtils#copy(int[])
	 * 
	 */
	public static String[] copy(String[] iarr) {
		String[] oarr = null;
		if (iarr != null) {
			oarr = new String[iarr.length];
			System.arraycopy(iarr, 0, oarr, 0, oarr.length);
		}
		return oarr;
	}
	
	public static<T> List<List<T>> spitList(final List<T> all, final int length) {
		List<List<T>> batches = new ArrayList<List<T>>();
		for (int i = 0, s = 0, t = 0; s < all.size(); s = t, i++) {
			t = s + length;
			if (t > all.size()) {
				t = all.size();
			}
			batches.add(all.subList(s, t));
		}
		return batches;
	}
	
}
