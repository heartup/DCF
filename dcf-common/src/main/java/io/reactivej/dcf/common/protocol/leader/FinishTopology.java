package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;

public class FinishTopology extends TopologyCommand {

    private final byte[] result;

    public FinishTopology(GlobalTopologyId topologyId, byte[] result) {
        super(topologyId);
        this.result = result;
    }

    public byte[] getResult() {
        return result;
    }
}