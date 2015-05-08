/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.engine.conf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.taobao.datax.common.constants.Constants;
import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.util.StrUtils;
import com.taobao.datax.engine.plugin.DefaultPluginParam;

/**
 * A utility tool to parse configure XML file.
 * 
 */
public class ParseXMLUtil {
	private static final Logger LOG = Logger.getLogger(ParseXMLUtil.class);

	/**
	 * Parse dataX job configuration file.
	 * 
	 * @param filename
	 *            Job configure filename.
	 * 
	 * @return a JobConf instance which describes this Job configuration file.
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public static JobConf loadJobConfig(String filename) {
		JobConf job = new JobConf();
		Document document;
		try {
			String xml = FileUtils
					.readFileToString(new File(filename), "UTF-8");
			document = DocumentHelper.parseText(xml);
		} catch (DocumentException e) {
			LOG.error("DataX Can not find " + filename + " .");
			throw new DataExchangeException(e.getCause());
		} catch (IOException e) {
			LOG.error(String.format("DataX read config file %s failed .",
					filename));
			throw new DataExchangeException(e.getCause());
		}

		String xpath = "/jobs/job";
		Element jobE = (Element) document.selectSingleNode(xpath);
		job.setId(jobE.attributeValue("id") == null ? 
				"DataX_is_still_a_virgin" : jobE.attributeValue("id").trim());

		JobPluginConf readerJobConf = new JobPluginConf();
		Element readerE = (Element) document
				.selectSingleNode(xpath + "/loader");
		if (null == readerE)
			readerE = (Element) document.selectSingleNode(xpath + "/reader");

		String readerId = readerE.attributeValue("id");
		readerJobConf.setId(readerId == null ? "virgin-reader" : readerId
				.trim());
		Element readerPluinE = (Element) readerE.selectSingleNode("plugin");
		readerJobConf.setName(readerPluinE.getStringValue().trim()
				.replace("loader", "reader").toLowerCase());

		Map<String, String> readerParamMap = new HashMap<String, String>();

		/*
		 * for historic reason, we need to check concurrency node first add by
		 * bazhen.csy
		 */
		if (readerE.selectSingleNode("concurrency") != null) {
			Element readerConcurrencyE = (Element) readerE
					.selectSingleNode("concurrency");
			readerParamMap.put("concurrency", StrUtils
					.replaceString(readerConcurrencyE.attributeValue("core")));
		}

		List<Element> readerParamE = (List<Element>) readerE
				.selectNodes("param");
		for (Element e : readerParamE) {
			readerParamMap.put(e.attributeValue("key").toLowerCase(),
					StrUtils.replaceString(e.attributeValue("value").trim()));
		}

		PluginParam readerPluginParam = new DefaultPluginParam(readerParamMap);
		
//		if (!readerPluginParam.hasValue("concurrency")
//				|| readerPluginParam.getIntValue("concurrency", 1) < 0) {
//			throw new IllegalArgumentException(
//					"Reader option [concurrency] error !");
//		}

		readerJobConf.setPluginParams(readerPluginParam);

		List<JobPluginConf> writerJobConfs = new ArrayList<JobPluginConf>();
		List<Element> writerEs = (List<Element>) document.selectNodes(xpath
				+ "/dumper");
		if (null == writerEs || 0 == writerEs.size())
			writerEs = (List<Element>) document.selectNodes(xpath + "/writer");

		for (Element writerE : writerEs) {
			JobPluginConf writerPluginConf = new JobPluginConf();

			String writerId = writerE.attributeValue("id");
			writerPluginConf.setId(writerId == null ? "virgin-writer"
					: writerId.trim());

			String destructLimit = writerE.attributeValue("destructlimit");
			if (destructLimit != null) {
				writerPluginConf.setDestructLimit(Integer
						.valueOf(destructLimit));
			}

			Element writerPluginE = (Element) writerE
					.selectSingleNode("plugin");
			writerPluginConf.setName(writerPluginE.getStringValue().trim()
					.replace("dumper", "writer").toLowerCase());

			Map<String, String> writerParamMap = new HashMap<String, String>();

			/*
			 * for historic reason, we need to check concurrency node add by
			 * bazhen.csy
			 */
			if (writerE.selectSingleNode("concurrency") != null) {
				Element writerConcurrencyE = (Element) writerE
						.selectSingleNode("concurrency");
				writerParamMap.put("concurrency", StrUtils
						.replaceString(writerConcurrencyE
								.attributeValue("core")));
			}

			List<Element> writerParamE = (List<Element>) writerE
					.selectNodes("param");
			for (Element e : writerParamE) {
				writerParamMap
						.put(e.attributeValue("key").toLowerCase(),
								StrUtils.replaceString(e
										.attributeValue("value").trim()));
			}

			PluginParam writerPluginParam = new DefaultPluginParam(
					writerParamMap);

			writerPluginConf.setPluginParams(writerPluginParam);
			writerJobConfs.add(writerPluginConf);
		}

		job.setReaderConf(readerJobConf);
		job.setWriterConfs(writerJobConfs);

		return job;
	}

	/**
	 * Parse plugins configuration file, get all plugin configurations。
	 * 
	 * @return a map mapping plugin name to plugin configurations。
	 * */
	@SuppressWarnings("unchecked")
	public static Map<String, PluginConf> loadPluginConfig() {
		Map<String, PluginConf> plugins = new HashMap<String, PluginConf>();
		File file = new File(Constants.PLUGINSXML);
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			document = saxReader.read(file);
		} catch (DocumentException e) {
			LOG.error("DataX Can not find " + Constants.PLUGINSXML);
            throw new DataExchangeException(e.getCause());
		}
		String xpath = "/plugins/plugin";
		List<Node> pluginnode = (List<Node>) document.selectNodes(xpath);
		for (Node node : pluginnode) {
			PluginConf plugin = new PluginConf();
			plugin.setVersion(node.selectSingleNode("./version")
					.getStringValue());
			plugin.setName(node.selectSingleNode("./name").getStringValue());
			plugin.setTarget(node.selectSingleNode("./target").getStringValue());
			plugin.setJar(node.selectSingleNode("./jar").getStringValue());
			plugin.setType(node.selectSingleNode("./type").getStringValue());
			plugin.setClassName(node.selectSingleNode("./class")
					.getStringValue());
			plugin.setMaxthreadnum(Integer.parseInt(node.selectSingleNode(
					"./maxthreadnum").getStringValue()));
			if (node.selectSingleNode("./path") != null)
				plugin.setPath(node.selectSingleNode("./path").getStringValue());
			plugins.put(plugin.getName(), plugin);
		}
		return plugins;
	}

	/**
	 * Parse engine configuration file。
	 * 
	 * @return {@link EngineConf}.
	 * 
	 * */
	public static EngineConf loadEngineConfig() {

		EngineConf engineConf = EngineConf.getInstance();
		File file = new File(Constants.ENGINEXML);
		SAXReader saxReader = new SAXReader();
		Document document = null;

		try {
			document = saxReader.read(file);
		} catch (DocumentException e) {
			LOG.error("DataX Can not find " + Constants.ENGINEXML
					+ " , program exits .");
			throw new DataExchangeException(e);
		}

		engineConf.setVersion(Integer.parseInt(document.selectSingleNode(
				"/engine/version").getStringValue()));

		engineConf.setStorageClassName(document.selectSingleNode(
				"/engine/storage/class").getStringValue());
		engineConf.setStorageLineLimit(StrUtils.getIntParam(
				document.selectSingleNode("/engine/storage/linelimit")
						.getStringValue(), 10000, 100, 1000000));
		engineConf.setStorageByteLimit(StrUtils.getIntParam(
				document.selectSingleNode("/engine/storage/bytelimit")
						.getStringValue(), 1000000, 10000, 100000000));
		engineConf.setStorageBufferSize(StrUtils.getIntParam(document
				.selectSingleNode("/engine/storage/buffersize")
				.getStringValue(), 10, 1, 500));

		engineConf.setPluginRootPath(document.selectSingleNode(
				"/engine/pluginrootpath").getStringValue());

		return engineConf;
	}

	/**
	 * Parse engine configuration file, and get DataX Statistics report url.
	 * NOTE: This feature can be abandon.
	 * 
	 * @return DataX statistics report URL
	 * 
	 * */
	public static String loadScheduleURL() {
		String url = "";
		File file = new File(Constants.ENGINEXML);
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			document = saxReader.read(file);
			url = document.selectSingleNode("/engine/schedule/url")
					.getStringValue();
		} catch (DocumentException e) {
			LOG.error("Can not find " + Constants.ENGINEXML
					+ ", please check it again.");
            throw new DataExchangeException(e.getCause());
		}
		return url.trim();
	}
}
