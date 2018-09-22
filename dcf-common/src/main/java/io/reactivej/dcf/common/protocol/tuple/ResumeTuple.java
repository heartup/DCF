package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.SystemMessage;

/**
 * Created by heartup@gmail.com on 8/7/16.
 */
public class ResumeTuple extends SystemMessage {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
