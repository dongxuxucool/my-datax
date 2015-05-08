/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */


package com.taobao.datax.engine.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.taobao.datax.engine.conf.JobConf;
import com.taobao.datax.engine.conf.JobPluginConf;
import com.taobao.datax.engine.conf.ParseXMLUtil;


public class Reporter {
	private static final Logger LOG = Logger.getLogger(Reporter.class);

	private static final String LOG_URL = "{0}?{1}";

	public static Map<String, String> stat = new HashMap<String, String>();
	
	public static Reporter instance() {
		return new Reporter();
	}
	
	private Reporter() {
	}

	public String parseJobConf(JobConf jobConf) {
		StringBuilder log = new StringBuilder();
		log.append("ID=").append(jobConf.getId());
		JobPluginConf readerConf = jobConf.getReaderConf();
		log.append("&SOURCE_TYPE=").append(readerConf.getName());
		for (String key : readerConf.getPluginParams().getAllKeys()) {
			key = key.toLowerCase();
			if ("dbname".indexOf(key) != -1) {
				log.append("&SOURCE_DB=").append(
						readerConf.getPluginParams().getValue(key));
			} else if ("tables".indexOf(key) != -1) {
				log.append("&SOURCE_TABLE=").append(
						readerConf.getPluginParams().getValue(key));
			} else if ("dir".indexOf(key) != -1) {
				log.append("&SOURCE_DB=").append("HDFS");
				log.append("&SOURCE_TABLE=").append("HDFS");
			}
		}
		JobPluginConf writerConf = jobConf.getWriterConfs().get(0);
		log.append("&SINK_TYPE=").append(writerConf.getName());
		for (String key : writerConf.getPluginParams().getAllKeys()) {
			key = key.toLowerCase();
			if ("dbname".indexOf(key) != -1) {
				log.append("&SINK_DB=").append(
						writerConf.getPluginParams().getValue(key));
			} else if ("table".indexOf(key) != -1) {
				log.append("&SINK_TABLE=").append(
						writerConf.getPluginParams().getValue(key));
			} else if ("dir".indexOf(key) != -1) {
				log.append("&SINK_DB=").append("HDFS");
				log.append("&SINK_TABLE=").append("HDFS");
			}
		}
		log.append("&READ_RECORDS").append("=")
				.append(stat.get("READ_RECORDS"));
		log.append("&WRITE_RECORDS").append("=")
				.append(stat.get("WRITE_RECORDS"));
		log.append("&DISCARD_RECORDS").append("=")
				.append(stat.get("DISCARD_RECORDS"));
		log.append("&BEGINE_TIME").append("=").append(stat.get("BEGIN_TIME"));
		log.append("&END_TIME").append("=").append(stat.get("END_TIME"));

		long readRe = Long.valueOf(stat.get("READ_RECORDS"));
		long disRe = Long.valueOf(stat.get("DISCARD_RECORDS"));
		long writeRe = readRe - disRe;
		long byteAvg = 0;

		if (readRe != 0)
			byteAvg = Long.valueOf(stat.get("BYTE_RX_TOTAL")) / readRe;

		log.append("&READ_BYTES=").append(readRe * byteAvg);
		log.append("&WRITE_BYTES=").append(writeRe * byteAvg - disRe);
		log.append("&DISCARD_BYTES=").append(disRe * byteAvg);

		return log.toString().trim();
	}

	public void report(JobConf jobConf) {
		URL url;
		try {
			String requestURL = java.text.MessageFormat.format(LOG_URL,
					ParseXMLUtil.loadScheduleURL(), parseJobConf(jobConf));
			url = new URL(requestURL.replace(" ", "%20"));// replace space in
															// URL
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(1000 * 5);// 5 seconds connection time out
			connection.setReadTimeout(1000 * 5);// 10 seconds read time out
			try {
				connection.setDoOutput(true);// DEFAULT FALSE
				InputStream in = connection.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(in));
				String rsStatus = bufferedReader.readLine();
				if (!"0".equals(rsStatus)) {
					LOG.info("Report DataX statistic failed .");
					return;
				}
				LOG.info("Report DataX statistic successfully .");
			} catch (Exception ex) {
				LOG.info("Report DataX statistic failed, " + ex.getMessage());
			} finally {
				connection.disconnect();
			}
		} catch (MalformedURLException e) {
			LOG.info("Report DataX statistic failed: " + e.getMessage());
		} catch (IOException e) {
			LOG.info("Report DataX statistic failed: " + e.getMessage());
		} catch (Exception e) {
			LOG.info("Report DataX statistic failed: " + e.getMessage());
		}
	}
}
