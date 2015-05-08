package com.taobao.datax.plugins.reader.fakereader;

public class ParamKey {
	/*
	 * @name: fieldnum
	 * @description: how many field will be added into one line
	 * @range:
	 * @mandatory: false
	 * @default: 4
	 */
	public final static String fieldNum = "fieldnum";
	
	/*
	 * @name: field
	 * @description: what content will be added into one line
	 * @range:
	 * @mandatory: false
	 * @default: bazhen.csy
	 */
	public final static String field = "field";
	 /*
     * @name:concurrency
     * @description:concurrency of the job
     * @range:1-10
     * @mandatory: false
     * @default:1
     */
	public final static String concurrency = "concurrency";
}
