package io.reactivej.dcf.common.topology.client;

import io.reactivej.dcf.common.info.LeaderInfo;
import io.reactivej.dcf.common.info.LeaderState;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.info.WorkerState;
import io.reactivej.dcf.common.protocol.leader.TopologyMessage;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public interface DCFListener {

    public void onTopologySubmitted(Topology topology);

    public void onTopologyStarted(Topology topology);

    /**
     * 运行被中断
      */
    public void onTopologyKilled(Topology topology);

    /**
     * 运行正常停止
     * @param topology
     */
    public void onTopologyFinished(Topology topology, Serializable result);

    /**
     * 运行终止
     * @param topology
     */
    public void onTopologyTerminated(Topology topology);

    public void onLeaderStateUpdated(LeaderState state);

    public void onLeaderInfoUpdated(LeaderInfo info);

    public void onWorkerStateUpdated(WorkerState state);

    public void onTaskStateUpdated(TaskInfo taskInfo);

    public void onException(Throwable e);

    void onTopologyMessage(GlobalTopologyId topologyId, Serializable message);
}
