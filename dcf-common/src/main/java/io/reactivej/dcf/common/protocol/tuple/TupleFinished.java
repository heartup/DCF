package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Root Tuple 最终被ack
 *
 * Created by heartup@gmail.com on 8/7/16.
 */
public class TupleFinished implements Serializable {

    private final Serializable rootId;

    public TupleFinished(Serializable rootId) {
        this.rootId = rootId;
    }

    public Serializable getRootId() {
        return rootId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rootId", rootId)
                .toString();
    }
}
