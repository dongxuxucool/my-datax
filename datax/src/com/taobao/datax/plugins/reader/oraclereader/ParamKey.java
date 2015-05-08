package com.taobao.datax.plugins.reader.oraclereader;

public final class ParamKey {
	/*
       * @name: dbname
       * @description: Oracle database name
       * @range:
       * @mandatory: true
       * @default:
       */
	public final static String dbname = "dbname";
	/*
       * @name: username
       * @description:  Oracle database login username
       * @range:
       * @mandatory: true
       * @default:
       */
	public final static String username = "username";
	/*
       * @name: password
       * @description: Oracle database login password
       * @range:
       * @mandatory: true
       * @default:
       */
	public final static String password = "password";
	/*
       * @name: schema
       * @description: Oracle database schema
       * @range:
       * @mandatory: true
       * @default:
       */
	public final static String schema = "schema";
	 /*
       * @name: ip
       * @description: Oracle database ip address
       * @range:
       * @mandatory: true
       * @default:
       */
	public final static String ip = "ip";
	/*
       * @name: port
       * @description: Oracle database port
       * @range:
       * @mandatory: true
       * @default: 1521
       */
	public final static String port = "port";
	/*
       * @name: tables
       * @description: tables to be exported
       * @range: 
       * @mandatory: true
       * @default: 
       */
	public final static String tables = "tables";
	
	/*
       * @name: columns
       * @description: columns to be selected
       * @range: 
       * @mandatory: false
       * @default: *
       */
	public final static String columns = "columns";
	
	/*
       * @name: where
       * @description: where clause, like 'gmtdate > trunc(sysdate)'
       * @range: 
       * @mandatory: false
       * @default: 
       */
	public final static String where = "where";		
	/*
       * @name: sql
       * @description: self-defined sql statement
       * @range: 
       * @mandatory: false
       * @default: 
       */
	public final static String sql = "sql";
	/*
       * @name: encoding
       * @description: oracle database encode
       * @range: UTF-8|GBK|GB2312
       * @mandatory: false
       * @default: UTF-8
       */
	public final static String encoding = "encoding";
	/*
       * @name: split_mod
       * @description: how to split job
       * @range: 0-no split, 1-rowid split, others ntile split 
       * @mandatory: false
       * @default: 1
       */
	public final static String splitMod = "split_mod";
	/*
       * @name: tnsfile
       * @description: tns file path
       * @range: 
       * @mandatory: true
       * @default: /home/oracle/product/10g/db/network/admin/tnsnames.ora
       */
	public final static String tnsFile = "tnsfile";

	 /*
       * @name:concurrency
       * @description:concurrency of the job
       * @range:1-100
       * @mandatory: false
       * @default:1
       */
	public final static String concurrency = "concurrency";
}
