/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.schedule;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.taobao.datax.common.constants.ExitStatus;
import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.exception.ExceptionTracker;
import com.taobao.datax.common.plugin.LineSender;
import com.taobao.datax.common.plugin.Pluginable;
import com.taobao.datax.common.plugin.Reader;
import com.taobao.datax.engine.conf.PluginConf;

/**
 * {@link MyReaderWorker} represents executor of {@link Reader} </br>
 * {@link Engine} use {@link MyReaderWorker} to read data.</br>
 * 
 * */
public class MyReaderWorker extends PluginWorker implements Runnable {
	private LineSender sender;

	private Method connect;

	private Method startRead;

	private Method finish;

	private static int globalIndex = 0;

	private static final Logger logger = Logger.getLogger(MyReaderWorker.class);
	
	/**
	 * Constructor for {@link MyReaderWorker}.
	 * 
	 * @param	pluginConf
	 * 			{@link PluginConf}
	 * 
	 * @param	myClass
	 * 			class of {@link Pluginable}
	 * 
	 * @throws		{@link DataExchangeException}
	 * 
	 * */
	public MyReaderWorker(PluginConf pluginConf, Class<?> myClass) {
		super(pluginConf, myClass);
		try {
			connect = myClass.getMethod("connect", new Class[] {});
			startRead = myClass
					.getMethod("startRead", new Class[] { Class
							.forName("com.taobao.datax.common.plugin.LineSender") });
			finish = myClass.getMethod("finish", new Class[] {});
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
		this.setMyIndex(globalIndex++);
	}


	/**
	 * Read data, main execute logic code of {@link Reader} <br>
	 * NOTE: When catches exception, {@link MyReaderWorker} exit process immediately.
	 * 
	 * */
	@Override
	public void run() {
		try {
			int iRetcode = (Integer) init.invoke(myObject, new Object[] {});
			if (iRetcode != 0) {
				logger.error("Reader initialize failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) connect.invoke(myObject,
					new Object[] {});
			if (iRetcode != 0) {
				logger.error("Reader connect to DB failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) startRead.invoke(myObject,
					new Object[] { this.sender });
			if (iRetcode != 0) {
				logger.error("Reader startRead failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
			iRetcode = (Integer) finish.invoke(myObject, new Object[] {});
			if (iRetcode != 0) {
				logger.error("Reader finish loading failed .");
				System.exit(ExitStatus.FAILED.value());
				return;
			}
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			System.exit(ExitStatus.FAILED.value());
		}
	}
	
	public void setLineSender(LineSender sender) {
		this.sender = sender;
	}

}
