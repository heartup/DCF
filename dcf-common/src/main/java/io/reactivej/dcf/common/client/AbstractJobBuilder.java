package io.reactivej.dcf.common.client;

import com.google.common.collect.Lists;
import io.reactivej.dcf.common.topology.IComponentDescription;
import io.reactivej.dcf.common.topology.Topology;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
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
        return configJobTopology(topology, params);
    }

    @Override
    public Map<String, String> getJobDefaultParameters() {
        Map<String, String> defaultParams = new HashMap<String, String>();

        defaultParams.put(tupleTimeoutKey, "300000000000");
        defaultParams.put(tupleQueueSizeLowKey, "500");
        defaultParams.put(tupleQueueSizeHighKey, "800");

        return defaultParams;
    }

    private void mergeMap(Map<String, String> target, Map<String, String> conf) {
        for (Map.Entry<String, String> e : conf.entrySet()) {
            String k = e.getKey();
            if (target.get(k) == null) {
                target.put(k, e.getValue());
            }
        }
    }

    @Override
    public Topology configJobTopology(Topology topology, Map<String, String> params) {
        // 和默认值进行合并
        mergeMap(params, getJobDefaultParameters());

        // 设置所有job的通用属性
        for (String compName : topology.getDag().getComponents().keySet()) {
            String memKey = compName + memorySuffix;
            String coreKey = compName + coreSuffix;
            String paralKey = compName + parallelismSuffix;
            String threadKey = compName + threadSuffix;
            String locKey = compName + locationSuffix;

            String memStr = params.get(memKey);
            String coreStr = params.get(coreKey);
            String paralStr = params.get(paralKey);
            String threadStr = params.get(threadKey);
            String locStr = params.get(locKey);

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

            params.remove(memKey);
            params.remove(coreKey);
            params.remove(paralKey);
            params.remove(threadKey);
            params.remove(locKey);
        }

        String tupleTimeoutStr = params.get(tupleTimeoutKey);
        if (tupleTimeoutStr != null) {
            topology.setTupleTimeout(Long.parseLong(tupleTimeoutStr));
        }

        String tupleQueueSizeLowStr = params.get(tupleQueueSizeLowKey);
        if (tupleQueueSizeLowStr != null) {
            topology.setTupleQueueSizeLow(Integer.parseInt(tupleQueueSizeLowStr));
        }

        String tupleQueueSizeHighStr = params.get(tupleQueueSizeHighKey);
        if (tupleQueueSizeHighStr != null) {
            topology.setTupleQueueSizeHigh(Integer.parseInt(tupleQueueSizeHighStr));
        }

        params.remove(tupleTimeoutKey);
        params.remove(tupleQueueSizeLowKey);
        params.remove(tupleQueueSizeHighKey);

        // 其他属性按照String类型先储存
        for (String k: params.keySet()) {
            topology.getConfig().put(k, params.get(k));
        }

        return topology;
    }
}
