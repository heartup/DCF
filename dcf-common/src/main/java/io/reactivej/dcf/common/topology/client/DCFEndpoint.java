package io.reactivej.dcf.common.topology.client;

import io.reactivej.dcf.common.protocol.acker.AckerInfoUpdated;
import io.reactivej.dcf.common.protocol.acker.RemoveAckerMonitor;
import io.reactivej.dcf.common.protocol.acker.RemoveAckerTopology;
import io.reactivej.dcf.common.protocol.acker.SetAckerMonitor;
import io.reactivej.dcf.common.protocol.leader.*;
import io.reactivej.dcf.common.protocol.task.TaskStateUpdated;
import io.reactivej.dcf.common.protocol.worker.RemoveWorkerMonitor;
import io.reactivej.dcf.common.protocol.worker.SetWorkerMonitor;
import io.reactivej.dcf.common.protocol.worker.WorkerStateUpdated;
import io.reactivej.*;
import io.reactivej.dcf.common.util.SerializeUtil;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class DCFEndpoint extends ReactiveComponent {

    private final DCFListener listener;
    private ReactiveRef clusterClient;

    public DCFEndpoint(DCFListener listener) {
        this.listener = listener;
    }

    @Override
    public void preStart() {
        super.preStart();
        this.clusterClient = getContext().createChild("clusterClient", true, ClusterClient.class.getName());
    }

    @Override
    public void onSupervise(SystemMessage msg) {
        if (msg instanceof Failure) {
            Throwable e = ((Failure) msg).getCause();
            if (e instanceof ClusterClient.SingletonNonExistException) {
                listener.onException(e);
                return;
            }
        }

        super.onSupervise(msg);
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof SubmitTopology ||
                        msg instanceof StartTopology ||
                        msg instanceof KillTopology ||
                        msg instanceof SetLeaderMonitor ||
                        msg instanceof RemoveLeaderMonitor ||
                        msg instanceof SetTopologyMonitor ||
                        msg instanceof RemoveTopologyMonitor) {
                    clusterClient.tell(new ClusterClient.ClusterMessage("leader", msg), getSelf());
                } else if (msg instanceof TopologySubmitted) {
                    listener.onTopologySubmitted(((TopologySubmitted) msg).getTopology());
                } else if (msg instanceof TopologyStarted) {
                    listener.onTopologyStarted(((TopologyStarted) msg).getTopology());
                } else if (msg instanceof TopologyKilled) {
                    listener.onTopologyKilled(((TopologyKilled) msg).getTopologyId(), ((TopologyKilled) msg).getTopologyTasks());
                } else if (msg instanceof TopologyFinished) {
                    Serializable result = SerializeUtil.deserialize(((TopologyFinished) msg).getResult(), getContext().getSystem().getSystemClassLoader());
                    listener.onTopologyFinished(((TopologyFinished) msg).getTopologyId(), ((TopologyFinished) msg).getTopologyTasks(), result);
                } else if (msg instanceof LeaderStateUpdated) {
                    listener.onLeaderStateUpdated(((LeaderStateUpdated) msg).getState());
                } else if (msg instanceof LeaderInfoUpdated) {
                    listener.onLeaderInfoUpdated(((LeaderInfoUpdated) msg).getInfo());
                } else if (msg instanceof WorkerStateUpdated) {
                    listener.onWorkerStateUpdated(((WorkerStateUpdated) msg).getState());
                } else if (msg instanceof TaskStateUpdated) {
                    listener.onTaskStateUpdated(((TaskStateUpdated) msg).getTaskInfo());
                }
                else if (msg instanceof SetWorkerMonitor) {
                    ReactiveRef workerRef = getContext().getSystem().findComponent(((SetWorkerMonitor) msg).getHost(),
                            ((SetWorkerMonitor) msg).getPort(), ReactiveSystem.componentSplitter + "worker");
                    workerRef.tell(msg, getSelf());
                } else if (msg instanceof RemoveWorkerMonitor) {
                    ReactiveRef workerRef = getContext().getSystem().findComponent(((RemoveWorkerMonitor) msg).getHost(),
                            ((RemoveWorkerMonitor) msg).getPort(), ReactiveSystem.componentSplitter + "worker");
                    workerRef.tell(msg, getSelf());
                } else if (msg instanceof FailMessage) {
                    Failure failure = SerializeUtil.deserialize(((FailMessage) msg).getFailure(),  getContext().getSystem().getSystemClassLoader());
                    listener.onException(failure.getCause());
                } else if (msg instanceof TopologyMessage) {
                    Serializable message = SerializeUtil.deserialize(((TopologyMessage) msg).getMessage(),  getContext().getSystem().getSystemClassLoader());
                    listener.onTopologyMessage(((TopologyMessage) msg).getTopologyId(), message);
                } else if (msg instanceof ResetLeader) {
                    clusterClient.tell(new ClusterClient.ClusterMessage("leader", msg), getSelf());
                } else if (msg instanceof SetAckerMonitor ||
                        msg instanceof RemoveAckerMonitor) {
                    clusterClient.tell(new ClusterClient.ClusterMessage("acker", msg), getSelf());
                } else if (msg instanceof AckerInfoUpdated) {
                    listener.onAckerInfoUpdated(((AckerInfoUpdated) msg).getInfo());
                } else if (msg instanceof RemoveAckerTopology) {
                    clusterClient.tell(new ClusterClient.ClusterMessage("acker", msg), getSelf());
                }
            }
        };
    }
}
