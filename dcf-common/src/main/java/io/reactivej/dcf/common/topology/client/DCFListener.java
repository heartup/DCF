package io.reactivej.dcf.common.topology.client;

import io.reactivej.dcf.common.info.*;
import io.reactivej.dcf.common.protocol.leader.TopologyMessage;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.List;

/***
 * @author heartup@gmail.com
 */
public interface DCFListener {

    public void onTopologySubmitted(Topology topology);

    public void onTopologyStarted(Topology topology);

    public void onLeaderStateUpdated(LeaderState state);

    public void onLeaderInfoUpdated(LeaderInfo info);

    public void onAckerInfoUpdated(AckerInfo info);

    public void onWorkerStateUpdated(WorkerState state);

    public void onTaskStateUpdated(TaskInfo taskInfo);

    public void onException(Throwable e);

    void onTopologyMessage(GlobalTopologyId topologyId, Serializable message);

    void onTopologyKilled(GlobalTopologyId topologyId, List<TaskInfo> topologyTasks);

    void onTopologyFinished(GlobalTopologyId topologyId, List<TaskInfo> topologyTasks, Serializable result);
}
