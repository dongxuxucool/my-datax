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
import com.taobao.datax.common.plugin.PluginConst;

/**
 * Default implementation of {@link Line}.
 * 
 * */
public class DefaultLine implements Line {
	private String[] fieldList;

	private int length = 0;

	private int fieldNum = 0;

	/**
	 * Construct a line with at most PluginConst.LINE_MAX_FIELD fields.
	 * 
	 */
	public DefaultLine() {
		this.fieldList = new String[PluginConst.LINE_MAX_FIELD];
	}

	/**
	 * Clear 
	 */
	public void clear() {
		length = 0;
		fieldNum = 0;
	}

	/**
	 * Get length of all fields, exclude separate.
	 * 
	 * @return
	 *			length of all fields.
	 *
	 * */
	@Override
	public int length() {
		return length;
	}

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
	@Override
	public boolean addField(String field) {
		fieldList[fieldNum] = field;
		fieldNum++;
		if (field != null)
			length += field.length();
		return true;
	}

	/**
	 * 	
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
	@Override
	public boolean addField(String field, int index) {
		fieldList[index] = field;
		if (fieldNum < index + 1)
			fieldNum = index + 1;
		if (field != null)
			length += field.length();
		return true;
	}

	/**
	 * Get number of total fields in the {@link Line}.
	 * 
	 * @return
	 *			number of total fields in {@link Line}.
	 *
	 * */
	@Override
	public int getFieldNum() {
		return fieldNum;
	}

	/**
	 * Get one field of the {@link Line} indexed by the param.
	 * 
	 * @param	idx
	 * 			given position of the {@link Line}.
	 * 
	 * @return
	 *			field indexed by the param.
	 *
	 * */
	@Override
	public String getField(int idx) {
		return fieldList[idx];
	}
	
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
	public String checkAndGetField(int idx) {
		if (idx < 0 ||
				idx >= fieldNum) {
			return null;
		}
		return fieldList[idx];
	}

	/**
	 * Use param as separator of field, format the {@link Line} into {@link StringBuffer}.
	 * 
	 * @param	separator	
	 * 			field separate.
	 * 
	 * @return
	 * 			{@link Line} in {@link StringBuffer} style.
	 * 
	 * */
	@Override
	public StringBuffer toStringBuffer(char separator) {
		StringBuffer tmp = new StringBuffer();
		tmp.append(fieldNum);
		tmp.append(":\t");
		for (int i = 0; i < fieldNum; i++) {
			tmp.append(fieldList[i]).append(separator);
		}
		return tmp;
	}
	
	/**
	 * Use param as separator of field, translate the {@link Line} into {@link String}.
	 * 
	 * @param 	separator
	 * 			field separate.
	 * 
	 * @return
	 * 			{@link Line} in {@link String}.
	 * 
	 * */
	@Override
	public String toString(char separator) {
		return this.toStringBuffer(separator).toString();
	}

	/**
	 *  [empty implement]<br>
	 *  Use param(separator) as separator of field, split param(linestr) and construct {@link Line}.
	 *  
	 *  @param	lineStr
	 *			String will be translated into {@link Line}.
	 *  
	 *  @param 	separator
	 *			field separate.
	 *  
	 *  @return	
	 *  		{@link Line}
	 *  
	 * */
	@Override
	public Line fromString(String lineStr, char separator) {
		return null;
	}
}
