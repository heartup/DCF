package io.reactivej.dcf.common.client;

import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public interface JobListener {

    public void onJobSubmitted(GlobalTopologyId jobId);

    public void onJobStarted(GlobalTopologyId jobId);

    /**
     * 运行被中断
     */
    public void onJobKilled(GlobalTopologyId jobId);

    /**
     * 运行正常停止
     * @param jobId
     */
    public void onJobFinished(GlobalTopologyId jobId, Serializable result);

    /**
     * 运行终止
     * @param jobId
     */
    public void onJobTerminated(GlobalTopologyId jobId);

    public void onException(Exception e);
}
