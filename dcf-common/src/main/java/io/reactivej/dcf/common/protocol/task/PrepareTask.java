package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public class PrepareTask implements Serializable {

    private Topology topology;
    private long taskId;
    private Map<Long, TaskInfo> topologyTasks = new HashMap<>();

    public PrepareTask(Topology topology, long taskId) {
        this.topology = topology;
        this.taskId = taskId;
    }

    public Topology getTopology() {
        return topology;
    }

    public long getTaskId() {
        return taskId;
    }

    public Map<Long, TaskInfo> getTopologyTasks() {
        return topologyTasks;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topology", topology)
                .add("taskId", taskId)
                .add("topologyTasks", topologyTasks)
                .toString();
    }
}
