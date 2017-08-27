package io.reactivej.dcf.common.info;

import io.reactivej.dcf.common.protocol.tuple.ReceiveTuple;
import io.reactivej.ReactiveRef;
import io.reactivej.dcf.common.topology.IComponent;
import io.reactivej.dcf.common.topology.ITuple;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/***
 * @author heartup@gmail.com
 */
public class TaskState implements Serializable {

    private long taskId;
    private String persistId;

    private TaskInfo taskInfo;

    private Topology topology;

    private IComponent executableComponent;   // emitter 或 Gear如果支持序列话,那么task支持故障转移

    /**
     * 下面两个field会被多个component共享访问，考虑多线程安全策略
     */
    private volatile Map<Long, TaskInfo> topologyTasks;

    private Map<String, ITuple> pendingTuples = new ConcurrentHashMap<>();

    private Set<ReactiveRef> suspendedTask = new HashSet<ReactiveRef>();
    private List<ReceiveTuple> suspendedReceiveTuples = new LinkedList<ReceiveTuple>();
    private List<ReactiveRef> suspendedNextTuples = new LinkedList<>();

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    public IComponent getExecutableComponent() {
        return executableComponent;
    }

    public void setExecutableComponent(IComponent executableComponent) {
        this.executableComponent = executableComponent;
    }

    public void setTopologyTasks(Map<Long, TaskInfo> topologyTasks) {
        this.topologyTasks = topologyTasks;
    }

    public Map<Long, TaskInfo> getTopologyTasks() {
        return topologyTasks;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }

    public String getPersistId() {
        return persistId;
    }

    public void setPersistId(String persistId) {
        this.persistId = persistId;
    }

    public Set<ReactiveRef> getSuspendedTask() {
        return suspendedTask;
    }

    public List<ReceiveTuple> getSuspendedReceiveTuples() {
        return suspendedReceiveTuples;
    }

    public List<ReactiveRef> getSuspendedNextTuples() {
        return suspendedNextTuples;
    }

    public Map<String, ITuple> getPendingTuples() {
        return pendingTuples;
    }
}
