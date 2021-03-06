package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class TopologyKilled implements Serializable {
    private final Topology topology;

    public TopologyKilled(Topology topology) {
        this.topology = topology;
    }

    public Topology getTopology() {
        return topology;
    }
}
