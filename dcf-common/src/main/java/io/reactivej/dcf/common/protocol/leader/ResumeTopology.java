package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.topology.GlobalTopologyId;

public class ResumeTopology extends TopologyCommand {

    public ResumeTopology(GlobalTopologyId topologyId) {
        super(topologyId);
    }
}