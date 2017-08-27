package io.reactivej.dcf.common.protocol.leader;

import io.reactivej.dcf.common.info.LeaderInfo;
import io.reactivej.dcf.common.info.LeaderState;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class LeaderInfoUpdated implements Serializable {

    private final LeaderInfo info;

    public LeaderInfoUpdated(LeaderInfo info) {
        this.info = info;
    }

    public LeaderInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "LeaderInfoUpdated{" +
                "info=" + info +
                '}';
    }
}
