package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.WorkerState;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class WorkerStateUpdated implements Serializable {
    private final WorkerState state;

    public WorkerStateUpdated(WorkerState state) {
        this.state = state;
    }

    public WorkerState getState() {
        return state;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("state", state)
                .toString();
    }
}
