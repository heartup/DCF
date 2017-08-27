package io.reactivej.dcf.common.topology.builder;

import io.reactivej.dcf.common.topology.*;

/***
 * 用来定义Gear的输入数据流，同时定义了数据的分组方式
 * 
 * @author heartup@gmail.com
 *
 */
public class GearInputStreamDeclarer {

	private DAG dag = null;
	private String gearId = null;
	
	public GearInputStreamDeclarer(DAG dag, String targetId) {
		this.dag = dag;
		this.gearId = targetId;
	}

	public DAG getDag() {
		return dag;
	}
	
	public String getGearId() {
		return gearId;
	}
	
	/**
	 * 定义了这个Gear的一个根据Fields进行分组的输入流
	 * @param sourceComponentId 源component id
	 * @param streamId 源component的哪个Stream
	 * @param fields 数据根据那些fields聚合
	 * @return 返回this，可以用于定义下一条输入流
	 */
	public GearInputStreamDeclarer fieldGrouping(String sourceComponentId, String streamId, Fields fields) {
		IStream stream = new Stream();
		stream.setGrouping(Grouping.fieldGrouping(fields));
		
		dag.getComponent(gearId).getInputStreams().put(new GlobalStreamId(sourceComponentId, streamId), stream);
		return this;
	}

	/**
	 *
	 * 定义了这个Gear的一个平均分组的输入流
	 * @param sourceComponentId 源component
	 * @param streamId 源component的哪个Stream
	 * @return 返回this，可以用于定义下一条输入流
	 */
	public GearInputStreamDeclarer meanGrouping(String sourceComponentId, String streamId) {
		IStream stream = new Stream();
		stream.setGrouping(Grouping.meanGrouping());

		getDag().getComponent(gearId).getInputStreams().put(new GlobalStreamId(sourceComponentId, streamId), stream);
		return this;
	}

	/**
	 * 
	 * 定义了这个Gear的一个随机分组的输入流
	 * @param sourceComponentId 源component
	 * @param streamId 源component的哪个Stream
	 * @return 返回this，可以用于定义下一条输入流
	 */
	public GearInputStreamDeclarer randomGrouping(String sourceComponentId, String streamId) {
		IStream stream = new Stream();
		stream.setGrouping(Grouping.randomGrouping());
		
		getDag().getComponent(gearId).getInputStreams().put(new GlobalStreamId(sourceComponentId, streamId), stream);
		return this;
	}
	
	public GearInputStreamDeclarer randomGrouping(String sourceComponentId) {
		return randomGrouping(sourceComponentId, "DEFAULT");
	}
	
	public GearInputStreamDeclarer globalGrouping(String sourceComponentId, String streamId) {
		IStream stream = new Stream();
		stream.setGrouping(Grouping.globalGrouping());
		
		getDag().getComponent(gearId).getInputStreams().put(new GlobalStreamId(sourceComponentId, streamId), stream);
		return this;
	} 
}
