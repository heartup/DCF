package io.reactivej.dcf.acker;

import io.reactivej.dcf.common.component.IAcker;
import io.reactivej.dcf.common.info.AckerState;
import io.reactivej.dcf.common.info.TupleAckInfo;
import io.reactivej.dcf.common.protocol.leader.TopologyFinished;
import io.reactivej.dcf.common.protocol.leader.TopologyKilled;
import io.reactivej.dcf.common.protocol.tuple.*;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ReactiveComponent;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heartup@gmail.com on 8/7/16.
 */
public class ReactiveAcker extends ReactiveComponent implements IAcker {
    private static Logger logger = LoggerFactory.getLogger(ReactiveAcker.class);

    private AckerState state = new AckerState();

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
                }
            }
        };
    }

    private void onTopologyFinished(TopologyFinished msg) {
        GlobalTopologyId id = msg.getTopology().getTopologyId();
        removeTopologyInfo(id);
    }

    private void onTopologyKilled(TopologyKilled msg) {
        GlobalTopologyId id = msg.getTopology().getTopologyId();
        removeTopologyInfo(id);
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
}
