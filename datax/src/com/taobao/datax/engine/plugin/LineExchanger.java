/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.plugin;

import com.taobao.datax.common.plugin.Line;
import com.taobao.datax.common.plugin.LineReceiver;
import com.taobao.datax.common.plugin.LineSender;
import com.taobao.datax.common.plugin.Reader;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.engine.storage.Storage;

/**
 * A handler used by {@link Reader} to write into {@link Storage}
 * or by {@link Writer} to read from {@link Storage}.
 * 
 * @see BufferedLineExchanger
 * 
 * */
public class LineExchanger implements LineSender, LineReceiver {

	private Storage storage;

	/**
	 * Construct a {@link LineExchanger}.
	 * 
	 * @param 	storage
	 * 			a {@link Storage}.
	 * 
	 */
	public LineExchanger(Storage storage) {
		this.storage = storage;
	}

	/**
	 * Get next line of data writed to data destination.
	 * 
	 * @return
	 * 			next {@link Line}.
	 * 
	 */
	@Override
	public Line getFromReader() {
		return storage.pull();
	}

	/**
	 * Construct one {@link Line} of data in {@link Storage} which will be used to exchange data.
	 * 
	 * @return
	 * 			{@link Line}.
	 * 
	 * */
	@Override
	public Line createLine() {
		return new DefaultLine();
	}

	/**
	 * Put one {@link Line} into {@link Storage}.
	 * 
	 * @param 	line	
	 * 			{@link Line} of data pushed into {@link Storage}.
	 * 
	 * @return
	 *			true for OK, false for failure.
	 *
	 * */
	@Override
	public boolean sendToWriter(Line line) {
		return storage.push(line);
	}

	/**
	 * For test.
	 * 
	 * */
	@Override
	public boolean fakeSendToWriter(int lineLength) {
		storage.fakePush(lineLength);
		return false;
	}

	/**
	 * Flush data in buffer (if exists) to {@link Storage}.
	 * 
	 * */
	@Override
	public void flush() {
    }
}
