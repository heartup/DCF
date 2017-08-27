package io.reactivej.dcf.demo;

import io.reactivej.dcf.common.topology.ITopologyContext;
import io.reactivej.dcf.common.topology.AbstractEmitter;
import io.reactivej.dcf.common.topology.Fields;
import io.reactivej.dcf.common.topology.IOutputFieldsDeclarer;
import io.reactivej.dcf.common.topology.ITuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author heartup@gmail.com on 8/13/16.
 */
public class PiStage1 extends AbstractEmitter {

    private Integer shardIts;
    private AtomicInteger curIt = new AtomicInteger(0);

    @Override
    public void open(Map<String, Serializable> conf, ITopologyContext context) throws Exception {
        super.open(conf, context);
        shardIts  = (Integer)conf.get(PiJobBuilder.totalKey);
    }

    /**
     * 产生一次计算任务，在集群中被并行调用，但是一般情况下的处理逻辑是做计算任务切分，在下一个计算逻辑中并行执行
     * 在产生计算任务的时候可以指定id(使用OutputSender的 List<Long> send(List<Serializable> newValues, MessageId msgId))
     * 方便在ack或fail中得知某个计算任务是否被正确处理完毕
     * <p>
     * InterruptedException应该被抛出，以支持Topology的终止操作
     */
    @Override
    public boolean nextTuple() throws Exception {
        if (curIt.incrementAndGet() <= shardIts.intValue()) {
            List<Serializable> vals = new ArrayList<Serializable>();
            vals.add(1);
            getContext().send("shard", vals);

            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 通知计算逻辑指定的tuple（包含msgId，和values）已经被正确处理完毕
     * 虽然Emitter在集群中并行执行，但是从某个Emitter发出的Tuple保证会由自身接受ack或fail通知
     *
     * @param tuple
     */
    @Override
    public void ack(ITuple tuple) {

    }

    /**
     * 通知计算逻辑指定的tuple（包含msgId，和values）已经处理超时或者出现错误
     * 虽然Emitter在集群中并行执行，但是从某个Emitter发出的Tuple保证会由自身接受ack或fail通知
     *
     * @param tuple
     * @param cause
     */
    @Override
    public void fail(ITuple tuple, Throwable cause) {

    }

    @Override
    public void timeout(ITuple tuple) {

    }

    /**
     * 当计算任务被终止是被调用，可以在此做清理工作
     */
    @Override
    public void close() {

    }

    @Override
    public Map<String, Serializable> getComponentConfigration() {
        return null;
    }

    /**
     * @param declarer
     * @Title: defineOutputFields
     * @Description: 定义流的输出field, 用于流的分组
     * 根据传入的streamId, 找到对应的fields
     * send的时候，会根据该流的fields值，算出hash发给不同的worker
     * 如果不指定，则随机选一个worker发送一次
     * @return: void
     */
    @Override
    public void defineOutputFields(IOutputFieldsDeclarer declarer) {
        declarer.declare("shard", new Fields("shardIts"));
    }
}
