package io.reactivej.dcf.common.protocol.leader;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class SetTopologyMonitor implements Serializable {
    private final GlobalTopologyId id;

    public SetTopologyMonitor(GlobalTopologyId id) {
        this.id = id;
    }

    public GlobalTopologyId getId() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
