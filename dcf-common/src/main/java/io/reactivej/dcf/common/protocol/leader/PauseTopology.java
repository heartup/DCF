package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.topology.GlobalTopologyId;

public class PauseTopology extends TopologyCommand {

    public PauseTopology(GlobalTopologyId topologyId) {
        super(topologyId);
    }
}