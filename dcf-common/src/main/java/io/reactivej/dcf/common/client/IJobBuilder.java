package io.reactivej.dcf.common.client;

import io.reactivej.dcf.common.topology.Topology;

import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public interface IJobBuilder {

    public Topology createJobTopology();

    public Topology configJobTopology(Topology topology, Map<String, String> params);

    public Map<String, String> getJobDefaultParameters();

    /**
     * 返回job的所有参数以及参数的描述
     * @return
     */
    public Map<String, String> getJobParameters();
}
