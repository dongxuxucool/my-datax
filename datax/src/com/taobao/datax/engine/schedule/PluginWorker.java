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
import java.util.List;

import org.apache.log4j.Logger;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.exception.ExceptionTracker;
import com.taobao.datax.common.plugin.PluginMonitor;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.Pluginable;
import com.taobao.datax.engine.conf.PluginConf;

/**
 * {@link PluginWorker} represents executor of {@link Pluginable}.</br>
 * DataX {@link Engine} uses {@link PluginWorker} to load methods from each {@link Pluginable} by reflections </br>
 * and call each method later.
 * 
 * */
public abstract class PluginWorker {

	protected String pluginName;

	protected Method getParam;

	protected Method setParam;

	protected Method setMonitor;

	protected Method getMonitor;
	
	protected Method init;

	protected Method prepare;

	protected Method post;

	protected Method split;

	protected Method cleanup;

	protected Method regMyMetaData;

	protected Method getMyMetaData;

	protected Method setOppositeMetaData;

	protected Method getOppositeMetaData;

	protected Object myObject;

	protected static Class<?> myClass;

	protected int myIndex = 0;

	private Logger logger = Logger.getLogger(PluginWorker.class);

	/**
	 * Constructor
	 * 
	 * @param	pluginConf
	 * 			{@link PluginConf}
	 * 
	 * @param	myClass
	 * 			class of {@link Pluginable}
	 * 
	 * @throws	{@link DataExchangeException}
	 * 
	 * */
	public PluginWorker(PluginConf pluginConf, Class<?> myClass) {
		try {
			PluginWorker.myClass = myClass;
			this.pluginName = pluginConf.getName();
			this.myObject = myClass.newInstance();
			this.getParam = myClass.getMethod("getParam", new Class[] {});
			this.setParam = myClass.getMethod("setParam",
					new Class[] { Class.forName("com.taobao.datax.common.plugin.PluginParam") });

			this.setMonitor = myClass
					.getMethod("setMonitor", new Class[] { Class
							.forName("com.taobao.datax.common.plugin.PluginMonitor") });
			this.getMonitor = myClass.getMethod("getMonitor", new Class[] {});

			this.init = myClass.getMethod("init", 
					new Class[] {});
			
			this.split = myClass.getMethod("split",
					new Class[] { Class.forName("com.taobao.datax.common.plugin.PluginParam") });
			
			this.prepare = myClass.getMethod("prepare",
					new Class[] { Class.forName("com.taobao.datax.common.plugin.PluginParam") });

			this.post = myClass.getMethod("post",
					new Class[] { Class.forName("com.taobao.datax.common.plugin.PluginParam") });

			this.cleanup = myClass.getMethod("cleanup", new Class[] {});

			this.regMyMetaData = myClass.getMethod("setMyMetaData",
					new Class[] { Class.forName("com.taobao.datax.common.plugin.MetaData") });

			this.getMyMetaData = myClass.getMethod("getMyMetaData",
					new Class[] {});

			this.setOppositeMetaData = myClass.getMethod("setOppositeMetaData",
					new Class[] { Class.forName("com.taobao.datax.common.plugin.MetaData") });

			this.getOppositeMetaData = myClass.getMethod("getOppositeMetaData",
					new Class[] {});

		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e.getCause());
		}

	}

	/**
	 * DataX call this method to do init work
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * @throws	DataExchangeException
	 * 			for other exceptions
	 * 
	 * */
	public int init() {
		try {
			return (Integer) init.invoke(myObject);
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e.getCause());
		}
	}
	
	
	/**
	 * DataX call this method to do prepare work before read or write data
	 * 
	 * @param	oParam
	 * 			{@link PluginParam}
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * @throws	DataExchangeException
	 * 			for other exceptions
	 * 
	 * */
	public int prepare(PluginParam oParam) {
		try {
			return (Integer) prepare.invoke(myObject, new Object[]{oParam});
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
	}


	/**
	 * DataX call method post before read or write data
	 * 
	 * @param	oParam
	 * 			{@link PluginParam}
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * @throws	DataExchangeException
	 * 			for other exceptions
	 *
	 * */
	public int post(PluginParam oParam) {
		try {
			return (Integer) post.invoke(myObject, new Object[] { oParam });
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e.getCause());
		}
	}

	

	/**
	 * DataX call method prepare before read or write data
	 * 
	 * @param	oParam
	 * 			{@link PluginParam}
	 * 
	 * @return
	 *			a list of sub-job {@link PluginParam}
	 * 
	 * @throws	DataExchangeException
	 *
	 * */
	@SuppressWarnings("unchecked")
	public List<PluginParam> doSplit(PluginParam oParam) {
		try {
			return (List<PluginParam>) split.invoke(myObject,
					new Object[] { oParam });
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e.getCause());
		}
	}

	/**
	 * DataX call method cleandup when reading or writing data ends
	 * 
	 * @return
	 * 			0 for OK, others for failure.
	 * 
	 * @throws	DataExchangeException
	 * 
	 * */
	public int cleanup() {
		try {
			return (Integer) cleanup.invoke(myObject, new Object[] {});
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
	}

	
	/**
	 * Get {@link PluginParam}
	 * 
	 * @return
	 * 			@see {@link PluginParam}
	 * 
	 * @throws	DataExchangeException
	 * 			for other exceptions
	 * 
	 * */
	public PluginParam getParam() {
		try {
			return (PluginParam) getParam.invoke(myObject, new Object[] {});
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
	}

	/**
	 * Set {@link PluginParam}
	 * 
	 * @param	oParam
	 * 			@see {@link PluginParam}.
	 * 
	 * @throws	DataExchangeException
	 * 			for other exceptions
	 *
	 * */
	public void setParam(PluginParam oParam) {
		try {
			setParam.invoke(myObject, new Object[] { oParam });
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
	}

	/**
	 * Get {@link PluginMonitor}.
	 * 
	 * @return
	 * 			@see {@link PluginMonitor}.
	 * 
	 * @throws	DataExchangeException
	 * 			for other exceptions
	 *
	 * */
	public PluginMonitor getMonitor() {
		try {
			return (PluginMonitor) getMonitor.invoke(myObject, new Object[] {});
		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
	}

	

	/**
	 * Set {@link PluginMonitor}
	 * 
	 * @param	PluginMonitor
	 * 			@see {@link PluginMonitor}。
	 * 
	 * @throws	DataExchangeException
	 * 			for other exceptions
	 * 
	 * */
	public void setMonitor(PluginMonitor monitor)  {
		try {
            setMonitor.invoke(myObject, new Object[]{monitor});
		}  catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(e);
		}
	}

	/**
	 * Get name of plugin.
	 * 
	 * @return
	 * 			name of plugin.
	 * 
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * Set name of plugin.
	 * 
	 * @param	pluginName
	 * 			name of plugin.
	 * 
	 */
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	/**
	 * Get index of plugin.
	 * 
	 * @return
	 * 			index of plugin.
	 * 
	 */
	public int getMyIndex() {
		return myIndex;
	}

	/**
	 * Set index of plugin.
	 * 
	 * @param	myIndex
	 * 			index of plugin.
	 * 
	 */
	public void setMyIndex(int myIndex) {
		this.myIndex = myIndex;
	}

}
