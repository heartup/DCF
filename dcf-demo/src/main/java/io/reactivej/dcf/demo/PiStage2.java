package io.reactivej.dcf.demo;

import io.reactivej.dcf.common.topology.AbstractGear;
import io.reactivej.dcf.common.topology.Fields;
import io.reactivej.dcf.common.topology.IOutputFieldsDeclarer;
import io.reactivej.dcf.common.topology.ITuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author heartup@gmail.com on 8/13/16.
 */
public class PiStage2 extends AbstractGear {
    @Override
    public void execute(ITuple input) throws Exception {
        double x = Math.random();
        double y = Math.random();
        x = x * 2 - 1;
        y = y * 2 - 1;
        int res = (x * x + y * y < 1) ? 1 : 0;

        List<Serializable> resVal = new ArrayList<Serializable>();
        resVal.add(res);
        resVal.add(1);
        getContext().send("result", resVal);
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
        declarer.declare("result", new Fields("res", "its"));
    }
}
