package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;
import io.reactivej.SystemMessage;

import java.io.Serializable;

/**
 * Created by heartup@gmail.com on 8/20/16.
 */
public class UpdateTaskInfo extends SystemMessage {
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
