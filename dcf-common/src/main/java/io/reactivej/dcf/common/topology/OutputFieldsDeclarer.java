package io.reactivej.dcf.common.topology;

import java.util.HashMap;
import java.util.Map;

/***
 * 一个OutputFieldsDeclarer和一个component对应，用来通过component的defineOutputFields方法来定义此component的输出数据流
 * component的输入数据流通过GearDeclare的方法来进行定义
 * 
 * @author heartup@gmail.com
 *
 */
public class OutputFieldsDeclarer implements IOutputFieldsDeclarer {
	
	private Map<String, IStream> outputStreams = new HashMap<>();

	public OutputFieldsDeclarer() {
	}

	public Map<String, IStream> getOutputStreams() {
		return outputStreams;
	}

	@Override
	public void declare(String streamId, Fields fields) {
		IStream stream = new Stream(fields);
		outputStreams.put(streamId, stream);
	}

	@Override
	public void declare(Fields fields) {
		declare(IDAG.DEFAULT_STREAMID, fields);
	}

}
