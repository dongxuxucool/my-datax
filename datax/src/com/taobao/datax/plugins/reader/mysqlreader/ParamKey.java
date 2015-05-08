package com.taobao.datax.plugins.reader.mysqlreader;

public final class ParamKey {
		 /*
	       * @name: ip
	       * @description: Mysql database's ip address
	       * @range:
	       * @mandatory: true
	       * @default:
	       */
		public final static String ip = "ip";
		/*
	       * @name: port
	       * @description: Mysql database's port
	       * @range:
	       * @mandatory: true
	       * @default:3306
	       */
		public final static String port = "port";
		/*
	       * @name: dbname
	       * @description: Mysql database's name
	       * @range:
	       * @mandatory: true
	       * @default:
	       */
		public final static String dbname = "dbname";
		/*
	       * @name: username
	       * @description: Mysql database's login name
	       * @range:
	       * @mandatory: true
	       * @default:
	       */
		public final static String username = "username";
		/*
	       * @name: password
	       * @description: Mysql database's login password
	       * @range:
	       * @mandatory: true
	       * @default:
	       */
		public final static String password = "password";
		/*
	       * @name: tables
	       * @description: tables to export data, format can support simple regex, table[0-63]
	       * @range: 
	       * @mandatory: true
	       * @default: 
	       */
		public final static String tables = "tables";
		/*
	       * @name: where
	       * @description: where clause, like 'modified_time > sysdate'
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
	       * @name: columns
	       * @description: columns to be selected, default is *
	       * @range: 
	       * @mandatory: false
	       * @default: *
	       */
		public final static String columns = "columns";
		/*
	       * @name: encoding
	       * @description: mysql database's encode
	       * @range: UTF-8|GBK|GB2312
	       * @mandatory: false
	       * @default: UTF-8
	       */
		public final static String encoding = "encoding";
		
       /*
	       * @name: mysql.params
	       * @description: mysql driver params, starts with no &, e.g. loginTimeOut=3000&yearIsDateType=false
	       * @range: 
	       * @mandatory: false
	       * @default:
	       */
		public final static String mysqlParams = "mysql.params";
		
		 /*
	       * @name: concurrency
	       * @description: concurrency of the job
	       * @range: 1-10
	       * @mandatory: false
	       * @default: 1
	       */
		public final static String concurrency = "concurrency";
}
