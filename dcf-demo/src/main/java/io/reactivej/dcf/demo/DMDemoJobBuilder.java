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
public class DMDemoJobBuilder extends AbstractJobBuilder {

    public static final String mapper = "mapper";
    public static final String exec = "exec";
    public static final String totalNumKey = "totalNum";
    public static final String emitterDelay = "emitterDelay";

    @Override
    public Map<String, String> getJobParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(totalNumKey, "子任务总数");
        params.put(totalNumKey, "子任务产生的事件间隔，单位是millisecond");

        return params;
    }

    @Override
    public Topology createJobTopology() {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setEmitter(mapper, DataMigrationMockEmitter.class.getName());
        builder.setGear(exec, DataMigrationMockGear.class.getName()).meanGrouping(mapper, "jobShard");
        return builder.createTopology();
    }

    @Override
    public Topology configJobTopology(Topology topology, Map<String, String> params) {
        Map<String, String> defaultParmas = getJobDefaultParameters();

        String totalNumStr = params.get(totalNumKey) != null ? params.get(totalNumKey) : defaultParmas.get(totalNumKey);
        topology.getConfig().put(totalNumKey, Integer.parseInt(totalNumStr));

        String emitterDelayStr = params.get(emitterDelay) != null ? params.get(emitterDelay) : defaultParmas.get(emitterDelay);
        topology.getConfig().put(emitterDelay, Integer.parseInt(emitterDelayStr));

        return topology;
    }

    @Override
    public Map<String, String> getJobDefaultParameters() {
        Map<String, String> configs = Maps.newHashMap();
        configs.put(totalNumKey, Integer.toString(300));
        configs.put(emitterDelay, Integer.toString(200));

        return configs;
    }
}
