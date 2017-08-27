package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.info.LeaderState;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class LeaderStateUpdated implements Serializable {

    private final LeaderState state;

    public LeaderStateUpdated(LeaderState state) {
        this.state = state;
    }

    public LeaderState getState() {
        return state;
    }
}
