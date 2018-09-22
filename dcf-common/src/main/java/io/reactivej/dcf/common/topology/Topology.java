package io.reactivej.dcf.common.topology;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @ClassName: Topology
 * 
 * @Description: 一个拓扑结构包含DAG、并行度和参数 持久化到ZK
 * 包含调度信息
 * 
 * @author: Wang Xiao Tian
 * 
 * @date: 2015年8月20日 下午10:04:42
 */
public class Topology implements Serializable {

	private static final long serialVersionUID = -3752953147849701802L;

	public enum TopologyState {

		INIT,
		/**
		 * 准备中，是一种提交到集群之前的状态
		 */
		PREPARING,

		/**
		 * 执行中
		 */
		EXECUTING,

		KILLED,

		FINISHED,

		/**
		 * 此Topology生命周期结束，需要被清理
		 */
		TERMINATED
	}

	private TopologyState state = TopologyState.INIT;

	private GlobalTopologyId topologyId;
	private String name;
	private String description;

	private long tupleTimeout;
	private int tupleQueueSizeLow;
	private int tupleQueueSizeHigh;

	private String cronExp;   // 是否是定时任务

	private DAG dag;
	private Map<String, Integer> parallelism;
	private Map<String, Integer> threadParallelism;

	protected Map<String, Serializable> config;
	private List<String> resourceUrls = Lists.newArrayList();

	public Topology() {
	}

	public Topology(DAG holdedDAG, Map<String, Integer> parallelismHolder, Map<String, Integer> threadParallelismHolder, Map<String, Serializable> config) {
		this.dag = holdedDAG;
		this.parallelism = parallelismHolder;
		this.threadParallelism = threadParallelismHolder;
		this.config = config;
	}

	public TopologyState getState() {
		return state;
	}

	public void setState(TopologyState state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, Integer> getParallelism() {
		return parallelism;
	}

	public void setParallelism(Map<String, Integer> parallelism) {
		this.parallelism = parallelism;
	}

	public Map<String, Integer> getThreadParallelism() {
		return threadParallelism;
	}

	public void setThreadParallelism(Map<String, Integer> threadParallelism) {
		this.threadParallelism = threadParallelism;
	}

	public Map<String, Serializable> getConfig() {
		return config;
	}

	public void setConfig(Map<String, Serializable> config) {
		this.config = config;
	}

	public GlobalTopologyId getTopologyId() {
		return topologyId;
	}

	public void setTopologyId(GlobalTopologyId topologyId) {
		this.topologyId = topologyId;
	}

	public long getTupleTimeout() {
		return tupleTimeout;
	}

	public void setTupleTimeout(long tupleTimeout) {
		this.tupleTimeout = tupleTimeout;
	}

	public int getTupleQueueSizeLow() {
		return tupleQueueSizeLow;
	}

	public void setTupleQueueSizeLow(int tupleQueueSizeLow) {
		this.tupleQueueSizeLow = tupleQueueSizeLow;
	}

	public int getTupleQueueSizeHigh() {
		return tupleQueueSizeHigh;
	}

	public void setTupleQueueSizeHigh(int tupleQueueSizeHigh) {
		this.tupleQueueSizeHigh = tupleQueueSizeHigh;
	}

	public String getCronExp() {
		return cronExp;
	}

	public void setCronExp(String cronExp) {
		this.cronExp = cronExp;
	}

	public DAG getDag() {
		return dag;
	}

	public void setDag(DAG dag) {
		this.dag = dag;
	}

	public List<String> getResourceUrls() {
		return resourceUrls;
	}

	public void setResourceUrls(List<String> resourceUrls) {
		this.resourceUrls = resourceUrls;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("state", state)
				.add("topologyId", topologyId)
				.add("name", name)
				.add("description", description)
				.add("tupleTimeout", tupleTimeout)
				.add("tupleQueueSizeLow", tupleQueueSizeLow)
				.add("tupleQueueSizeHigh", tupleQueueSizeHigh)
				.add("cronExp", cronExp)
				.add("dag", dag)
				.add("parallelism", parallelism)
				.add("threadParallelism", threadParallelism)
				.add("config", config)
				.add("resourceUrls", resourceUrls)
				.toString();
	}
}