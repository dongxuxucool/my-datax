/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.engine.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtils {

	private static final String METHOD_DECLARATION = "(/\\*(.+?)\\*/)([^;]+;)";

	private static final String COMMENT_PATTERN = "(\\w+\\s*)(:)([^\\r\\n]+)";

	private static final String MEMBER_PATTERN = "([^=]=\\s*)(\"[^\"]+\")";

	public static ClassNode parse(String name, String source) {
		source = source.substring(source.indexOf("{") + 1);
		source = source.substring(0, source.lastIndexOf('}'));

		ClassNode node = ClassNode.newInstance();
		node.setName(name);

		Pattern pattern = Pattern.compile(METHOD_DECLARATION, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(source);

		while (matcher.find()) {
			/* parse comment */
			// System.out.println("method: " + matcher.group(0));
			Pattern commentPattern = Pattern.compile(COMMENT_PATTERN);
			Matcher commentMatcher = commentPattern.matcher(matcher.group(1));
			if (!commentMatcher.find())
				throw new IllegalArgumentException(
						"File format error: class declaration without comment @"
								+ matcher.group(1));

			Map<String, String> attributes = new HashMap<String, String>();
			do {
				attributes.put(commentMatcher.group(1), commentMatcher.group(3)
						.trim());
			} while (commentMatcher.find());

			/* add key */
			Pattern memberPattern = Pattern.compile(MEMBER_PATTERN);
			Matcher memberMatcher = memberPattern.matcher(matcher.group(3));

			if (!memberMatcher.find())
				throw new IllegalArgumentException(
						"File format error: comment without member declaration @"
								+ matcher.group(3));

			node.addMember(ClassNode.createMember(memberMatcher.group(2), attributes));
		}

		return node;
	}

	/*
	public static void main(String[] args) throws IOException {
		System.out.println(parse("reader",
				FileUtils
						.readFileToString(new File(
								"/home/hedgehog/workspace/dataexchange/src/com/taobao/datax/plugins/reader/streamreader/ParamKey.java"))));
		
		return;
	}
	*/
}
