package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.topology.GlobalTopologyId;

public class KillTopology extends TopologyCommand {

    public KillTopology(GlobalTopologyId topologyId) {
        super(topologyId);
    }
}