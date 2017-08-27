package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.ITuple;

import java.io.Serializable;

/**
 * @author heartup@gmail.com on 8/6/16.
 */
public class SendTuple implements Serializable {

    private final ITuple tuple;

    public SendTuple(ITuple tuple) {
        this.tuple = tuple;
    }

    public ITuple getTuple() {
        return tuple;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tuple", tuple)
                .toString();
    }
}
