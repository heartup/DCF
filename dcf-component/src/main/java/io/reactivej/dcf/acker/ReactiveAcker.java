package io.reactivej.dcf.acker;

import io.reactivej.dcf.common.component.IAcker;
import io.reactivej.dcf.common.info.AckerInfo;
import io.reactivej.dcf.common.info.AckerState;
import io.reactivej.dcf.common.info.LeaderInfo;
import io.reactivej.dcf.common.info.TupleAckInfo;
import io.reactivej.dcf.common.init.SystemConfig;
import io.reactivej.dcf.common.protocol.acker.*;
import io.reactivej.dcf.common.protocol.leader.*;
import io.reactivej.dcf.common.protocol.tuple.*;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ReactiveComponent;
import io.reactivej.ReactiveRef;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by heartup@gmail.com on 8/7/16.
 */
public class ReactiveAcker extends ReactiveComponent implements IAcker {
    private static Logger logger = LoggerFactory.getLogger(ReactiveAcker.class);

    private AckerState state = new AckerState();

    private ReactiveRef ackerMonitor;

    @Override
    public void preStart() {
        super.preStart();

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long startTime = runtimeMXBean.getStartTime();
        String name = runtimeMXBean.getName();
        Integer pid = Integer.valueOf(name.split("@")[0]);
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        AckerInfo ackerInfo = state.getAckerInfo();
        ackerInfo.setLastHeartbeat(new Date().getTime());
        ackerInfo.setMemoryMax(rt.maxMemory());
        ackerInfo.setMemoryUsed(usedMemory);
        ackerInfo.setPid(pid);
        ackerInfo.setStartTime(startTime);

        int heartbeatInterval = SystemConfig.getIntValue(SystemConfig.heartbeat_interval);
        getContext().getSystem().getScheduler().schedule(0, heartbeatInterval, getSelf(), new UpdateAckerInfo(), null);
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof SubmitTuple) {
                    onSubmitTuple((SubmitTuple) msg);
                } else if (msg instanceof AckTuple) {
                    onAckTuple((AckTuple) msg);
                } else if (msg instanceof CheckTupleTimeout) {
                    onCheckTupleTimeout((CheckTupleTimeout) msg);
                } else if (msg instanceof FailTuple) {
                    onFailTuple((FailTuple) msg);
                } else if (msg instanceof TopologyKilled) {
                    onTopologyKilled((TopologyKilled) msg);
                } else if (msg instanceof TopologyFinished) {
                    onTopologyFinished((TopologyFinished) msg);
                } else if (msg instanceof UpdateAckerInfo) {
                    onUpdateAckerInfo((UpdateAckerInfo) msg);
                } else if (msg instanceof SetAckerMonitor) {
                    onSetAckerMonitor((SetAckerMonitor) msg);
                } else if (msg instanceof RemoveAckerMonitor) {
                    onRemoveAckerMonitor((RemoveAckerMonitor) msg);
                } else if (msg instanceof RemoveAckerTopology) {
                    onRemoveAckerTopology((RemoveAckerTopology) msg);
                }
            }
        };
    }

    private void onUpdateAckerInfo(UpdateAckerInfo msg) {
        ackerInfoUpdated();
    }

    private void ackerInfoUpdated() {
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        AckerInfo info = state.getAckerInfo();
        info.setMemoryUsed(usedMemory);
        info.getPendingAckTupleCounts().clear();
        for (Map.Entry<GlobalTopologyId, Map<Serializable, TupleAckInfo>> e : state.getTopologyAckInfos().entrySet()) {
            GlobalTopologyId key = e.getKey();
            Long size = Long.valueOf(e.getValue().size());
            info.getPendingAckTupleCounts().put(key, size);
        }

        if (getAckerMonitor() != null) {
            getAckerMonitor().tell(new AckerInfoUpdated(info), getSelf());
        }
    }

    private void onTopologyFinished(TopologyFinished msg) {
        GlobalTopologyId id = msg.getTopologyId();
        removeTopologyInfo(id);
        ackerInfoUpdated();
    }

    private void onTopologyKilled(TopologyKilled msg) {
        GlobalTopologyId id = msg.getTopologyId();
        removeTopologyInfo(id);
        ackerInfoUpdated();
    }

    private void onRemoveAckerTopology(RemoveAckerTopology msg) {
        GlobalTopologyId id = msg.getTopologyId();
        removeTopologyInfo(id);
        ackerInfoUpdated();
    }

    private void removeTopologyInfo(GlobalTopologyId topologyId) {
        state.getTopologyAckInfos().remove(topologyId);
    }

    public void onCheckTupleTimeout(CheckTupleTimeout msg) {
        Map<Serializable, TupleAckInfo> ackInfos = state.getTupleAckInfos(msg.getTopologyId());
        if (ackInfos != null) {
            TupleAckInfo ackInfo = ackInfos.get(msg.getRootId());
            if (ackInfo != null) {
                timeoutTuple(ackInfo);
            }
        }
    }

    public void onAckTuple(AckTuple msg) {
        Map<Serializable, TupleAckInfo> ackInfos = state.getTupleAckInfos(msg.getTopologyId());
        if (ackInfos == null) {
            ackInfos = new HashMap<>();
            state.getTopologyAckInfos().put(msg.getTopologyId(), ackInfos);
        }

        for (Serializable rootId : msg.getRootIds()) {
            TupleAckInfo ackInfo = ackInfos.get(rootId);
            if (ackInfo == null) {
                ackInfos.put(rootId, new TupleAckInfo(msg.getTopologyId(), rootId, msg.getMsgId()));
            } else {
                long ack = ackInfo.getAck() ^ msg.getMsgId().hashCode();
                ackInfo.setAck(ack);
                if (ack == 0 && ackInfo.getEmitterTask() != null) {
                    finishTuple(ackInfo);
                }
            }
        }
    }

    public void onFailTuple(FailTuple msg) {
        Map<Serializable, TupleAckInfo> ackInfos = state.getTupleAckInfos(msg.getTopologyId());
        if (ackInfos == null) {
            ackInfos = new HashMap<>();
            state.getTopologyAckInfos().put(msg.getTopologyId(), ackInfos);
        }

        for (Serializable rootId : msg.getRootIds()) {
            TupleAckInfo ackInfo = ackInfos.get(rootId);
            if (ackInfo == null) {
                ackInfo = new TupleAckInfo(msg.getTopologyId(), rootId);
                ackInfos.put(rootId, ackInfo);
            }

            ackInfo.setFailed(true);
            ackInfo.setFailCause(msg.getCause());

            if (ackInfo.getTuple() != null && ackInfo.getEmitterTask() != null) {
                failTuple(ackInfo);
            }
        }
    }

    public void onSubmitTuple(SubmitTuple msg) {
        Map<Serializable, TupleAckInfo> ackInfos = state.getTupleAckInfos(msg.getTopologyId());
        if (ackInfos == null) {
            ackInfos = new HashMap<>();
            state.getTopologyAckInfos().put(msg.getTopologyId(), ackInfos);
        }

        for (Serializable rootId : msg.getRootIds()) {
            TupleAckInfo ackInfo = ackInfos.get(rootId);
            if (ackInfo == null) {
                ackInfo = new TupleAckInfo(msg.getTopologyId(), rootId);
                ackInfos.put(rootId, ackInfo);
            }

            ackInfo.setAck(ackInfo.getAck() ^ msg.getMsgId().hashCode());

            if (msg.getTuple() != null) {
                ackInfo.setTuple(msg.getTuple());
                ackInfo.setEmitterTask(getSender());

                if (ackInfo.isFailed()) {
                    failTuple(ackInfo);
                    return;
                }
                else if (ackInfo.getAck() != 0){
                    getContext().getSystem().getScheduler().scheduleOnce(msg.getTimeout(), getSelf(), new CheckTupleTimeout(msg.getTopologyId(), ackInfo.getRootId()), getSelf());
                }
            }


            if (ackInfo.getAck() == 0 && ackInfo.getEmitterTask() != null) {
                finishTuple(ackInfo);
            }
        }
    }

    private void finishTuple(TupleAckInfo ackInfo) {
        Map<Serializable, TupleAckInfo> ackInfos = state.getTupleAckInfos(ackInfo.getTopologyId());
        if (ackInfos != null) {
            ackInfos.remove(ackInfo.getRootId());
            ackInfo.getEmitterTask().tell(new FinishTuple(ackInfo.getTopologyId(), ackInfo.getTuple()), getSelf());
        }
    }

    private void timeoutTuple(TupleAckInfo ackInfo) {
        Map<Serializable, TupleAckInfo> ackInfos = state.getTupleAckInfos(ackInfo.getTopologyId());
        if (ackInfos != null) {
            ackInfos.remove(ackInfo.getRootId());
            ackInfo.getEmitterTask().tell(new TimeoutTuple(ackInfo.getTopologyId(), ackInfo.getTuple()), getSelf());
        }
    }

    private void failTuple(TupleAckInfo ackInfo) {
        Map<Serializable, TupleAckInfo> ackInfos = state.getTupleAckInfos(ackInfo.getTopologyId());
        if (ackInfos != null) {
            ackInfos.remove(ackInfo.getRootId());
            // 从acker传到emitter的FailTuple不需要指定rootIds，从task传到acker的消息才需要rootIds
            ackInfo.getEmitterTask().tell(new FailTuple(ackInfo.getTopologyId(), null, ackInfo.getTuple(), ackInfo.getFailCause()), getSelf());
        }
    }

    private void onSetAckerMonitor(SetAckerMonitor cmd) {
        setAckerMonitor(getSender());
        getAckerMonitor().tell(new AckerInfoUpdated(state.getAckerInfo()), getSelf());
    }

    private void onRemoveAckerMonitor(RemoveAckerMonitor cmd) {
        setAckerMonitor(null);
    }

    public ReactiveRef getAckerMonitor() {
        return ackerMonitor;
    }

    public void setAckerMonitor(ReactiveRef ackerMonitor) {
        this.ackerMonitor = ackerMonitor;
    }
}
