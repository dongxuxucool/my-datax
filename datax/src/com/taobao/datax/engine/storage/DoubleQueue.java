/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.storage;

import com.taobao.datax.common.plugin.Line;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * A import class in DataX, represents a region with two swap spaces, one for
 * storing data which from data source, the other one for storing data which 
 * will be transferred to data destination.
 * <p>
 * A classical DoubleQueue, In beginning, space A and space B both empty, then
 * loading task begin to load data to space A, when A is almost full, let the
 * data from data source being loaded to space B, then dumping task begin to
 * dump data from space A to data source. When space A is empty, switch the two
 * spaces for load and dump task. Repeat the above operation.
 * </p>
 * 
 */
public class DoubleQueue extends AbstractQueue<Line> implements
		BlockingQueue<Line>, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	private int lineLimit;
	
	private final Line[] itemsA;
	
	private final Line[] itemsB;

	private ReentrantLock readLock, writeLock;
	
	private Condition notEmpty;
	
	private Condition notFull;
	
	private Condition awake;

	/**
	 * writeArray : in reader's eyes, reader get data from data source and write data to this line array.
	 * readArray : in writer's eyes, writer put data to data destination from this line array.
	 * 
	 * Because of this is doubleQueue mechanism, the two line will exchange when time is suitable.
	 * 
	 */
	private Line[] writeArray, readArray;
	
	private volatile int writeCount, readCount;
	
	private int writeArrayHP, writeArrayTP, readArrayHP, readArrayTP;
	
	private int byteLimit;
	
	private boolean closed = false;
	
	private int spillSize = 0;

	private long lineRx = 0;
	
	private long lineTx = 0;
	
	/**	received byte number of data from data source(eg:httpreader load data from httpurl) */
	private long byteRx = 0;

	/**
	 * Get info of line number in {@link DoubleQueue} space. 
	 * 
	 * @return
	 * 			Information of line number.
	 * 
	 */
	public String info() {
		return lineRx + ":" + lineTx;
	}

	/**
	 * Use the two parameters to construct a {@link DoubleQueue} which hold the swap areas.
	 * 
	 * @param	lineLimit
	 * 			Limit of the line number the {@link DoubleQueue} can hold.
	 * 
	 * @param	byteLimit
	 * 			Limit of the bytes the {@link DoubleQueue} can hold.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public DoubleQueue(int lineLimit, int byteLimit) {
		if (lineLimit <= 0 || byteLimit <= 0) {
			throw new IllegalArgumentException(
					"Queue initial capacity can't less than 0!");
		}
		this.lineLimit = lineLimit;
		this.byteLimit = byteLimit;
		itemsA = new Line[lineLimit];
		itemsB = new Line[lineLimit];

		readLock = new ReentrantLock();
		writeLock = new ReentrantLock();

		notEmpty = readLock.newCondition();
		notFull = writeLock.newCondition();
		awake = writeLock.newCondition();

		readArray = itemsA;
		writeArray = itemsB;
		spillSize = lineLimit * 8 / 10;
	}

	/**
	 * Get line number of the {@link DoubleQueue}
	 * 
	 * @return	lineLimit 
	 * 			Limit of the line number the {@link DoubleQueue} can hold.
	 * 
	 */
	public int getLineLimit() {
		return lineLimit;
	}

	/**
	 * Set line number of the {@link DoubleQueue}.
	 * 
	 * @param	capacity
	 * 			Limit of the line number the {@link DoubleQueue} can hold.
	 * 
	 */
	public void setLineLimit(int capacity) {
		this.lineLimit = capacity;
	}

	/**
	 * Insert one line of record to a apace which buffers the swap data.
	 * 
	 * @param	line
	 * 			The inserted line.
	 * 
	 */
	private void insert(Line line) {
		writeArray[writeArrayTP] = line;
		++writeArrayTP;
		++writeCount;
		++lineRx;
		byteRx += line.length();
	}

	/**
	 * Insert a line array(appointed the limit of array size) of data to a apace which buffers the swap data.
	 * 
	 * @param lines
	 * 			Inserted line array.
	 * 
	 * @param size
	 * 			Limit of inserted size of the line array.
	 * 
	 */
	private void insert(Line[] lines, int size) {
		for (int i = 0; i < size; ++i) {
			writeArray[writeArrayTP] = lines[i];
			++writeArrayTP;
			++writeCount;
			++lineRx;
			byteRx += lines[i].length();
		}
	}

	/**
	 * Extract one line of record from the space which contains current data.
	 * 
	 * @return	line
	 * 			A line of data.
	 * 
	 */
	private Line extract() {
		Line e = readArray[readArrayHP];
		readArray[readArrayHP] = null;
		++readArrayHP;
		--readCount;
		++lineTx;
		return e;
	}


	/**
	 * Extract a line array of data from the space which contains current data.
	 * 
	 * @param ea
        * @return
	 * 			Extracted line number of data.
	 * 
	 */
	private int extract(Line[] ea) {
		int readsize = Math.min(ea.length, readCount);
		for (int i = 0; i < readsize; ++i) {
			ea[i] = readArray[readArrayHP];
			readArray[readArrayHP] = null;
			++readArrayHP;
			--readCount;
			++lineTx;
		}
		return readsize;
	}

	/**
	 * switch condition: read queue is empty && write queue is not empty.
	 * Notice:This function can only be invoked after readLock is grabbed,or may
	 * cause dead lock.
	 * 
	 * @param	timeout
	 * 
	 * @param	isInfinite
	 *          whether need to wait forever until some other thread awake it.
	 *          
	 * @return
	 * 
	 * @throws InterruptedException
	 * 
	 */

	private long queueSwitch(long timeout, boolean isInfinite)
			throws InterruptedException {
		writeLock.lock();
		try {
			if (writeCount <= 0) {
				if (closed) {
					return -2;
				}
				try {
					if (isInfinite && timeout <= 0) {
						awake.await();
						return -1;
					} else {
						return awake.awaitNanos(timeout);
					}
				} catch (InterruptedException ie) {
					awake.signal();
					throw ie;
				}
			} else {
				Line[] tmpArray = readArray;
				readArray = writeArray;
				writeArray = tmpArray;

				readCount = writeCount;
				readArrayHP = 0;
				readArrayTP = writeArrayTP;

				writeCount = 0;
				writeArrayHP = readArrayHP;
				writeArrayTP = 0;

				notFull.signal();
				// logger.debug("Queue switch successfully!");
				return -1;
			}
		} finally {
			writeLock.unlock();
		}
	}

	
	/**
	 * If exists write space, it will return true, and write one line to the space.
	 * otherwise, it will try to do that in a appointed time,when time is out if still failed, return false. 
	 * 
	 * @param	line
	 * 			a Line.
	 * 
	 * @param	timeout
	 * 			appointed limit time
	 * 
	 * @param	unit
	 * 			time unit
	 * 
	 * @return
	 * 			True if success,False if failed.
	 * 
	 */
	public boolean offer(Line line, long timeout, TimeUnit unit)
			throws InterruptedException {
		if (line == null) {
			throw new NullPointerException();
		}
		long nanoTime = unit.toNanos(timeout);
		writeLock.lockInterruptibly();
		try {
			for (;;) {
				if (writeCount < writeArray.length) {
					insert(line);
					if (writeCount == 1) {
						awake.signal();
					}
					return true;
				}

				// Time out
				if (nanoTime <= 0) {
					return false;
				}
				// keep waiting
				try {
					nanoTime = notFull.awaitNanos(nanoTime);
				} catch (InterruptedException ie) {
					notFull.signal();
					throw ie;
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * If exists write space, it will return true, and write a line array to the space.<br>
	 * otherwise, it will try to do that in a appointed time,when time out if still failed, return false. 
	 * 
	 * @param	lines
	 * 			line array contains lines of data
	 * 
	 * @param	size
	 * 			Line number needs to write to the space.
	 * 
	 * @param	timeout
	 * 			appointed limit time
	 * 
	 * @param	unit
	 * 			time unit
	 * 
	 * @return
	 * 			status of this operation, true or false.
	 * 
	 * @throws	InterruptedException
	 * 			if being interrupted during the try limit time.
	 * 
	 */
	public boolean offer(Line[] lines, int size, long timeout, TimeUnit unit)
			throws InterruptedException {
		if (lines == null) {
			throw new NullPointerException();
		}
		long nanoTime = unit.toNanos(timeout);
		writeLock.lockInterruptibly();
		try {
			for (;;) {
				if (writeCount + size <= writeArray.length) {
					insert(lines, size);
					if (writeCount >= spillSize) {
						awake.signalAll();
					}
					return true;
				}

				// Time out
				if (nanoTime <= 0) {
					return false;
				}
				// keep waiting
				try {
					nanoTime = notFull.awaitNanos(nanoTime);
				} catch (InterruptedException ie) {
					notFull.signal();
					throw ie;
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Close the synchronized lock and one inner state.
	 * 
	 */
	public void close() {
		writeLock.lock();
		try {
			closed = true;
			awake.signalAll();
		} finally {
			writeLock.unlock();
		}
	}

	
	/**
	 * 
	 * 
	 * @param	timeout
	 * 			appointed limit time
	 * 
	 * @param	unit
	 * 			time unit
	 */
	public Line poll(long timeout, TimeUnit unit) throws InterruptedException {
		long nanoTime = unit.toNanos(timeout);
		readLock.lockInterruptibly();

		try {
			for (;;) {
				if (readCount > 0) {
					return extract();
				}

				if (nanoTime <= 0) {
					return null;
				}
				nanoTime = queueSwitch(nanoTime, true);
			}
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 
	 * @param ea    line buffer
	 *
	 * 
	 * @param	timeout
	 * 			a appointed limit time
	 * 
	 * @param	unit
	 * 			a time unit
	 * 
	 * @return
	 * 			line number of data.if less or equal than 0, means fail.
	 * 
	 * @throws	InterruptedException
	 * 			if being interrupted during the try limit time.
	 */
	public int poll(Line[] ea, long timeout, TimeUnit unit)
			throws InterruptedException {
		long nanoTime = unit.toNanos(timeout);
		readLock.lockInterruptibly();

		try {
			for (;;) {
				if (readCount > 0) {
					return extract(ea);
				}

				if (nanoTime == -2) {
					return -1;
				}

				if (nanoTime <= 0) {
					return 0;
				}
				nanoTime = queueSwitch(nanoTime, false);
			}
		} finally {
			readLock.unlock();
		}
	}

	public Iterator<Line> iterator() {
		return null;
	}

	/**
	 * Get size of {@link Storage} in bytes.
	 * 
	 * @return
	 * 			Storage size.
	 * 
	 * */
	@Override
	public int size() {
		return (writeCount + readCount);
	}

	@Override
	public int drainTo(Collection<? super Line> c) {
		return 0;
	}

	@Override
	public int drainTo(Collection<? super Line> c, int maxElements) {
		return 0;
	}

	/**
	 * If exists write space, it will return true, and write one line to the space.<br>
	 * otherwise, it will try to do that in a appointed time(20 milliseconds),when time out if still failed, return false. 
	 * 
	 * @param	line
	 * 			a Line.
	 * 
	 * @see DoubleQueue#offer(Line, long, TimeUnit)
	 * 			
	 */
	@Override
	public boolean offer(Line line) {
		try {
			return offer(line, 20, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	@Override
	public void put(Line e) throws InterruptedException {
	}

	@Override
	public int remainingCapacity() {
		return 0;
	}

	@Override
	public Line take() throws InterruptedException {
		return null;
	}

	@Override
	public Line peek() {
		return null;
	}

	@Override
	public Line poll() {
		try {
			return poll(20, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
