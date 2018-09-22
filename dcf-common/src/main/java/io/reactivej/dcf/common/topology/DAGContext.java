package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * 
 * @ClassName: DAGContext
 * 
 * @Description: DAG的上下文，用于传递给每个emitter和gear的prepare和open阶段的信息
 * 一个Component对应一个DAGContext
 * 持久化到ZK
 * 
 * @author: Wang Xiao Tian, heartup@gmail.com
 * 
 * @date: 2015年8月10日 下午7:52:28
 */
public class DAGContext implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5001813183492021897L;
	/**
	 * 该上下文对应的全局DAG
	 */
	private DAG dag;
	/**
	 * 全局配置
	 */
	private Map<String, Serializable> globalConf;
	
	// 当前component id
	private String componentId;
	
	/**
	 * streamId -> componentId -> groupStrategy
	 * 
	 * 用于快速的查找从当前component（由this.componentId指定），
	 * 发往指定Stream【每个component的输出可以根据streamid流往不同的下一个component（计算逻辑）】（由streamId指定）的数据流，
	 * 应该通过怎样的聚合方式【聚合方式决定了数据流往component的哪个分片（具体哪个worker）】（由groupStrategy指定），
	 * 到达哪个component【component决定了下一步的计算逻辑】（由componentId确定）
	 */
	private Map<String, Map<String, Grouping>> streamTargets;

	private Map<String, IStream> outputStreams;

	public DAGContext() {
	}

	public DAGContext(DAG thisDAG, Map<String, IStream> outputStreams, Map<String, Serializable> globalConf, String componentId) {
		this.dag = thisDAG;
		this.outputStreams = outputStreams;
		this.globalConf = globalConf;
		this.componentId = componentId;
	}

	public String getComponentId() {
		return componentId;
	}

	public Map<String, Serializable> getGlobalConf() {
		return globalConf;
	}

	public void setGlobalConf(Map<String, Serializable> globalConf) {
		this.globalConf = globalConf;
	}

	public DAG getDAG() {
		return dag;
	}

	public void setDAG(DAG DAG) {
		this.dag = DAG;
	}

	public Map<String, IStream> getOutputStreams() {
		return outputStreams;
	}

	/**
	 * 返回对应Stream的target，target是 componentId -> groupingStrategy的Map
	 * @param sid
	 * @return
	 */
	public synchronized Map<String, Grouping> getTargets(String sid) {
		if (streamTargets == null) {
			this.streamTargets = new HashMap<String, Map<String, Grouping>>();
			
			for (String compId : getDAG().getComponents().keySet()) {
				IComponentDescription comp = getDAG().getComponent(compId);
				Map<GlobalStreamId, IStream> streams = comp.getInputStreams();
				for (GlobalStreamId streamId : streams.keySet()) {
					if (streamId.getComponentId().equals(getComponentId())) {
						Map<String, Grouping> targets = streamTargets.get(streamId.getStreamId());
						if (targets == null) {
							targets = new HashMap<String, Grouping>();
							streamTargets.put(streamId.getStreamId(), targets);
						}
						
						targets.put(compId, streams.get(streamId).getGrouping());
					}
				}
			}
		}
		
		return streamTargets.get(sid);
	}
}
