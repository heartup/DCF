package io.reactivej.dcf.common.protocol.leader;

import com.google.common.base.MoreObjects;
import io.reactivej.SystemMessage;

/**
 * @author heartup@gmail.com on 8/20/16.
 */
public class UpdateLeaderInfo extends SystemMessage {
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
