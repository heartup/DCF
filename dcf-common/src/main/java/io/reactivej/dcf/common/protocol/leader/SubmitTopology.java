package io.reactivej.dcf.common.protocol.leader;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;

public class SubmitTopology implements Serializable {

    private final Topology topology;

    public SubmitTopology(Topology topology) {
        this.topology = topology;
    }

    public Topology getTopology() {
        return topology;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topology", topology)
                .toString();
    }
}