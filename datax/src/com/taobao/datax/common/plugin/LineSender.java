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
 * DataX use {@link Storage} to help {@link Reader} and {@link Writer} to exchange data,s
 * {@link Reader} use {@link LineSender} to put data into {@link Storage}.
 * 
 *  @see LineReceiver
 *  @see BufferedLineExchanger
 *  
 * */
public interface LineSender {
	
	/**
	 * Construct one {@link Line} of data in {@link Storage} which will be used to exchange data.
	 * 
	 * @return
	 * 			a new {@link Line}.
	 * 
	 * */
	public Line createLine();
	
	/**
	 * Put one {@link Line} into {@link Storage}.
	 * 
	 * @param line	
	 * 			{@link Line} of data pushed into {@link Storage}.
	 * 
	 * @return
	 *			true for OK, false for failure.
	 *
	 * */
	public boolean sendToWriter(Line line);
	
	/**
	 * For test
	 * */
	public boolean fakeSendToWriter(int lineLength);
	
	/**
	 * Flush data in buffer (if exists) to {@link Storage}.
	 * 
	 * */
	public void flush();
}
