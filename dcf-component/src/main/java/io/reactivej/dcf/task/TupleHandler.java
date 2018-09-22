package io.reactivej.dcf.task;

import io.reactivej.dcf.common.protocol.tuple.*;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ReactiveComponent;
import io.reactivej.dcf.common.topology.*;
import io.reactivej.dcf.common.util.SerializeUtil;
import org.apache.commons.lang3.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by heartup@gmail.com on 8/6/16.
 */
public class TupleHandler extends ReactiveComponent {
    private static Logger logger = LoggerFactory.getLogger(TupleHandler.class);

    private final IComponent executableComponent;

    public TupleHandler(IComponent executableComponent) {
        this.executableComponent = executableComponent;
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof ReceiveTuple) {
                    onRecieveTuple((ReceiveTuple) msg);
                } else if (msg instanceof NextTuple) {
                    onNextTuple((NextTuple) msg);
                } else if (msg instanceof FinishTuple) {
                    onFinishTuple((FinishTuple) msg);
                } else if (msg instanceof TimeoutTuple) {
                    onTimeoutTuple((TimeoutTuple) msg);
                } else if (msg instanceof FailTuple) {
                    onFailTuple((FailTuple) msg);
                }
            }
        };
    }

    private void onFinishTuple(FinishTuple msg) {
        IEmitter emitter = (IEmitter) executableComponent;

        ITuple tuple = SerializeUtil.deserialize(msg.getTuple(), getContext().getSystem().getSystemClassLoader());

        emitter.ack(tuple);
        getSender().tell(new TupleFinished(tuple.getRootIds().get(0)), getSelf());
    }

    private void onFailTuple(FailTuple msg) {
        IEmitter emitter = (IEmitter) executableComponent;

        ITuple tuple = SerializeUtil.deserialize(msg.getTuple(), getContext().getSystem().getSystemClassLoader());

        Throwable cause = null;
        if (msg.getCause() != null) {
            cause = SerializeUtil.deserialize(msg.getCause(), getContext().getSystem().getSystemClassLoader());
        }
        emitter.fail(tuple, cause);

        // emitter这里，rootIds只可能是一个（其他内部阶段，rootIds在多个anchor的情况下可能是多个）
        getSender().tell(new TupleFailed(tuple.getRootIds().get(0)), getSelf());
    }

    public void onTimeoutTuple(TimeoutTuple msg) {
        IEmitter emitter = (IEmitter) executableComponent;
        ITuple tuple = SerializeUtil.deserialize(msg.getTuple(), getContext().getSystem().getSystemClassLoader());

        emitter.timeout(tuple);
        getSender().tell(new TupleTimeouted(tuple.getRootIds().get(0)), getSelf());
    }

    public void onNextTuple(NextTuple msg) throws Exception {
        IEmitter emitter = (IEmitter) executableComponent;
        if (emitter.nextTuple()) {
            // 如果数据流终止，不再产生后续的NextTuple请求
            getSender().tell(new NextTupleCreated(), getSelf());
        }
    }

    public void onRecieveTuple(ReceiveTuple msg) throws Exception {
        IGear gear = (IGear) executableComponent;
        gear.execute(msg.getTuple());
        getSender().tell(new TupleProcessed(msg.getTuple()), getSelf());
    }
}
