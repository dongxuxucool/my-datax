package com.dxx.test;

import java.util.HashMap;
import java.util.Map;

import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.engine.plugin.DefaultPluginParam;
import com.taobao.datax.engine.plugin.LineExchanger;
import com.taobao.datax.engine.storage.RAMStorage;
import com.taobao.datax.engine.storage.Storage;
import com.taobao.datax.plugins.reader.mysqlreader.MysqlReader;

public class DataTranferTest {

	public static void main(String[] args) {
		
		MysqlReader reader = new MysqlReader();
		
		reader.secondInit();
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("", value)
//		PluginParam param = new DefaultPluginParam(params);
		reader.prepare(null);
		reader.connect();
		Storage storage = new RAMStorage();
		storage.init("0", 3000, 1000000, 50);
		LineExchanger exchanger = new LineExchanger(storage);
		int res = reader.startRead(exchanger);
		System.out.println("res = " + res);
	}

	
}
