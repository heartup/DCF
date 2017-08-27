package io.reactivej.dcf.demo;

import io.reactivej.dcf.common.topology.ITopologyContext;
import io.reactivej.dcf.common.topology.AbstractGear;
import io.reactivej.dcf.common.topology.IOutputFieldsDeclarer;
import io.reactivej.dcf.common.topology.ITuple;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author heartup@gmail.com on 8/13/16.
 */
public class PiStage3 extends AbstractGear {
    private Integer totalIts;
    private AtomicInteger totalRes = new AtomicInteger(0);
    private AtomicInteger curInts = new AtomicInteger(0);

    @Override
    public void prepare(Map<String, Serializable> conf, ITopologyContext context) throws Exception {
        super.prepare(conf, context);
        totalIts  = (Integer)conf.get(PiJobBuilder.totalKey);
    }

    @Override
    public void execute(ITuple input) throws Exception {
        int res = totalRes.addAndGet(input.getInteger(0));
        int cur = curInts.incrementAndGet();

        if (cur == totalIts.intValue()) {
            getContext().returnResult(4.0 * res / (totalIts - 1));
        }
    }

    @Override
    public void close() {

    }

    @Override
    public Map<String, Serializable> getComponentConfigration() {
        return null;
    }

    @Override
    public void defineOutputFields(IOutputFieldsDeclarer declarer) {

    }
}
