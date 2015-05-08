/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.common.plugin;

/**
 * DataX use {@link Storage} to help {@link Reader} and {@link Writer} to exchange data,
 * {@link Writer} uses {@link LineReceiver} to get data from {@link Storage}(Usually in memory).
 *  
 * @see LineSender
 * @see BufferedLineExchanger
 * 
 * */
public interface LineReceiver {
	
	/**
	 * Fetch the next {@link Line} from {@link Storage}.
	 * 
	 * @return	{@link Line}
	 * 			next {@link Line} in {@link Storage}, null if read to end.
	 * 
	 * */
	public Line getFromReader();
}
