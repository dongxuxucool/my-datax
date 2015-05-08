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
 *  Represents one record of data which may be in database, HDFS, etc.
 *  {@link Reader} and {@link Writer} exchange data with each other in the way of Line
 *  (Usually exchange multiple lines every time).
 *  
 * */
public interface Line {
	
	/**
	 * Add a field into the {@link Line}.
	 * 
	 * @param	field	
	 * 			Field added into {@link Line}.
	 * 
	 * @return 
	 * 			true for OK, false for failure.
	 * 
	 * */
	public boolean addField(String field);
	
	/**
	 * Add a field into the {@link Line}.
	 * 
	 * @param	field	
	 * 			field added into {@link Line}.
	 * 
	 * @param 	index	
	 * 			given position of field in the {@link Line}.
	 * 
	 * @return 
	 *			true for OK, false for failure.
	 *
	 * */
	public boolean addField(String field, int index);
	
	/**
	 * Get one field of the {@link Line} indexed by the param.
	 * 
	 * NOTE:
	 * if index specifed by user beyond field number of {@link Line}
	 * it may throw runtime excepiton
	 * 
	 * 
	 * @param	 idx
	 * 			given position of the {@link Line}.
	 * 
	 * @return
	 *			field indexed by the param.
	 *
	 * */
	public String getField(int idx);
	
	/**
	 * Get one field of the {@link Line} indexed by the param.
	 * if idx specified by user beyond field number of {@link Line}
	 * null will be returned
	 * 
	 * @param	 idx
	 * 			given position of the {@link Line}.
	 * 
	 * @return
	 *			field indexed by the param.
	 *
	 * */
	public String checkAndGetField(int idx);
	
	/**
	 * Get number of total fields in the {@link Line}.
	 * 
	 * @return
	 *			number of total fields in {@link Line}.
	 *
	 * */
	public int getFieldNum();
	
	/**
	 * Use param as separator of field, format the {@link Line} into {@link StringBuffer}.
	 * 
	 * @param	separator	
	 * 			field separator.
	 * 
	 * @return
	 * 			{@link Line} in {@link StringBuffer} style.
	 * 
	 * */
	public StringBuffer toStringBuffer(char separator);
	
	/**
	 * Use param as separator of field, translate the {@link Line} into {@link String}.
	 * 
	 * @param 	separator
	 * 			field separator.
	 * 
	 * @return
	 * 			{@link Line} in {@link String}.
	 * 
	 * */
	public String toString(char separator);
	
	/**
	 *  Use param(separator) as separator of field, split param(linestr) and construct a {@link Line}.
	 *  
	 *  @param 	lineStr
	 *				String will be translated into {@link Line}.
	 *  
	 *  @param 	separator
	 *				field separate.
	 *  
	 *  @return	
	 *  			{@link Line}
	 *  
	 * */
	public Line fromString(String lineStr, char separator);
	
	/**
	 * Get length of all fields, exclude separate.
	 * 
	 * @return
	 *			length of all fields.
	 *
	 * */
	public int length();
	
}
