package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Root Tuple最终超时而没有被处理
 *
 * @author heartup@gmail.com on 8/7/16.
 */
public class TupleTimeouted implements Serializable {

    private final Serializable rootId;

    public TupleTimeouted(Serializable rootId) {
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
