package io.reactivej.dcf.common.client;

import io.reactivej.dcf.common.info.LeaderState;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class AbstractJobListener implements JobListener {
    @Override
    public void onJobSubmitted(GlobalTopologyId jobId) {

    }

    @Override
    public void onJobStarted(GlobalTopologyId jobId) {

    }

    @Override
    public void onJobKilled(GlobalTopologyId jobId) {

    }

    @Override
    public void onJobFinished(GlobalTopologyId jobId, Serializable result) {

    }

    @Override
    public void onJobTerminated(GlobalTopologyId jobId) {

    }

    @Override
    public void onException(Exception e) {

    }
}
