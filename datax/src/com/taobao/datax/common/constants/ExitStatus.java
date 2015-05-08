/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.common.constants;

/**
 * Exit code for DataX program。
 * 
 * */
public enum ExitStatus {
	FAILED(2), SUCCESSFUL(0);

	private int status;

	private ExitStatus(int status) {
		this.status = status;
	}

	public int value() {
		return status;
	}

}