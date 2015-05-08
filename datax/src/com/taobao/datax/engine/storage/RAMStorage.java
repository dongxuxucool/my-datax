/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.storage;

import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

import com.taobao.datax.common.plugin.Line;
import com.taobao.datax.common.plugin.Reader;
import com.taobao.datax.common.plugin.Writer;


/**
 * A concrete storage which use RAM memory space to store the swap spaces. 
 * It provides a high-speed,safe way to realize data exchange.
 * 
 *@see {@link Storage}
 *@see {@link DoubleQueue}
 *@see {@link BufferedLineExchanger}
 */
public class RAMStorage extends Storage {
	private static final Logger log = Logger.getLogger(RAMStorage.class);

	private DoubleQueue mars = null;

	private final int waitTime = 3000; 

	public boolean init(String id, int lineLimit, int byteLimit,
			int destructLimit) {
		if (super.init(id, lineLimit, byteLimit, destructLimit) == false){
			return false;
		}
		this.mars = new DoubleQueue(lineLimit, byteLimit);
		return true;
	}

	
	/**
	 * Push one line into {@link Storage}, used by {@link Reader}
	 * 
	 * @param 	line
	 * 			One line of record which will push into storage, see {@link Line}
	 * 
	 * @return
	 * 			true for OK, false for failure.
	 * 
	 * */
	@Override
	public boolean push(Line line) {
		if (isPushClosed())
			return false;
		try {
			while (mars.offer(line, waitTime, TimeUnit.MILLISECONDS) == false) {
				getStat().incLineRRefused(1);
			}
		} catch (InterruptedException e) {
			return false;
		}
		getStat().incLineRx(1);
		getStat().incByteRx(line.length());
		return true;
	}


	/**
	 * Push multiple lines into {@link Storage}, used by {@link Reader}
	 * 
	 * @param 	lines
	 * 			multiple lines of records which will push into storage, see {@link Line}
	 * 
	 * @param 	size
	 * 			limit of line number to be pushed.
	 * 
	 * @return
	 * 			true for OK, false for failure.
	 * 
	 * */
	@Override
	public boolean push(Line[] lines, int size) {
		if (isPushClosed()) {
			return false;
		}

		try {
			while (mars.offer(lines, size, waitTime, TimeUnit.MILLISECONDS) == false) {
			
				getStat().incLineRRefused(1);
			
                /*
                 * If destruct limit value setted and line refused more than desctruct limit value,
                 * then close the Storage.
                 * shenggong.wang@aliyun-inc.com
                 * Oct 10, 2011
                 */
                if (getDestructLimit() > 0 && getStat().getLineRRefused() >= getDestructLimit()){
                	if (!isPushClosed()){
                		log.warn("Close RAMStorage for " + getStat().getId() + ". Queue:" + info() + " Timeout times:" + getStat().getLineRRefused());
                        setPushClosed(true);
                	}
                    return false;
                }
			
			}
		} catch (InterruptedException e) {
			return false;
		}

		getStat().incLineRx(size);
		for (int i = 0; i < size; i++) {
			getStat().incByteRx(lines[i].length());
		}

		return true;
	}

	@Override
	public boolean fakePush(int lineLength) {
		getStat().incLineRx(1);
		getStat().incByteRx(lineLength);
		return false;
	}

	/**
	 * Pull one line from {@link Storage}, used by {@link Writer}
	 * 
	 * @return 
	 * 			one {@link Line} of record.
	 * 
	 * */
	@Override
	public Line pull() {
		Line line = null;
		try {
			while ((line = mars.poll(waitTime, TimeUnit.MILLISECONDS)) == null) {
				getStat().incLineTRefused(1);
			}
		} catch (InterruptedException e) {
			return null;
		}
		if (line != null) {
			getStat().incLineTx(1);
			getStat().incByteTx(line.length());
		}
		return line;
	}

	/**
	 * Pull multiple lines from {@link Storage}, used by {@link Writer}
	 * 
	 * @param	lines
	 * 			an empty array which will be filled with multiple {@link Line} as the result.
	 * 
	 * @return
	 * 			number of lines pulled
	 * 
	 * */
	@Override
	public int pull(Line[] lines) {
		int readNum = 0;
		try {
			while ((readNum = mars.poll(lines, waitTime, TimeUnit.MILLISECONDS)) == 0) {
				getStat().incLineTRefused(1);
			}
		} catch (InterruptedException e) {
			return 0;
		}
		if (readNum > 0) {
			getStat().incLineTx(readNum);
			for (int i = 0; i < readNum; i++) {
				getStat().incByteTx(lines[i].length());
			}
		}
		if (readNum == -1) {
			return 0;
		}
		return readNum;
	}

	/**
	 * Get the used byte size of {@link Storage}.
	 * @return
	 * 			Used byte size of storage.
	 * 
	 */
	@Override
	public int size() {
		return mars.size();
	}

	/**
	 * Check whether the storage space is empty or not.
	 * @return
	 * 			true if empty, false if not empty.
	 * 
	 */
	@Override
	public boolean empty() {
		return (size() <= 0);
	}

	/**
	 * Get line number of the {@link Storage}
	 * 
	 * @return 
	 * 			Limit of the line number the {@link Storage} can hold.
	 * 
	 */
	@Override
	public int getLineLimit() {
		return mars.getLineLimit();
	}

	/**
	 * Get info of line number in {@link Storage} space. 
	 * 
	 * @return
	 * 			Information of line number.
	 * 
	 */
	@Override
	public String info() {
		return mars.info();
	}

	/**
	 * Set push state closed.
	 * 
	 * @param close
	 * 			A boolean value represents the wanted state of push.
	 * 
	 */
	@Override
	public void setPushClosed(boolean close) {
		super.setPushClosed(close);
		mars.close();
	}

}
