/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.engine.tools;

import com.taobao.datax.common.constants.Constants;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JobConfGenDriver {

	public enum PluginType {
		Reader, Writer;
	}

	public static void showCopyRight() {
		System.out.println("Taobao DataX V1.0 ");
	}

	private static List<String> getPluginDirAsList(String path) {
		return Arrays.asList(new File(path).list());
	}

	private static Map<PluginType, List<String>> filterPluginType(String path) {
		Map<PluginType, List<String>> ret = new HashMap<PluginType, List<String>>();

		ret.put(PluginType.Reader, getPluginDirAsList(path + "/reader/"));
		ret.put(PluginType.Writer, getPluginDirAsList(path + "/writer/"));

		return ret;
	}
	
	private static int showChoice(String header, List<String> plugins) {
		System.out.println(header);

		int idx = 0;
		for (String plugin : plugins) {
			System.out
					.println(String.format(
							"%d\t%s",
							idx++,
							plugin.toLowerCase().replace("reader", "")
									.replace("writer", "")));
		}

		System.out.print(String.format("Please choose [%d-%d]: ", 0,
				plugins.size() - 1));

		try {
			idx = Integer.valueOf(new Scanner(System.in).nextLine());
		} catch (Exception e) {
			// TODO: handle exception
			idx = -1;
		}

		return idx;
	}
	
	private static int genXmlFile(String filename, ClassNode reader,
			ClassNode writer) throws IOException {

		Document document = DocumentHelper.createDocument();
		Element jobsElement = document.addElement("jobs");
		Element jobElement = jobsElement.addElement("job");
		String id = reader.getName() + "_to_" + writer.getName() + "_job";
		jobElement.addAttribute("id", id);

		/**
		 * 生成reader部分的xml文件
		 */
		Element readerElement = jobElement.addElement("reader");
		Element plugin_Element = readerElement.addElement("plugin");
		plugin_Element.setText(reader.getName());

		ClassNode readerNode = reader;
		Element tempElement = null;

		List<ClassMember> members = readerNode.getAllMembers();
		for (ClassMember member : members) {
			StringBuilder command = new StringBuilder("\n");

			Set<String> set = member.getAllKeys();
			String value = "";
			for (String key : set) {
				value = member.getAttr("default");
				command.append(key).append(":").append(member.getAttr(key))
						.append("\n");
			}
			readerElement.addComment(command.toString());

			String keyName = member.getName();
			keyName = keyName.substring(1, keyName.length() - 1);
			tempElement = readerElement.addElement("param");
			tempElement.addAttribute("key", keyName);

			if (value == null || "".equals(value)) {
				value = "?";
			}
			tempElement.addAttribute("value", value);
		}

		/**
		 * 生成writer部分的xml文件
		 */
		Element writerElement = jobElement.addElement("writer");
		plugin_Element = writerElement.addElement("plugin");
		plugin_Element.setText(writer.getName());

        members = writer.getAllMembers();
		for (ClassMember member : members) {
			StringBuilder command = new StringBuilder("\n");
			Set<String> set = member.getAllKeys();

			String value = "";
			for (String key : set) {
				value = member.getAttr("default");
				command.append(key).append(":").append(member.getAttr(key))
						.append("\n");
			}
			writerElement.addComment(command.toString());

			String keyName = member.getName();
			keyName = keyName.substring(1, keyName.length() - 1);
			tempElement = writerElement.addElement("param");
			tempElement.addAttribute("key", keyName);

			if (value == null || "".equals(value)) {
				value = "?";
			}
			tempElement.addAttribute("value", value);
		}

		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			XMLWriter writerOfXML = new XMLWriter(new FileWriter(new File(
					filename)), format);
			writerOfXML.write(document);
			writerOfXML.close();
		} catch (Exception ex) {
			throw new IOException(ex.getCause());
		}
		
		return 0;
	}

	public static int produceXmlConf() throws IOException {
		showCopyRight();

		/* get all plugin name list */
		Map<PluginType, List<String>> plugins = filterPluginType(Constants.DATAX_LOCATION
				+ "/plugins/");

		/* interactive with user */
		int readerIdx = -1;
		do {
			readerIdx = showChoice("Data Source List :", plugins.get(PluginType.Reader));
		} while (readerIdx < 0
				|| readerIdx >= plugins.get(PluginType.Reader).size());

		int writerIdx = -1;
		do {
			writerIdx = showChoice("Data Destination List :", plugins.get(PluginType.Writer));
		} while (writerIdx < 0
				|| writerIdx >= plugins.get(PluginType.Writer).size());

		/* parse paramkey in each plugin directory */
		String readerName = plugins.get(PluginType.Reader).get(readerIdx);
		String readerPath = Constants.DATAX_LOCATION + "/plugins/reader/"
				+ readerName + "/ParamKey.java";
		ClassNode reader = ParseUtils.parse(readerName,
				FileUtils.readFileToString(new File(readerPath)));

		String writerName = plugins.get(PluginType.Writer).get(writerIdx);
		String writerPath = Constants.DATAX_LOCATION + "/plugins/writer/"
				+ writerName + "/ParamKey.java";
		ClassNode writer = ParseUtils.parse(writerName,
				FileUtils.readFileToString(new File(writerPath)));
		
		String filename = System.getProperty("user.dir") + "/jobs/"
				+ reader.getName() + "_to_" + writer.getName() + "_"
				+ System.currentTimeMillis() + ".xml";
		
		if (0 != genXmlFile(filename, reader, writer)) {
			return -1;
		}
		
		System.out.println(String.format("Generate %s successfully .", filename));
		return 0;
	}

}
