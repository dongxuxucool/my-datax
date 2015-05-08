/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.common.plugin;

import java.util.List;

/**
 * {@link PluginParam} represents some key-value params defined in job xml for {@link Pluginable}.
 * Use getXXX to get value from {@link PluginParam} specified by key </br>
 * 
 * */
public interface PluginParam extends Cloneable {
	
	/**
	 * Get value specified by key in String.
	 * if the key does not exiss in {@link PluginParam}, or value is empty String,<br>
	 * throw IllegalArgumentException.
	 * 
	 * @param	key
        *                   parameter key  
	 * 
	 * @return		
	 * 			Corresponding value in {@link PluginParam}.
	 * 				 	
	 * @throws	IllegalArgumentException
	 * 			if the key does not exists in {@link PluginParam}, or value is empty String.
	 *
	 * */
	public String getValue(String sKey);
	
	/**
	 * Get value specified by key .
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @param	defaultValue
	 * 			if the key does not exists in {@link PluginParam}, return defaultValue instead.
	 * 
	 * @return		
	 * 			Corresponding value in {@link PluginParam}, <br>
	 * 			if the key does not exists in {@link PluginParam}, or value is empty String, </br>
	 * 			return defaultValue instead.
	 * 
	 * */
	public String getValue(String key, String defaultValue);


	/**
	 * Get boolean value specified by key.
	 * if the key does not exists in {@link PluginParam}, value is empty, or value is not true/false, </br>
	 * throw IllegalArgumentException.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @return		
	 * 			Corresponding value in {@link PluginParam}.
	 * 
	 * @throws	IllegalArgumentException
	 * 			if the key does not exists in {@link PluginParam}, or value is empty String.
	 * 
	 */
	public boolean getBoolValue(String key);

	/**
	 * Get boolean value specified by key.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @param	defaultValue
	 * 			if the key does not exists in {@link PluginParam}, return defaultValue instead.
	 * 
	 * @return		
	 * 			corresponding value in {@link PluginParam} <br> 
	 * 			if the key does not exists in {@link PluginParam}, value is empty, or value is not true/false, return false instead.
	 * 
	 */
	public boolean getBoolValue(String key, boolean defaultValue);

	/**
	 * Get char value specified by key.
	 * if the key does not exists in {@link PluginParam}, value is empty, throw IllegalArgumentException.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @return		
	 * 			Corresponding value in {@link PluginParam}.
	 * 
	 * @throws	IllegalArgumentException
	 * 			if the key does not exists in {@link PluginParam}, or value is empty String.
	 */
	public char getCharValue(String key);

	/**
	 * Get char value specified by key.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @param	defaultValue
	 * 			if the key does not exists in {@link PluginParam}, return defaultValue instead.
	 * 
	 * @return		
	 * 			Corresponding value in {@link PluginParam}, if the key does not exists in {@link PluginParam}, value is empty, return defaultValue instead.
	 * 
	 */
	public char getCharValue(String key, char defaultValue);

	
	/**
	 * Get int value specified by key.
	 * if the key does not exists in {@link PluginParam}, value is empty, or value is not legal, throw IllegalArgumentException.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @return		
	 * 			corresponding value in {@link PluginParam}.
	 * 
	 * @throws	IllegalArgumentException
	 * 			if the key does not exists in {@link PluginParam}, or value is empty String.
	 * 
	 */
	public int getIntValue(String key);
	
	/**
	 * Get int value specified by key.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @param	defaultValue
	 * 			if the key does not exists in {@link PluginParam}, return defaultValue instead.
	 * 
	 * @return		
	 * 			corresponding value in {@link PluginParam},
	 *  		if the key does not exists in {@link PluginParam}, value is empty, or value is not legal, return defaultValue instead.
	 *  
	 */
	public int getIntValue(String key, int defaultValue);
	
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
	public int getIntValue(String key, int defaultValue,
			int min, int max);
	
	/**
	 * Get int value specified by key.
	 * if the key does not exists in {@link PluginParam}, or value is empty, or value is not legal double, throw IllegalArgumentException.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @return		
	 * 			corresponding value in {@link PluginParam}.
	 * 
	 * @throws	IllegalArgumentException
	 * 			if the key does not exists in {@link PluginParam}, or value is empty String.
	 * 
	 */
	public double getDoubleValue(String key);

	/**
	 * Get defaultValue value specified by key.
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @param	defaultValue
	 * 			if the key does not exists in {@link PluginParam}, return defaultValue instead.
	 * 
	 * @return		
	 * 			Corresponding value in {@link PluginParam}.<br>
	 * 			if the key does not exists in {@link PluginParam}, or value is empty, or value is not legal double, return defaultValue.
	 */
	public double getDoubleValue(String key, double defaultValue);

	
	/**
	 * set value 
	 * 
	 * @param	key
	 * 			parameter key.
	 * 
	 * @param	value
	 * 			parameter value.
	 * 
	 */
	public void putValue(String key, String value);

	/**
	 * Check if {@link PluginParam} contains this key.
	 * 
	 * @param	key
	 * 			key to be check.
	 * 
	 * @return		
	 * 			if key exists in {@link PluginParam}, return true, others returns false.
	 * 
	 * */
	public boolean hasKey(String key);
	
	/**
	 * Check if {@link PluginParam} has value specified by key <br>
	 * NOTE: if key is null, or key does not exist in this {@link PluginParam}, or value is empty, return false.
	 * 
	 * @param	key
	 * 			key to be check.
	 * 
	 * @return		
	 * 			if key exists in {@link PluginParam}, return true, others returns false.
	 * 
	 * */
	public boolean hasValue(String key);
	
	/**
	 * Get all keys exists in {@link PluginParam}.
	 * 
	 * @return		
	 * 			all keys exist in {@link PluginParam}.
	 * 
	 * */
	public List<String> getAllKeys();

	/**
	 * Merge current param to input param.
	 * 
	 * @param	param
	 * 			param to be merged.
	 * 
	 * */
	public void mergeTo(PluginParam param);

	
	/** Merge current param to input multiple params.
	 * 
	 * @param	param
	 * 			params to be merged.
	 * 
	 */
	public void mergeTo(List<PluginParam> param);

	/**
	 * Get information in String format.
	 * 
	 * @return		
	 * 			information in String.
	 * 
	 * */
	public String toString();

	/**
	 * Get current meta data.
	 * 
	 * @return
	 * 			get current meta data.
	 * 
	 * */
	public MetaData getMyMetaData();

	/**
	 * Register Current meta data.
	 * 
	 * @param	md
	 * 			current meta data to be registered.
	 * 
	 * */
	public void setMyMetaData(MetaData md);

	/**
	 * Get opposite meta data.
	 * 
	 * @return
	 * 			get opposite meta data.
	 * 
	 * */
	public MetaData getOppositeMetaData();

	/**
	 * Set opposite meta data.
	 * NOTE: {@link Reader} is opposite to a {@link Writer}.
	 * 
	 * @param	md
	 * 			opposite meta data to be registered.
	 * 
	 * */
	public void setOppositeMetaData(MetaData md);

	/**
	 * Get copy of current {@link PluginParam} 
	 */
	public PluginParam clone();
}
