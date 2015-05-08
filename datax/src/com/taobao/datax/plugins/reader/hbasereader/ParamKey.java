package com.taobao.datax.plugins.reader.hbasereader;

public final class ParamKey {
	/*
	 * @name: htable
	 * @description:hbase table name
	 * @range:
	 * @mandatory: true
	 * @default: 
	 */
	public final static String htable = "htable";
	/*
	 * @name: hbase_conf
	 * @description: where the hbase_conf file is
	 * @range:
	 * @mandatory: true
	 * @default: 
	 */
	public final static String hbase_conf = "hbase_conf";
	/*
	 * @name: hadoop_conf
	 * @description: where the hadoop_conf file is
	 * @range:
	 * @mandatory: true
	 * @default: 
	 */
	public final static String hadoop_conf = "hadoop_conf";
	/*
	 * @name: encoding
	 * @description: indict the encoding in hbase
	 * @range: UTF-8|GBK
	 * @mandatory: false
	 * @default: UTF-8
	 */
	public final static String encoding = "encoding";

	/*
	 * @name: columns_key
	 * @description: indict which CF:qualifier should be write, split by ","
	 * @range: 
	 * @mandatory: false
	 * @default: 
	 */
	public final static String columns_key = "columns_key";
	
	/*
	 * @name: rowkey_range
	 * @description: range of  rowkey
	 * @range: 
	 * @mandatory: false
	 * @default: 
	 */
	public final static String rowkey_range="rowkey_range";
	
	 /*
       * @name:concurrency
       * @description:concurrency of the job
       * @range:1-100
       * @mandatory: false
       * @default:1
       */
	public final static String concurrency = "concurrency";
}
