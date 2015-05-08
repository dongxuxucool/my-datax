package com.taobao.datax.plugins.writer.hdfswriter;

public final class ParamKey {
	/*
	 * @name: ugi
	 * @description: HDFS login account, e.g. username,groupname(groupname...),#password
	 * @range:
	 * @mandatory: false
	 * @default:
	 */
	public final static String ugi = "hadoop.job.ugi";
	
	/*
	 * @name: hadoop_conf
	 * @description: hadoop-site.xml path
	 * @range:
	 * @mandatory: false
	 * @default:
	 */
	public final static String hadoop_conf = "hadoop_conf";
	
	/*
	 * @name: dir
	 * @description: hdfs dir，hdfs://ip:port/path, or file:///home/taobao
	 * @range:
	 * @mandatory: true 
	 * @default:
	 */
	public final static String dir = "dir";
	/*
	 * @name: prefixname
	 * @description: hdfs filename
	 * @range:
	 * @mandatory: false 
	 * @default: part
	 */
	public final static String prefixname = "prefix_filename";

	/*
	 * @name: fieldSplit
	 * @description: how to seperate fields
	 * @range:\t,\001,",'
	 * @mandatory: false 
	 * @default:\t
	 */
	public final static String fieldSplit = "field_split";
	/*
	 * @name: lineSplit
	 * @description: how to seperate lines
	 * @range:\n
	 * @mandatory: false 
	 * @default:\n
	 */
	public final static String lineSplit = "line_split";
	/*
	 * @name: encoding 
	 * @description: encode
	 * @range: UTF-8|GBK|GB2312
	 * @mandatory: false
	 * @default: UTF-8
	 */
	public final static String encoding = "encoding";
	
	/*
	 * @name: nullChar
	 * @description: how to replace null in hdfs
	 * @range: 
	 * @mandatory: false
	 * @default:
	 */
	public final static String nullChar = "nullchar";
	
	/*
	 * @name: codecClass
	 * @description: compress codecs
	 * @range:org. apache.hadoop.io.compress.BZip2Codec|org.apache.hadoop.io.compress.DefaultCodec|org.apache.hadoop.io.compress.GzipCodec
	 * @mandatory: false 
	 * @default: org.apache.hadoop.io.compress.DefaultCodec
	 */
	public final static String codecClass = "codec_class";
	/*
	 * @name: compressionType
	 * @description: how to compress file
	 * @range: BLOCK|NONE|RECORD   
	 * @mandatory: false
	 * @default: NONE
	 */
	public final static String compressionType = "compression_type";
	/*
	 * @name: keyFieldIndex
	 * @description: 
	 * @range: [-1-1023]
	 * @mandatory: false
	 * @default: -1
	 */		
	public final static String keyFieldIndex = "key_field_index";
	/*
	 * @name: bufferSize
	 * @description: how much the buffer size is
	 * @range: [1024-4194304]
	 * @mandatory: false 
	 * @default: 4096
	 */
	public final static String bufferSize = "buffer_size";
	/*
	 * @name: fileType
	 * @description: Filetype TXT->TextFile,SEQ->SequenceFile,TXT_COMP->Compressed TextFile,SEQ_COMP->Compressed SequenceFile
	 * @range: TXT|SEQ|TXT_COMP|SEQ_COMP
	 * @mandatory: true 
	 * @default: TXT
	 */
	public final static String fileType = "file_type";
	/*
	 * @name: keyClass
	 * @description:SequenceFile key class
	 * @range:org.apache.hadoop.io.Text|org.apache.hadoop.io.LongWritable|org.apache.hadoop.io.IntWritable|org.apache.hadoop.io.DoubleWritable|org.apache.hadoop.io.BooleanWritable|org.apache.hadoop.io.ByteWritable|org.apache.hadoop.io.VIntWritable|org.apache.hadoop.io.VLongWritable|org.apache.hadoop.io.NullWritable
	 * @mandatory: false 
	 * @default:org.apache.hadoop.io.Text
	 */
	public final static String keyClass = "key_class";
	/*
	 * @name: valueClass
	 * @description:SequenceFile value class
	 * @range:org.apache.hadoop.io.Text|org.apache.hadoop.io.LongWritable|org.apache.hadoop.io.IntWritable|org.apache.hadoop.io.DoubleWritable|org.apache.hadoop.io.BooleanWritable|org.apache.hadoop.io.ByteWritable|org.apache.hadoop.io.VIntWritable|org.apache.hadoop.io.VLongWritable|org.apache.hadoop.io.NullWritable
	 * @mandatory: false
	 * @default:org.apache.hadoop.io.Text
	 */
	public final static String valueClass = "value_class";
	/* 
	 * @name: delMode
	 * @description: do clean data before loading  0: overwrite file with the same filename  1: report error when exists same filename  2: delete single file  3: delete all files with the same prefix name 	4: delete all files in the directory     
	 * @range:[0-4]   
	 * @mandatory: false
	 * @default:3
	 */
	public final static String delMode = "del_mode";
	 /*
       * @name:concurrency
       * @description:concurrency of the job
       * @range:1-100
       * @mandatory: false
       * @default:1
       */
	public final static String concurrency = "concurrency";
	
}
