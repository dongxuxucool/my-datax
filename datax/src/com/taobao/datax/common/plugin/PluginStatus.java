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
 * State of {@link Pluginable}.
 * 
 * */
public enum PluginStatus {
	FAILURE(-1),
	SUCCESS(0),
	CONNECT(1),
	READ(2),
	READ_OVER(3),
	WRITE(4),
	WRITE_OVER(5),	
	WAITING(6);
	
	private int status;

	public int value() {
		return status;
	}

	private PluginStatus(int status) {
		this.status = status;
	}
	
	public static void main(String[] args) {
		System.out.println(PluginStatus.FAILURE);
		System.out.println(PluginStatus.FAILURE.value());
	}
}
