package io.reactivej.dcf.demo;

import com.google.common.collect.Maps;
import io.reactivej.dcf.common.client.AbstractJobBuilder;
import io.reactivej.dcf.common.topology.Topology;
import io.reactivej.dcf.common.topology.builder.TopologyBuilder;

import java.util.HashMap;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public class PiJobBuilder extends AbstractJobBuilder {

    public static String totalKey = "total";
    public static final String stage1 = "stage1";
    public static final String stage2 = "stage2";
    public static final String stage3 = "stage3";

    @Override
    public Map<String, String> getJobParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(totalKey, "迭代次数");

        return params;
    }

    @Override
    public Topology createJobTopology() {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setEmitter(stage1, PiStage1.class.getName());
        builder.setGear(stage2, PiStage2.class.getName()).meanGrouping(stage1, "shard");
        builder.setGear(stage3, PiStage3.class.getName()).globalGrouping(stage2, "result");

        Topology topo = builder.createTopology();
        return topo;
    }

    @Override
    public Topology configJobTopology(Topology topology, Map<String, String> params) {
        Map<String, String> defaultParmas = getJobDefaultParameters();

        String totalStr = params.get(totalKey) != null ? params.get(totalKey) : defaultParmas.get(totalKey);
        topology.getConfig().put(totalKey, Integer.parseInt(totalStr));

        return topology;
    }

    @Override
    public Map<String, String> getJobDefaultParameters() {
        Map<String, String> defaults = Maps.newHashMap();
        defaults.put(tupleTimeoutKey, Long.toString(350000000));
        defaults.put(stage1 + memorySuffix, Long.toString(500 * 1024 * 1024));
        defaults.put(stage1 + coreSuffix, Integer.toString(1));
        defaults.put(stage1 + parallelismSuffix, Integer.toString(1));
        defaults.put(stage1 + threadSuffix, Integer.toString(1));

        defaults.put(stage2 + memorySuffix, Long.toString(500 * 1024 * 1024));
        defaults.put(stage2 + coreSuffix, Integer.toString(1));
        defaults.put(stage2 + parallelismSuffix, Integer.toString(2));
        defaults.put(stage2 + threadSuffix, Integer.toString(1));

        defaults.put(stage3 + memorySuffix, Long.toString(500 * 1024 * 1024));
        defaults.put(stage3 + coreSuffix, Integer.toString(1));
        defaults.put(stage3 + parallelismSuffix, Integer.toString(1));
        defaults.put(stage3 + threadSuffix, Integer.toString(1));

        defaults.put(totalKey, "90000");

        return defaults;
    }
}
