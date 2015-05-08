package com.taobao.datax.plugins.reader.fakereader;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.taobao.datax.common.plugin.Line;
import com.taobao.datax.common.plugin.LineSender;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Reader;

public class FakeReader extends Reader {

	private String field = "bazhen.csy";

	private int fieldNum = 0;

	private int concurrency = 1;

	private Logger logger = Logger.getLogger(FakeReader.class);

	@Override
	public int init() {
		this.field = param.getValue(ParamKey.field, "bazhen.csy");
		this.fieldNum = param.getIntValue(ParamKey.fieldNum, 0);
		this.concurrency = param.getIntValue(ParamKey.concurrency,
				this.concurrency);

		this.logger.info("FakeReader init completed .");
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public List<PluginParam> split(PluginParam param) {
		List<PluginParam> params = new ArrayList<PluginParam>();
		for (int i = 0; i < this.concurrency; i++) {
			params.add(param.clone());
		}
		return params;
	}

	@Override
	public int connect() {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int startRead(LineSender sender) {
		this.logger.info("FakeReader start to produce lines .");
		while (true) {
			Line line = sender.createLine();
			for (int i = 0; i < fieldNum; i++) {
				line.addField(field);
			}
			sender.sendToWriter(line);
		}
	}

	@Override
	public int finish() {
		return PluginStatus.SUCCESS.value();
	}

}
