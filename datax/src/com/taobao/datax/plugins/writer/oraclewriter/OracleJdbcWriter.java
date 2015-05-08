/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.plugins.writer.oraclewriter;
///*
// * (C) 2010-2011 Alibaba Group Holding Limited
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//
//package com.taobao.datax.plugins.writer.oraclewriter;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.log4j.Logger;
//
//import com.taobao.datax.plugins.writer.oraclewriter.OracleConnection;
//
//import com.taobao.datax.common.plugin.Writer;
//import com.taobao.datax.common.plugin.PluginParam;
//import com.taobao.datax.common.plugin.PluginStatus;
//import com.taobao.datax.common.plugin.LineReceiver;
//import com.taobao.datax.common.util.DumperHandler;
//
//public class OracleJdbcWriter extends Writer{
//	protected OracleConnection connection;
//
//	private Logger logger = Logger.getLogger(OracleWriter.class);
//	
//	@Override
//	public int init(){
//		return PluginStatus.SUCCESS.value();
//	}
//
//	private void setUrls(ArrayList<Map<String, String>> urls){
//		for(int i = 0; i < urls.size(); i ++){
//			connection.setUrl(urls.get(i).get("ip"),
//					urls.get(i).get("port"),
//					urls.get(i).get("sid"));
//		}
//	}
//	
//	@Override
//	public int connect(){
//		/*
//		if(connection == null){
//			connection = new OracleConnection();
//		}
//		int maxconnections;
//		int threadMax;
//		try{
//			OracleTns tns = new OracleTns(StringUtil.getStrParam(iParam.getParam(ParamsKey.OracleDumper.tnsfile)));
//			ArrayList<Map<String, String>> urls = tns.find(StringUtil.getStrParam(iParam.getParam(ParamsKey.OracleDumper.dbname)));
//			connection.setPassword(StringUtil.getStrParam(iParam.getParam(ParamsKey.OracleDumper.password)));
//			connection.setUsername(StringUtil.getStrParam(iParam.getParam(ParamsKey.OracleDumper.username)));
//			threadMax = StringUtil.getIntParam(iParam.getParam("threadMax"),1,1,99);
//			//maxconnections = StringUtil.getIntParam(iParam.getParam(ParamsKey.OracleReader.maxconnection),10,threadMax+1,100);
//			maxconnections = threadMax + 1;
//			setUrls(urls);
//		}catch (Exception e){
//			e.printStackTrace();
//			return FAILED;
//		}
//		
//		connection.setMaxConnectionNum(maxconnections);
//		connection.createDatabase();
//		//logger.debug(connection.getUrl());
//		*/ 
//		 
//		return PluginStatus.SUCCESS.value();
//	}
//
//	@Override
//	public int finish(){
//		if(connection!=null){
//			connection.close();
//			connection = null;
//		}
//		return PluginStatus.SUCCESS.value();
//	}
//	
//	@Override
//	public List<PluginParam> split(PluginParam param){
//		OracleWriterSplitter spliter = new OracleWriterSplitter();
//		spliter.setParam(param);
//		spliter.init();
//		return spliter.split();	
//	}
//
//	@Override
//	public int commit() {
//		return PluginStatus.SUCCESS.value();
//	}
//
//	@Override
//	public int startWrite(LineReceiver resultHandler){
//		if(this.connection == null){
//			logger.error("connection is null!");
//			return PluginStatus.FAILURE.value();
//		}
//		
//		DumperHandler hander = new DumperHandler(resultHandler, getMonitor(), getParam());
//		hander.init();
//		hander.handle(this.connection);
//		getMonitor().setStatus(PluginStatus.WRITE_OVER);
//		return PluginStatus.SUCCESS.value();
//	}
//}
