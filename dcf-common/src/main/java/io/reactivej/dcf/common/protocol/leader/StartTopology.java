package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.topology.GlobalTopologyId;

public class StartTopology extends TopologyCommand {

    public StartTopology(GlobalTopologyId topologyId) {
        super(topologyId);
    }
}