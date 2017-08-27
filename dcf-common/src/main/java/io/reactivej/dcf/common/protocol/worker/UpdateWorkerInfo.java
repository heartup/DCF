package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;
import io.reactivej.SystemMessage;

import java.io.Serializable;

/**
 * @author heartup@gmail.com on 8/20/16.
 */
public class UpdateWorkerInfo extends SystemMessage {
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
