package io.reactivej.dcf.common.client;

import com.google.common.collect.Lists;
import io.reactivej.dcf.common.topology.IComponentDescription;
import io.reactivej.dcf.common.topology.Topology;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public abstract class AbstractJobBuilder implements IJobBuilder {

    public static final String tupleTimeoutKey = "tupleTimeout";
    public static final String tupleQueueSizeLowKey = "tupleQueueSizeLow";
    public static final String tupleQueueSizeHighKey = "tupleQueueSizeHigh";
    public static final String memorySuffix = ".memory";
    public static final String coreSuffix = ".core";
    public static final String parallelismSuffix = ".parallelism";
    public static final String threadSuffix = ".thread";
    public static final String locationSuffix = ".location";

    public Topology createJobTopology(Map<String, String> params) {
        Topology topology = createJobTopology();
        Map<String, String> defaultParams = getJobDefaultParameters();

        for (String compName : topology.getDag().getComponents().keySet()) {
            String memKey = compName + memorySuffix;
            String coreKey = compName + coreSuffix;
            String paralKey = compName + parallelismSuffix;
            String threadKey = compName + threadSuffix;
            String locKey = compName + locationSuffix;

            String memStr = params.get(memKey) != null ? params.get(memKey) : defaultParams.get(memKey);
            String coreStr = params.get(coreKey) != null ? params.get(coreKey) : defaultParams.get(coreKey);
            String paralStr = params.get(paralKey) != null ? params.get(paralKey) : defaultParams.get(paralKey);
            String threadStr = params.get(threadKey) != null ? params.get(threadKey) : defaultParams.get(threadKey);
            String locStr = params.get(locKey) != null ? params.get(locKey) : defaultParams.get(locKey);

            IComponentDescription compDefine = topology.getDag().getComponent(compName);

            if (StringUtils.isNotEmpty(memStr)) {
                Long memory = Long.parseLong(memStr);
                compDefine.setMemoryNeeded(memory);
            }
            if (StringUtils.isNotEmpty(coreStr)) {
                Integer core = Integer.parseInt(coreStr);
                compDefine.setCoreNeeded(core);
            }
            if (StringUtils.isNotEmpty(locStr)) {
                compDefine.setLocation(locStr.trim());
            }
            if (paralStr != null) {
                Integer paral = Integer.parseInt(paralStr);
                topology.getParallelism().put(compName, paral);
            }
            if (threadStr != null) {
                Integer thread = Integer.parseInt(threadStr);
                topology.getThreadParallelism().put(compName, thread);
            }
        }

        String tupleTimeoutStr = params.get(tupleTimeoutKey) != null ? params.get(tupleTimeoutKey) :
                defaultParams.get(tupleTimeoutKey);

        if (tupleTimeoutStr != null) {
            topology.setTupleTimeout(Long.parseLong(tupleTimeoutStr));
        }
        else {
            topology.setTupleTimeout(300000000000L);
        }

        String tupleQueueSizeLowStr = params.get(tupleQueueSizeLowKey) != null ? params.get(tupleQueueSizeLowKey) :
                defaultParams.get(tupleQueueSizeLowKey);

        if (tupleQueueSizeLowStr != null) {
            topology.setTupleQueueSizeLow(Integer.parseInt(tupleQueueSizeLowStr));
        }
        else {
            topology.setTupleQueueSizeLow(500);
        }

        String tupleQueueSizeHighStr = params.get(tupleQueueSizeHighKey) != null ? params.get(tupleQueueSizeHighKey) :
                defaultParams.get(tupleQueueSizeHighKey);

        if (tupleQueueSizeHighStr != null) {
            topology.setTupleQueueSizeHigh(Integer.parseInt(tupleQueueSizeHighStr));
        }
        else {
            topology.setTupleQueueSizeHigh(800);
        }

        return configJobTopology(topology, params);
    }

    @Override
    public Topology configJobTopology(Topology topology, Map<String, String> params) {
        for (String k: params.keySet()) {
            topology.getConfig().put(k, params.get(k));
        }

        return topology;
    }
}
