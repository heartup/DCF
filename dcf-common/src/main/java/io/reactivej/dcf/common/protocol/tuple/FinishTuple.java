package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.ITuple;

import java.io.Serializable;

/**
 * @author heartup@gmail.com on 8/7/16.
 */
public class FinishTuple implements Serializable {
    private final GlobalTopologyId topologyId;
    private final byte[] tuple;

    public FinishTuple(GlobalTopologyId topologyId, byte[] tuple) {
        this.topologyId = topologyId;
        this.tuple =  tuple;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public byte[] getTuple() {
        return tuple;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .toString();
    }
}
