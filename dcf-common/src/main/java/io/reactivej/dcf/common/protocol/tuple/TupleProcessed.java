package io.reactivej.dcf.common.protocol.tuple;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.ITuple;

import java.io.Serializable;

/**
 * 此Tuple被处理完毕
 *
 * @author heartup@gmail.com on 8/7/16.
 */
public class TupleProcessed implements Serializable {

    private final ITuple tuple;

    public TupleProcessed(ITuple tuple) {
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
