package io.reactivej.dcf.common.info;

import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lhh on 4/5/16.
 */
public class WorkerState implements Serializable {
    private WorkerInfo workerInfo = new WorkerInfo();

    private Map<GlobalTopologyId, Topology> topologys = new HashMap<>();
    /**
     * topologys所有相关的task的列表
     */
    private Map<Long, TaskInfo> tasks = new HashMap<>();
    // 在本机运行的tasks
    private Map<GlobalTopologyId, List<Long>> localTopologyTasks = new HashMap<>();
    private Map<GlobalTopologyId, List<Long>> topologyTasks = new HashMap<>();
    private List<Long> localTasks = new ArrayList<>();

    public WorkerInfo getWorkerInfo() {
        return workerInfo;
    }

    public void setWorkerInfo(WorkerInfo workerInfo) {
        this.workerInfo = workerInfo;
    }

    public Map<GlobalTopologyId, Topology> getTopologys() {
        return topologys;
    }

    public void setTopologys(Map<GlobalTopologyId, Topology> topologys) {
        this.topologys = topologys;
    }

    public Map<GlobalTopologyId, List<Long>> getLocalTopologyTasks() {
        return localTopologyTasks;
    }

    public Map<GlobalTopologyId, List<Long>> getTopologyTasks() {
        return topologyTasks;
    }

    public Map<Long, TaskInfo> getTasks() {
        return tasks;
    }

    public List<Long> getLocalTasks() {
        return localTasks;
    }
}
