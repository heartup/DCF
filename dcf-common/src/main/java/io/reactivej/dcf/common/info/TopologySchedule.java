package io.reactivej.dcf.common.info;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopologySchedule implements Serializable {
	String workerId;
	private Topology topology;
	private List<Long> task4Worker = new ArrayList<Long>();
	private Map<Long, TaskInfo> topologyTasks = new HashMap<>();

	public TopologySchedule() {
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public Topology getTopology() {
		return topology;
	}

	public void setTopology(Topology topology) {
		this.topology = topology;
	}

	public List<Long> getTask4Worker() {
		return task4Worker;
	}

	public void setTask4Worker(List<Long> task4Worker) {
		this.task4Worker = task4Worker;
	}

	public Map<Long, TaskInfo> getTopologyTasks() {
		return topologyTasks;
	}

	public void setTopologyTasks(Map<Long, TaskInfo> topologyTasks) {
		this.topologyTasks = topologyTasks;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("workerId", workerId)
				.add("topology", topology)
				.add("task4Worker", task4Worker)
				.add("topologyTasks", topologyTasks)
				.toString();
	}
}