package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;

/**
 * @author heartup@gmail.com on 8/7/16.
 */
public class CheckTupleTimeout implements Serializable {

    private final GlobalTopologyId topologyId;
    private final Serializable rootId;

    public CheckTupleTimeout(GlobalTopologyId topologyId, Serializable rootId) {
        this.topologyId = topologyId;
        this.rootId = rootId;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public Serializable getRootId() {
        return rootId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .add("rootId", rootId)
                .toString();
    }
}
