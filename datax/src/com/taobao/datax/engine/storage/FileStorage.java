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

public class FileStorage extends Storage {

	@Override
	public boolean empty() {
		return false;
	}

	@Override
	public boolean init(String id, int lineLimit, int byteLimit, int destructLimit) {
		return false;
	}

	@Override
	public boolean isPushClosed() {
		return false;
	}

	@Override
	public Line pull() {
		return null;
	}

	@Override
	public boolean push(Line line) {
		return false;
	}

	
	@Override
	public void setPushClosed(boolean close) {
		
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public int getLineLimit() {
		return 0;
	}

	@Override
	public boolean fakePush(int lineLength) {
		return false;
	}

	@Override
	public boolean push(Line[] lines, int size) {
		return false;
	}

	@Override
	public int pull(Line[] lines) {
		return 0;
	}

	@Override
	public String info() {
		return null;
	}

}
