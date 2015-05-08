/**
 * (C) 2010-2011 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * version 2 as published by the Free Software Foundation. 
 * 
 */

package com.taobao.datax.plugins.reader.hdfsreader;

import com.taobao.datax.common.exception.ExceptionTracker;
import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.*;
import com.taobao.datax.plugins.common.DFSUtils;
import com.taobao.datax.plugins.common.DFSUtils.HdfsFileType;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HdfsReader extends Reader {

	private static final Logger logger = Logger.getLogger(HdfsReader.class);

	private Map<Integer, String> constColMap = new HashMap<Integer, String>();

	private static Map<DFSUtils.HdfsFileType, Class<? extends DfsReaderStrategy>> readerStrategyMap = null;

	private static volatile boolean fileTypePrintVirgin = true;

	private DfsReaderStrategy readerStrategy = null;

	private FileSystem fs = null;

	private Path p = null;

	private char fieldSplit = '\t';

	private String encoding = "UTF-8";

	private int bufferSize = 4 * 1024;

	private String colFilter = null;

	private String ignoreKey = null;

	private Set<Integer> filters = null;

	private String nullString = "";

	private HdfsFileType fileType = null;

	private String configure = null;

	private String dir = null;

	private String ugi = null;

	private int[] colList = new int[PluginConst.LINE_MAX_FIELD];

	private boolean colListSet = false;

	private int emptyFile = 0;

	static {
		/* */
		readerStrategyMap = new HashMap<DFSUtils.HdfsFileType, Class<? extends DfsReaderStrategy>>();
		readerStrategyMap.put(DFSUtils.HdfsFileType.TXT,
				DfsReaderTextFileStrategy.class);
		readerStrategyMap.put(DFSUtils.HdfsFileType.COMP_TXT,
				DfsReaderCompTextFileStrategy.class);
		readerStrategyMap.put(DFSUtils.HdfsFileType.SEQ,
				DfsReaderSequeueFileStrategy.class);
		
		Thread.currentThread().setContextClassLoader(
				HdfsReader.class.getClassLoader());
	}

	@Override
	public int prepare(PluginParam param) {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public List<PluginParam> split(PluginParam param) {
		HdfsDirSplitter spliter = new HdfsDirSplitter();
		spliter.setParam(param);
		spliter.init();
		return spliter.split();
	}

	@Override
	public int init() {
		bufferSize = param.getIntValue(ParamKey.bufferSize,
				bufferSize);
		fieldSplit = param.getCharValue(ParamKey.fieldSplit, '\t');
		encoding = param.getValue(ParamKey.encoding, "utf-8");
		ignoreKey = param.getValue(ParamKey.ignoreKey, "true");
		nullString = param.getValue(ParamKey.nullString,
				this.nullString);
		colFilter = param.getValue(ParamKey.colFilter, "");
		configure = param.getValue(ParamKey.hadoop_conf, "");
		ugi = param.getValue(ParamKey.ugi, null);
		dir = param.getValue(ParamKey.dir);

		/* check parameters */
		if (StringUtils.isBlank(dir)) {
			throw new DataExchangeException("Can't find the param ["
					+ ParamKey.dir + "] in hdfs-reader-param.");
		}

		/*
		 * add user-define colums for hdfsreader e.g. #0, #1, #2, null we
		 * extract column 0, column 1, column 2 from original hdfsfile and
		 * assemble a new line with a empty column append
		 */

		if (!StringUtils.isBlank(colFilter)) {
			for (int i = 0; i < colList.length; ++i) {
				colList[i] = -1;
			}

			int index = 0;
			String filter = null;
			String[] cols = colFilter.split(",");
			for (; index < cols.length; ++index) {
				filter = cols[index].trim();
				if (filter.startsWith("#")) {
					try {
						int colIndex = Integer.valueOf(filter.substring(1));
						if (colIndex >= colList.length) {
							logger.error(String.format("Columns index larger than %d, not supported .",
									PluginConst.LINE_MAX_FIELD));
							return PluginStatus.FAILURE.value();
						}
						colList[colIndex] = index;
					} catch (NumberFormatException e) {
						throw new DataExchangeException(e.getCause());
					}
				} else if ("null".equalsIgnoreCase(filter)) {
					constColMap.put(index, "");
				} else {
					constColMap.put(index, filter);
				}
			}
			if (cols.length > 0) {
				colListSet = true;
			}
		}

		/* check hdfs file type */

		try {
			fs = DFSUtils.createFileSystem(URI.create(dir),
					DFSUtils.getConf(dir, ugi, configure));
		} catch (Exception e) {
			closeAll();
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(String.format(
					"Initialize file system failed:%s,%s", e.getMessage(),
					e.getCause()));
		}

		if (fs == null) {
			closeAll();
			throw new DataExchangeException("Create the file system failed.");
		}

		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int connect() {
		try {
			fileType = DFSUtils.checkFileType(fs, new Path(dir),
					DFSUtils.getConf(dir, ugi, configure));
			Class<? extends DfsReaderStrategy> recogniser = readerStrategyMap
					.get(fileType);
			String name = recogniser.getName().substring(
					recogniser.getName().lastIndexOf(".") + 1);
			if (fileTypePrintVirgin) {
				logger.info(String.format("Recognise filetype, use %s .", name));
				fileTypePrintVirgin = false;
			}
			readerStrategy = (DfsReaderStrategy) recogniser.getConstructors()[0]
					.newInstance(this);

		} catch (Exception e) {
			logger.error(ExceptionTracker.trace(e));
			closeAll();
			throw new DataExchangeException(String.format(
					"Initialize file system failed:%s,%s", e.getMessage(),
					e.getCause()));
		}

		p = new Path(dir);

        try {
			if (!fs.exists(p)) {
				closeAll();
				throw new DataExchangeException("File [" + dir
						+ "] does not exist.");
			}

			emptyFile = readerStrategy.open();

			getMonitor().setStatus(PluginStatus.CONNECT);
			return PluginStatus.SUCCESS.value();
		} catch (IOException e) {
			closeAll();
			logger.error(ExceptionTracker.trace(e));
			throw new DataExchangeException(String.format(
					"Initialize file system is failed:%s,%s", e.getMessage(),
					e.getCause()));
		}
	}

	@Override
	public int startRead(LineSender sender) {
		getMonitor().setStatus(PluginStatus.READ);
		try {
			if (emptyFile > -1) {
				readerStrategy.registerSender(sender);
				while (readerStrategy.next()) {
					readerStrategy.sendToWriter();
				}
				sender.flush();
			}
		} catch (Exception ex) {
			logger.error(ExceptionTracker.trace(ex));
			throw new DataExchangeException(String.format(
					"Errors in starting hdfsreader: %s, %s", ex.getMessage(),
					ex.getCause()));
		} finally {
			readerStrategy.close();
			closeAll();
		}
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int finish() {
		closeAll();
		getMonitor().setStatus(PluginStatus.READ_OVER);
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int cleanup() {
		closeAll();
		return PluginStatus.SUCCESS.value();
	}

	private void closeAll() {
		try {
			IOUtils.closeStream(fs);
		} catch (Exception e) {
			throw new DataExchangeException(String.format(
					"HdfsReader closing failed: %s,%s", e.getMessage(),
					e.getCause()));
		}
	}

	private String replace(String string) {
		if (nullString != null && nullString.equals(string)) {
			return null;
		}
		return string;
	}

	public interface DfsReaderStrategy {
		int open() throws IOException;

		void registerSender(LineSender sender);

		boolean next() throws IOException;

		Line sendToWriter();

		void close();
	}

	class DfsReaderSequeueFileStrategy implements DfsReaderStrategy {

		private Configuration conf = null;

		private SequenceFile.Reader reader = null;

		private LineSender sender = null;

		private Writable key = null;

		private Writable value = null;

		private String keyclass = null;

		private String valueclass = null;

		private boolean isIgnoreKey = false;

		private boolean isIgnoreValue = false;

		public DfsReaderSequeueFileStrategy() {
			this.conf = DFSUtils.newConf();
		}

		@Override
		public void close() {
			IOUtils.closeStream(reader);
		}

		@Override
		public boolean next() throws IOException {
            return reader.next(key, value);
		}

		@Override
		public int open() throws IOException {
			try {
				conf.setInt("io.file.buffer.size", bufferSize);
				reader = new SequenceFile.Reader(fs, p, conf);
				key = (Writable) ReflectionUtils.newInstance(
						reader.getKeyClass(), conf);
				value = (Writable) ReflectionUtils.newInstance(
						reader.getValueClass(), conf);
				keyclass = key.getClass().getName();
				valueclass = value.getClass().getName();
				if (("TRUE".equalsIgnoreCase(ignoreKey))
						|| ("org.apache.hadoop.io.NullWritable"
								.equals(keyclass))) {
					isIgnoreKey = true;
				}
				if ("org.apache.hadoop.io.NullWritable".equals(valueclass)) {
					isIgnoreValue = true;
				}
				return PluginStatus.SUCCESS.value();
			} catch (EOFException e) {
				logger.warn("File is empty file.");
				return PluginStatus.SUCCESS.value();
			}
        }

		@Override
		public Line sendToWriter() {
			if (null == sender) {
				throw new IllegalStateException("LineSender cannot be null .");
			}

			Line line = sender.createLine();
			if (!isIgnoreKey) {
				line.addField(key.toString());
			}
			if (!isIgnoreValue) {
				String s = value.toString();
				int begin = 0;
				int i = 0;
				if (!colListSet) {
					for (i = 0; i < s.length(); ++i) {
						if (s.charAt(i) == fieldSplit) {
							line.addField(replace(s.substring(begin, i)));
							begin = i + 1;
						}
					}
					// last field
					line.addField(replace(s.substring(begin, i)));
				} else {
					int colIndex = 0;
					for (i = 0; i < s.length(); ++i) {
						if (s.charAt(i) == fieldSplit) {
							if (colList[colIndex] >= 0) {
								line.addField(replace(s.substring(begin, i)),
										colList[colIndex]);
							}
							begin = i + 1;
							colIndex++;
						}
					}
					if (colList[colIndex] >= 0) {
						line.addField(replace(s.substring(begin, i)),
								colList[colIndex]);
					}
					// add constant columns
					for (Integer k : constColMap.keySet()) {
						line.addField(constColMap.get(k), k);
					}
				}
			}
			boolean flag = sender.sendToWriter(line);

			if (flag)
				getMonitor().lineSuccess();
			else
				getMonitor().lineFail("Adding the line is failed.");

			return line;
		}

		@Override
		public void registerSender(LineSender sender) {
            this.sender = sender;
		}

	}

	class DfsReaderTextFileStrategy implements DfsReaderStrategy {
		private Configuration conf = null;

		private FSDataInputStream in = null;

		private CompressionInputStream cin = null;

		private BufferedReader br = null;

		private LineSender sender = null;

		private String s = null;

		private boolean compressed = false;

		private DfsReaderTextFileStrategy(boolean compressed) {
			this.conf = DFSUtils.newConf();
			this.compressed = compressed;
		}

		public DfsReaderTextFileStrategy() {
			this(false);
		}

		@Override
		public void close() {
			IOUtils.cleanup(null, in, cin, br);
		}

		@Override
		public Line sendToWriter() {
			if (null == sender) {
				throw new IllegalStateException("LineSender cannot be null .");
			}

			Line line = sender.createLine();
			int begin = 0;
			int i = 0;
			if (!colListSet) {
				for (i = 0; i < s.length(); ++i) {
					if (s.charAt(i) == fieldSplit) {
						line.addField(replace(s.substring(begin, i)));
						begin = i + 1;
					}
				}
				// last field
				line.addField(replace(s.substring(begin, i)));
			} else {
				int colIndex = 0;
				for (i = 0; i < s.length(); ++i) {
					if (s.charAt(i) == fieldSplit) {
						if (colList[colIndex] >= 0) {
							line.addField(replace(s.substring(begin, i)),
									colList[colIndex]);
						}
						begin = i + 1;
						colIndex++;
					}
				}
				if (colList[colIndex] >= 0) {
					line.addField(replace(s.substring(begin, i)),
							colList[colIndex]);
				}
				// add constant columns
				for (Integer k : constColMap.keySet()) {
					line.addField(constColMap.get(k), k);
				}
			}
			boolean flag = sender.sendToWriter(line);

			if (flag)
				getMonitor().lineSuccess();
			else
				getMonitor().lineFail("Adding the line failed.");

			return line;
		}

		@Override
		public boolean next() throws IOException {
            s = br.readLine();
            if (null != s)
				return true;
			return false;
		}

		@Override
		public int open() throws IOException {
            if (compressed) {
                CompressionCodecFactory factory = new CompressionCodecFactory(
                        conf);
                CompressionCodec codec = factory.getCodec(p);
                if (codec == null) {
                    throw new IOException(
                            String.format(
                                    "Can't find any suitable CompressionCodec to this file:%s",
                                    p.toString()));
                }
                in = fs.open(p);
                cin = codec.createInputStream(in);
                br = new BufferedReader(
                        new InputStreamReader(cin, encoding), bufferSize);
            } else {
                in = fs.open(p);
                br = new BufferedReader(
                        new InputStreamReader(in, encoding), bufferSize);
            }
            if (in.available() == 0)
                return PluginStatus.FAILURE.value();
            else
                return PluginStatus.SUCCESS.value();
        }

		@Override
		public void registerSender(LineSender sender) {
			this.sender = sender;
		}
	}

	class DfsReaderCompTextFileStrategy extends DfsReaderTextFileStrategy {
		public DfsReaderCompTextFileStrategy() {
			super(true);
		}
	}

}
