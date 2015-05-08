/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.engine.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.taobao.datax.common.plugin.MetaData;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.Reader;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.common.util.StrUtils;

/**
 * Default implementation to {@link PluginParam}.
 * 
 * */
public class DefaultPluginParam implements PluginParam {
	private static final Logger LOG = Logger
			.getLogger(DefaultPluginParam.class);

	private final String KEY_NOT_EXISTS = "Job Configuration Option [ %s ] is missing .";

	private Map<String, String> params;

	private MetaData myMd;

	private MetaData oppositeMd;

	public DefaultPluginParam(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * Check if {@link PluginParam} has value specified by key <br>
	 * NOTE: if key is null, or key does not exists in this {@link PluginParam},
	 * or value is empty, return <code>false</code>
	 * 
	 * @param key
	 *            key to be check.
	 * 
	 * @return if key exists in {@link PluginParam}, return <code>true</code>,
	 *         others returns <code>false</code>.
	 * */
	public boolean hasValue(String key) {
		if (null == key) {
			return false;
		}
		key = key.toLowerCase();
		if (!params.containsKey(key)) {
			return false;
		}
		String value = params.get(key);
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return true;
	}

	/**
	 * set value
	 * 
	 * @param key
	 *            parameter key.
	 * 
	 * @param value
	 *            parameter value.
	 */
	@Override
	public void putValue(String key, String value) {
		this.params.put(key.toLowerCase(), value);
	}

	/**
	 * Get value specified by key in String. if the key does not exists in
	 * {@link PluginParam}, or value is empty String,<br>
	 * throw IllegalArgumentException.
	 * 
	 * @param key
	    *
	 * @return Corresponding value in {@link PluginParam}.
	 * 
	 * */
	@Override
	public String getValue(String key) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (hasValue(key)) {
			return StrUtils.replaceString(this.params.get(key));
		}
		throw new IllegalArgumentException(String.format(KEY_NOT_EXISTS, key));
	}

	/**
	 * Get value specified by key .
	 * 
	 * @param key
	 *            parameter key.
	 * 
	 * @param defaultValue
	 *            if the key does not exists in {@link PluginParam}, return
	 *            defaultValue instead.
	 * 
	 * @return Corresponding value in {@link PluginParam}, <br>
	 *         if the key does not exists in {@link PluginParam}, or value is
	 *         empty String, </br> return defaultValue instead.
	 * */
	@Override
	public String getValue(String key, String defaultValue) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			return defaultValue;
		}
		return StrUtils.replaceString(this.params.get(key));
	}

	/**
	 * Get char value specified by key. if the key does not exists in
	 * {@link PluginParam}, value is empty, throw IllegalArgumentException.
	 * 
	 * @param key
	 *            parameter key
	 * 
	 * @return Corresponding value in {@link PluginParam}
	 */
	@Override
	public char getCharValue(String key) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			throw new IllegalArgumentException(String.format(KEY_NOT_EXISTS,
					key));
		}
		return StrUtils
				.changeChar(StrUtils.replaceString(this.params.get(key)));
	}

	/**
	 * Get char value specified by key.
	 * 
	 * @param key
	 *            parameter key.
	 * 
	 * @param defaultValue
	 *            if the key does not exists in {@link PluginParam}, return
	 *            defaultValue instead.
	 * 
	 * @return Corresponding value in {@link PluginParam}, if the key does not
	 *         exists in {@link PluginParam}, value is empty, return
	 *         defaultValue instead.
	 */
	@Override
	public char getCharValue(String key, char defaultValue) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			return defaultValue;
		}
		return getCharValue(key);
	}

	/**
	 * Get boolean value specified by key. if the key does not exists in
	 * {@link PluginParam}, value is empty, or value is not true/false, </br>
	 * throw IllegalArgumentException.
	 * 
	 * @param key
	 *            parameter key
	 * 
	 * @return Corresponding value in {@link PluginParam}
	 */
	@Override
	public boolean getBoolValue(String key) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			throw new IllegalArgumentException(String.format(KEY_NOT_EXISTS,
					key));
		}
		return Boolean.valueOf(StrUtils.replaceString(this.params.get(key)));
	}

	/**
	 * Get boolean value specified by key.
	 * 
	 * @param key
	 *            parameter key
	 * 
	 * @param defaultValue
	 *            if the key does not exists in {@link PluginParam}, return
	 *            defaultValue instead.
	 * 
	 * @return Corresponding value in {@link PluginParam} <br>
	 *         if the key does not exists in {@link PluginParam}, value is
	 *         empty, or value is not true/false, return <code>false</code>
	 *         instead.
	 */
	@Override
	public boolean getBoolValue(String key, boolean defaultValue) {
		if (null != key)
			key = key.toLowerCase().trim();

		if (!hasValue(key))
			return defaultValue;

		return getBoolValue(key);
	}

	/**
	 * Get int value specified by key. if the key does not exists in
	 * {@link PluginParam}, value is empty, or value is not legal, throw
	 * IllegalArgumentException.
	 * 
	 * @param key
	 *            parameter key
	 * 
	 * @return Corresponding value in {@link PluginParam}
	 * 
	 */
	@Override
	public int getIntValue(String key) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			throw new IllegalArgumentException(String.format(KEY_NOT_EXISTS,
					key));
		}
		return Integer.valueOf(StrUtils.replaceString(this.params.get(key)));
	}

	/**
	 * Get int value specified by key.
	 * 
	 * @param key
	 *            parameter key
	 * 
	 * @param defaultValue
	 *            if the key does not exists in {@link PluginParam}, return
	 *            defaultValue instead.
	 * 
	 * @return Corresponding value in {@link PluginParam}, if the key does not
	 *         exists in {@link PluginParam}, value is empty, or value is not
	 *         legal, return defaultValue instead.
	 */
	@Override
	public int getIntValue(String key, int defaultValue) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			return defaultValue;
		}
		return getIntValue(key);
	}

	/**
	 * Get int value specified by key.
	 * 
	 * @param		key
	 * 					parameters key
	 * 
	 * @param		defaultValue
	 * 					if the key does not exists in {@link PluginParam}, return defaultValue instead.
	 * 
	 * @param		min
	 * 					check value is bigger than min value
	 * 
	 * @param		max
	 * 					check value is smaller than max value
	 * 
	 * @return		
	 * 					corresponding value in {@link PluginParam}
	 *					if the key does not exists in {@link PluginParam}, value is empty, or value is not legal, return defaultValue instead.
	 *  
	 * @throws	
	 * 					IllegalArgumentException
	 * 					if value < min or value > max
	 * 
	 * */
	@Override
	public int getIntValue(String key, int defaultValue, int min, int max) {
		int value = getIntValue(key, defaultValue);
		
		if (value < min || value > max) {
			throw new IllegalArgumentException(String.format(
					"%s's value is %d, must be in [%d, %d]", key, value, min,
					max));
		}
		
		return value;
	}
	
	/**
	 * Get int value specified by key. if the key does not exists in
	 * {@link PluginParam}, or value is empty, or value is not legal double,
	 * throw IllegalArgumentException.
	 * 
	 * @param key
	 *            parameter key.
	 * 
	 * @return Corresponding value in {@link PluginParam}.
	 * 
	 */
	@Override
	public double getDoubleValue(String key) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			throw new IllegalArgumentException(String.format(KEY_NOT_EXISTS,
					key));
		}
		return Double.valueOf(StrUtils.replaceString(this.params.get(key)));
	}

	/**
	 * Get defaultValue value specified by key.
	 * 
	 * @param key
	 *            parameter key.
	 * 
	 * @param defaultValue
	 *            if the key does not exists in {@link PluginParam}, return
	 *            defaultValue instead.
	 * 
	 * @return Corresponding value in {@link PluginParam} if the key does not
	 *         exists in {@link PluginParam}, or value is empty, or value is not
	 *         legal double, return defaultValue.
	 */
	@Override
	public double getDoubleValue(String key, double defaultValue) {
		if (null != key) {
			key = key.toLowerCase().trim();
		}
		if (!hasValue(key)) {
			return defaultValue;
		}
		return getDoubleValue(key);
	}

	/**
	 * Get all keys exists in {@link PluginParam}.
	 * 
	 * @return all keys exists in {@link PluginParam}.
	 * */
	@Override
	public List<String> getAllKeys() {
		Iterator<String> it = params.keySet().iterator();
		ArrayList<String> listKey = new ArrayList<String>();
		while (it.hasNext()) {
			String key = it.next();
			listKey.add(key);
		}
		return listKey;
	}

	/**
	 * Merge current param to input param.
	 * 
	 * @param param
	 *            param to be merged.
	 * 
	 * */
	public void mergeTo(PluginParam param) {
		List<String> keys = this.getAllKeys();
		for (String k : keys) {
			if (!param.hasKey(k))
				param.putValue(k, this.getValue(k));
		}
	}

	/**
	 * Merge current param to input multiple params.
	 * 
	 * @param param
	 *            params to be merged.
	 */
	@Override
	public void mergeTo(List<PluginParam> list) {
		for (PluginParam p : list) {
			mergeTo(p);
		}
	}

	/**
	 * Check if {@link PluginParam} contains this key.
	 * 
	 * @param key
	 *            key to be check.
	 * 
	 * @return if key exists in {@link PluginParam}, return <code>true</code>,
	 *         others returns <code>false</code>.
	 * */
	@Override
	public boolean hasKey(String key) {
		return this.params.containsKey(key);
	}

	/**
	 * Get information in String format .
	 * 
	 * @return Information in String.
	 * */
	@Override
	public String toString() {
		String s = "";
		for (String key : this.params.keySet()) {
			String value = this.getValue(key, "");
			if (key.equalsIgnoreCase("password")) {
				value = value.replaceAll(".", "*");
			}
			s += String.format("\n\t%25s=[%-30s]", key, value);
		}
		return s;
	}

	/**
	 * Get current meta data .
	 * 
	 * @return Get current meta data .
	 * */
	@Override
	public MetaData getMyMetaData() {
		return this.myMd;
	}

	/**
	 * Register Current meta data .
	 * 
	 * @param md
	 *            Current meta data to be registered
	 * 
	 * */
	@Override
	public void setMyMetaData(MetaData md) {
		this.myMd = md;
	}

	/**
	 * Get opposite meta data .
	 * 
	 * @return Get opposite meta data .
	 * */
	@Override
	public MetaData getOppositeMetaData() {
		return this.oppositeMd;
	}

	/**
	 * Set opposite meta data . NOTE: {@link Reader} is opposite to a
	 * {@link Writer}
	 * 
	 * @param md
	 *            Opposite meta data to be registered.
	 * */
	@Override
	public void setOppositeMetaData(MetaData md) {
		this.oppositeMd = md;
	}

	/**
	 * get the clone of current {@link PluginParam} 
	 * 
	 */
	@Override
	public PluginParam clone() {
		List<String> keyList = this.getAllKeys();
		PluginParam oParam = new DefaultPluginParam(
				new HashMap<String, String>());
		for (String key : keyList) {
			if (hasValue(key))
				oParam.putValue(key, this.getValue(key));
		}
		return oParam;
	}
}
