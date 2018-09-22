package io.reactivej.dcf.common.info;

import io.reactivej.ReactiveRef;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.MessageId;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by lhh on 8/7/16.
 */
public class TupleAckInfo implements Serializable {

    private GlobalTopologyId topologyId;
    private Serializable rootId;
    private byte[] tuple;
    private Date createTime;  // used for timeout
    private boolean failed = false;
    private byte[] failCause;
    private long ack;  // the xor of hash code of related tuples

    private transient ReactiveRef emitterTask;

    public TupleAckInfo(GlobalTopologyId topologyId, Serializable rootId, MessageId msgId) {
        this.topologyId = topologyId;
        this.rootId = rootId;
        this.ack = msgId.hashCode();
    }

    public TupleAckInfo(GlobalTopologyId topologyId, Serializable rootId) {
        this.topologyId = topologyId;
        this.rootId = rootId;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public byte[] getTuple() {
        return tuple;
    }

    public Serializable getRootId() {
        return rootId;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isFailed() {
        return failed;
    }

    public byte[] getFailCause() {
        return failCause;
    }

    public void setFailCause(byte[] failCause) {
        this.failCause = failCause;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public long getAck() {
        return ack;
    }

    public void setAck(long ack) {
        this.ack = ack;
    }

    public ReactiveRef getEmitterTask() {
        return emitterTask;
    }

    public void setTuple(byte[] tuple) {
        this.tuple = tuple;
        this.createTime = new Date();
    }

    public void setEmitterTask(ReactiveRef emitterTask) {
        this.emitterTask = emitterTask;
    }
}
