package io.reactivej.dcf.common.topology.builder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import io.reactivej.dcf.common.topology.*;

/**
 * 
 * 
 * @ClassName: TopologyBuilder
 * 
 * @Description: DAG构建器 基于 DAG的顶点（component）构建一个DAG，包括顶点和DAG上的一条通路
 * 
 * @author: Wang Xiao Tian
 * 
 * @date: 2015年8月13日 上午9:51:38
 */

public class TopologyBuilder {
	/**
	 * 内部hold的DAG
	 */
	private DAG DAGHolder;

	/**
	 * 内部 hold 的平行度
	 */
	private Map<String, Integer> parallelismHolder;

	private Map<String, Integer> threadParallelismHolder;
	
	/**
	 * 并行处理核心线程数
	 */
	private Map<String, Integer> concurrentCoreSizeHolder = Maps.newHashMap();
	/**
	 * 并行处理队列长度
	 */
	private Map<String, Integer> concurrentQueueSizeHolder = Maps.newHashMap();
	/**
	 * netty 读取高水位阈值
	 */
	private Map<String, Integer> highReadWaterMarkerHolder = Maps.newHashMap();
	/**
	 * netty 读取低水位阈值
	 */
	private Map<String, Integer> lowReadWaterMarkerHolder = Maps.newHashMap();
	
	private Map<String, Serializable> config;
	
	public TopologyBuilder() {
		DAGHolder = new DAG();
		parallelismHolder = new HashMap<String, Integer>();
		threadParallelismHolder = new HashMap<String, Integer>();
		config = new HashMap<String, Serializable>();
	}

	public GearInputStreamDeclarer setGear(String id, Serializable gearDefinition) {
		return setGear(id, gearDefinition, 300 * 1024 * 1024);
	}

	public GearInputStreamDeclarer setGear(String id, Serializable gearDefinition, long memoryNeeded) {
		DAGHolder.putGear(id, gearDefinition, memoryNeeded);
		GearInputStreamDeclarer declarer = new GearInputStreamDeclarer(DAGHolder, id);
		parallelismHolder.put(id, 1);
		threadParallelismHolder.put(id, 1);
		return declarer;
	}

	/**
	 * 
	
	 * @Title: setGear
	
	 * @Description: 在DAG中定义一个Gear,以及流入的边
	 * 指定了平行度
	 * 几个平行度就有几个task（线程）
	
	 * @param id
	 * @param gearDefinition
	 * @param parallelism
	 * @return
	
	 * @return: IDAGEdge
	 */
	public GearInputStreamDeclarer setGear(String id, Serializable gearDefinition, long memoryNeeded, int parallelism, int threadPara) {
		DAGHolder.putGear(id, gearDefinition, memoryNeeded);
		GearInputStreamDeclarer declarer = new GearInputStreamDeclarer(DAGHolder, id);
		parallelismHolder.put(id, parallelism);
		threadParallelismHolder.put(id, threadPara);
		return declarer;
	}

	public void setEmitter(String id, Serializable emitterDefinition) {
		setEmitter(id, emitterDefinition, 300 * 1024 * 1024);
	}
	/**
	 * 
	
	 * @Title: setEmitter
	
	 * @Description: 设置平行度为1的Emitter
	 * 如果有外部需求，则返回 declarer 处理
	
	 * @param id
	 * @param emitterDefinition
	
	 * @return: void
	 */
	public void setEmitter(String id, Serializable emitterDefinition, long memoryNeeded) {
		DAGHolder.putEmitter(id, emitterDefinition, memoryNeeded);
		parallelismHolder.put(id, 1);
		threadParallelismHolder.put(id, 1);
	}
	
	
	/**
	 * 
	
	 * @Title: setEmitter
	
	 * @Description: 设置平行度为参数 parallelism 的 Emitter
	
	 * @param id
	 * @param emitterDefinition
	 * @param parallelism
	
	 * @return: void
	 */
	public void setEmitter(String id, Serializable emitterDefinition, long memoryNeeded, int parallelism, int threadPara) {
		DAGHolder.putEmitter(id, emitterDefinition, memoryNeeded);
		parallelismHolder.put(id, parallelism);
		threadParallelismHolder.put(id, threadPara);
	}
	
	public Map<String, Serializable> getConfig() {
		return config;
	}
	
	public void setConfig(Map<String, Serializable> config) {
		this.config = config;
	}
	
	/**
	 * 
	
	 * @Title: createStreamOnDAG
	
	 * @Description: 创建DAG上的流
	
	 * @return: DAG
	 */
	public Topology createTopology() {
		return new Topology(DAGHolder, parallelismHolder, threadParallelismHolder, config);
	}
	
	public Topology createScheduledTopology(String cronExp) {
		Topology topology = createTopology();
		topology.setCronExp(cronExp);
		return topology;
	}
	
	public void config(String key, Serializable value) {
		this.config.put(key, value);
	}
}
