package io.reactivej.dcf.common.protocol.acker;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;

public class RemoveAckerTopology implements Serializable {

    private final GlobalTopologyId topologyId;

    public RemoveAckerTopology(GlobalTopologyId id) {
        this.topologyId = id;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .toString();
    }
}