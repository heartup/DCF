package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.MessageId;

import java.io.Serializable;
import java.util.List;

/**
 * @author heartup@gmail.com on 8/7/16.
 */
public class AckTuple implements Serializable {

    private final GlobalTopologyId topologyId;
    private final List<Serializable> rootIds;
    private final MessageId msgId;

    public AckTuple(GlobalTopologyId topologyId, List<Serializable> rootIds, MessageId msgId) {
        this.topologyId = topologyId;
        this.rootIds = rootIds;
        this.msgId = msgId;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public List<Serializable> getRootIds() {
        return rootIds;
    }

    public MessageId getMsgId() {
        return msgId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .add("rootIds", rootIds)
                .add("msgId", msgId)
                .toString();
    }
}
