/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.plugin;

import java.util.List;

import com.taobao.datax.common.plugin.Line;
import com.taobao.datax.common.plugin.LineReceiver;
import com.taobao.datax.common.plugin.LineSender;
import com.taobao.datax.common.plugin.Reader;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.engine.storage.Storage;

/**
 * A buffer handler used by {@link Reader} to write data into {@link Storage}
 * or by {@link Writer} to read data from {@link Storage}.
 * 
 * <p>
 * This class has two faces, when it works with reader, it shows its {@link LineSender} face.
 * when it works with Writer, it shows its {@link LineReceiver} face. Both faces have used 
 * Buffering mechanism to work well.
 * </p>
 * 
 * */
public class BufferedLineExchanger implements LineSender, LineReceiver {

	static private final int DEFAUTL_BUF_SIZE = 64;

	/**	store data which reader put to StroeageForWrite area. */
	private Line[] writeBuf;

	/**	store data which {@link Writer} get from StroeageForRead area. */
	private Line[] readBuf;

	private int writeBufIdx = 0;

	private int readBufIdx = 0;

	private List<Storage> storageForWrite;

	private Storage storageForRead;

	/**
	 * Construct a {@link BufferedLineExchanger}.
	 * 
	 * @param	storageForRead
	 * 			Storage which {@link Writer} get data from.
	 * 
	 * @param	storageForWrite
	 * 			Storage which {@link Reader} put data to.
	 * 
	 */
	public BufferedLineExchanger(Storage storageForRead, List<Storage> storageForWrite) {
		this(storageForRead, storageForWrite, DEFAUTL_BUF_SIZE);
	}

	/**
	 * Construct a {@link BufferedLineExchanger}.
	 * 
	 * @param	storageForRead
	 * 			Storage which {@link Writer} get data from.
	 * 
	 * @param 	storageForWrite
	 * 			Storage which {@link Reader} put data to.
	 * 
	 * @param	bufSize
	 * 			Storage buffer size.
	 * 
	 */
	public BufferedLineExchanger(Storage storageForRead,
			List<Storage> storageForWrite, int bufSize) {
		this.storageForRead = storageForRead;
		this.storageForWrite = storageForWrite;
		this.writeBuf = new Line[bufSize];
		this.readBuf = new Line[bufSize];
	}

	/**
	 * Get next line of data which dumped to data destination.
	 * 
	 * @return
	 * 			next {@link Line}.
	 * 
	 */
	@Override
	public Line getFromReader() {
		if (readBufIdx == 0) {
			readBufIdx = storageForRead.pull(readBuf);
			if (readBufIdx == 0) {
				return null;
			}
		}
		return readBuf[--readBufIdx];
	}

	/**
	 * Construct one {@link Line} of data in {@link Storage} which will be used to exchange data.
	 * 
	 * @return 
	 * 			a new {@link Line}.
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
		if (writeBufIdx >= writeBuf.length) {
			writeAllStorage(writeBuf, writeBufIdx);
			writeBufIdx = 0;
		}
		writeBuf[writeBufIdx++] = line;
		return true;
	}

	/**
	 * Flush data in buffer (if exists) to {@link Storage}.
	 * 
	 * */
	@Override
	public void flush() {
		if (writeBufIdx > 0) {
			writeAllStorage(writeBuf, writeBufIdx);
		}
	}

	/**
	 * For test.
	 * 
	 * */
	@Override
	public boolean fakeSendToWriter(int lineLength) {
		for (Storage s : storageForWrite) {
			s.fakePush(lineLength);
		}
		return false;
	}

	/**
	 * Write buffered data(in a line array) to all storages which offer data to {@link Writer}.
	 * This method is the base of double write(data dumped to multiple destinations).
	 * 
	 * @param lines
	 * 			A line array buffered data.
	 * 
	 * @param size
	 * 			Limit of the line array.
	 * 
	 * @return
	 * 			True or False represents write data to storages success or fail.
	 * 
	 */
	private boolean writeAllStorage(Line[] lines, int size) {
		for (Storage s : this.storageForWrite) {
			s.push(lines, size);
		}
		return true;
	}

}
