package io.reactivej.dcf.common.protocol.acker;

import com.google.common.base.MoreObjects;
import io.reactivej.SystemMessage;

/**
 * Created by lhh on 8/20/16.
 */
public class UpdateAckerInfo extends SystemMessage {
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
