package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.List;

/***
 * @author heartup@gmail.com
 */
public class TopologyKilled implements Serializable {
    private final List<TaskInfo> topologyTasks;
    private final GlobalTopologyId topologyId;

    public TopologyKilled(GlobalTopologyId topologyId, List<TaskInfo> topologyTasks) {
        this.topologyId = topologyId;
        this.topologyTasks = topologyTasks;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public List<TaskInfo> getTopologyTasks() {
        return topologyTasks;
    }
}
