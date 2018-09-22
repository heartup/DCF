package io.reactivej.dcf.common.protocol.leader;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;

/**
 * Created by heartup@gmail.com on 11/18/16.
 */
public class TopologyMessage implements Serializable {
    private final GlobalTopologyId topologyId;
    private final byte[] message;

    public TopologyMessage(GlobalTopologyId topologyId, byte[] msg) {
        this.topologyId = topologyId;
        this.message = msg;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public byte[] getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .toString();
    }
}
