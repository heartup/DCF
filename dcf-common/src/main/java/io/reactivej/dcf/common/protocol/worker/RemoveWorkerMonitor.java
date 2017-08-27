package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class RemoveWorkerMonitor implements Serializable {
    private final String workerId;
    private final String host;
    private final int port;

    public RemoveWorkerMonitor(String workerId, String host, int port) {
        this.workerId = workerId;
        this.host = host;
        this.port = port;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("workerId", workerId)
                .add("host", host)
                .add("port", port)
                .toString();
    }
}
