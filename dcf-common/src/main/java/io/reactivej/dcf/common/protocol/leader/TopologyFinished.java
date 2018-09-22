package io.reactivej.dcf.common.protocol.leader;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class TopologyFinished implements Serializable {

    private final List<TaskInfo> topologyTasks;
    private final byte[] result;
    private final GlobalTopologyId topologyId;

    public TopologyFinished(GlobalTopologyId topologyId, List<TaskInfo> topologyTasks, byte[] result) {
        this.topologyId = topologyId;
        this.topologyTasks = topologyTasks;
        this.result = result;
    }

    public List<TaskInfo> getTopologyTasks() {
        return topologyTasks;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public byte[] getResult() {
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .add("topologyTasks", Arrays.toString(topologyTasks.toArray()))
                .toString();
    }
}