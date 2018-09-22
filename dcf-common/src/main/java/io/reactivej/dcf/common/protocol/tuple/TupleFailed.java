package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Root Tuple的Fail操作处理完毕
 *
 * Created by heartup@gmail.com on 8/7/16.
 */
public class TupleFailed implements Serializable {

    private final Serializable rootId;

    public TupleFailed(Serializable rootId) {
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
