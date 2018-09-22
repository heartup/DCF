package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.ITuple;

import java.io.Serializable;

/**
 * Created by heartup@gmail.com on 8/7/16.
 */
public class NextTupleCreated implements Serializable {

    public NextTupleCreated() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}
