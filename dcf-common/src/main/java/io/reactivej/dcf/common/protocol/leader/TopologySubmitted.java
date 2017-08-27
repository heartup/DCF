package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class TopologySubmitted implements Serializable {
    private final Topology topology;

    public TopologySubmitted(Topology topology) {
        this.topology = topology;
    }

    public Topology getTopology() {
        return topology;
    }
}
