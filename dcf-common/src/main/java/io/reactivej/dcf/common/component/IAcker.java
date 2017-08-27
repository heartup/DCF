package io.reactivej.dcf.common.component;

import io.reactivej.dcf.common.protocol.tuple.AckTuple;
import io.reactivej.dcf.common.protocol.tuple.CheckTupleTimeout;
import io.reactivej.dcf.common.protocol.tuple.FailTuple;
import io.reactivej.dcf.common.protocol.tuple.SubmitTuple;

/**
 * @author heartup@gmail.com on 8/7/16.
 */
public interface IAcker {

    public void onSubmitTuple(SubmitTuple msg);

    public void onAckTuple(AckTuple msg);

    public void onCheckTupleTimeout(CheckTupleTimeout msg);

    public void onFailTuple(FailTuple msg);
}
