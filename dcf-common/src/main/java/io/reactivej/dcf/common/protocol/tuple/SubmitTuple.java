package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.MessageId;

import java.io.Serializable;
import java.util.List;

/**
 * @author heartup@gmail.com on 8/7/16.
 */
public class SubmitTuple implements Serializable {

    private final GlobalTopologyId topologyId;
    private final List<Serializable> rootIds;
    private final MessageId msgId;
    private byte[] tuple;
    private long timeout;

    public SubmitTuple(GlobalTopologyId topologyId, List<Serializable> rootIds, MessageId msgId, long timeout) {
        this.topologyId = topologyId;
        this.rootIds = rootIds;
        this.msgId = msgId;
        this.timeout = timeout;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public MessageId getMsgId() {
        return msgId;
    }

    public List<Serializable> getRootIds() {
        return rootIds;
    }

    public byte[] getTuple() {
        return tuple;
    }

    public void setTuple(byte[] tuple) {
        this.tuple = tuple;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .add("rootIds", rootIds)
                .add("msgId", msgId)
                .add("tuple", tuple == null ? "null" : "...")
                .add("timeout", timeout)
                .toString();
    }
}
