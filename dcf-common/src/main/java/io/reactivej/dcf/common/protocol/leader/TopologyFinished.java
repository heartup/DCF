package io.reactivej.dcf.common.protocol.leader;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;

public class TopologyFinished implements Serializable {

    private final Topology topology;
    private final byte[] result;

    public TopologyFinished(Topology topology, byte[] result) {
        this.topology = topology;
        this.result = result;
    }

    public Topology getTopology() {
        return topology;
    }

    public byte[] getResult() {
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topology", topology)
                .toString();
    }
}