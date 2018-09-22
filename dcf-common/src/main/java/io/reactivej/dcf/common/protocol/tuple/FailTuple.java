package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;
import java.util.List;

/**
 * Created by heartup@gmail.com on 8/7/16.
 */
public class FailTuple implements Serializable {

    private final GlobalTopologyId topologyId;
    private final List<Serializable> rootIds;
    private final byte[] tuple;
    private final byte[] cause;

    public FailTuple(GlobalTopologyId topologyId, List<Serializable> rootIds, byte[] tuple, byte[] cause) {
        this.topologyId = topologyId;
        this.rootIds = rootIds;
        this.tuple = tuple;
        this.cause = cause;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public List<Serializable> getRootIds() {
        return rootIds;
    }

    public byte[] getTuple() {
        return tuple;
    }

    public byte[] getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyId", topologyId)
                .add("rootIds", rootIds)
                .toString();
    }
}
