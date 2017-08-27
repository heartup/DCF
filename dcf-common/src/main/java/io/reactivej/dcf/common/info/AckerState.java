package io.reactivej.dcf.common.info;

import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heartup@gmail.com on 8/7/16.
 */
public class AckerState implements Serializable {

    private Map<GlobalTopologyId, Map<Serializable, TupleAckInfo>> topologyAckInfos = new HashMap<>();

    public Map<GlobalTopologyId, Map<Serializable, TupleAckInfo>> getTopologyAckInfos() {
        return topologyAckInfos;
    }

    public Map<Serializable, TupleAckInfo> getTupleAckInfos(GlobalTopologyId topologyId) {
        return topologyAckInfos.get(topologyId);
    }
}
