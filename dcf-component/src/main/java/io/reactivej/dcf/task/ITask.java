package io.reactivej.dcf.task;

import io.reactivej.dcf.common.protocol.task.PrepareTask;
import io.reactivej.dcf.common.protocol.task.StartTask;
import io.reactivej.dcf.common.protocol.tuple.NextTupleCreated;
import io.reactivej.dcf.common.protocol.tuple.ReceiveTuple;
import io.reactivej.dcf.common.protocol.tuple.TupleProcessed;

/**
 * Created by heartup@gmail.com on 8/7/16.
 */
public interface ITask {

    public void onStartTask(StartTask msg);

    public void onPrepareTask(PrepareTask msg);

    public void onRecieveTuple(ReceiveTuple msg);

    public void onTupleProcessed(TupleProcessed msg);

    public void onNextTupleCreated(NextTupleCreated msg);
}
