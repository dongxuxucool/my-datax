/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.storage;

import com.taobao.datax.common.exception.DataExchangeException;

/**
 * As its name, This class produce storage space.
 * 
 */
public class StorageFactory {
	private StorageFactory(){
	}
	
	/**
	 * Produce storage space according to the given classname.
	 * 
	 * @param	className
	 * 			Full Name of A concrete storage. 
	 * @return 
	 * 			If nothing wrong, return a storage, else return null.
	 * 
	 */
	public static Storage product(String className) {
		try {
			return (Storage) Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new DataExchangeException(e.getCause());
		}
	}
}
