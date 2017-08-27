package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkerRegister implements Serializable {
    private String workerId;
    private String host;
    private int port;
    private int cores;
    private int memory;
    private List<Integer> availablePort = new ArrayList<>();

    public WorkerRegister(String wid, String host, int port, int cores, int memory) {
        this.workerId = wid;
        this.host = host;
        this.port = port;
        this.cores = cores;
        this.memory = memory;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public List<Integer> getAvailablePort() {
        return availablePort;
    }

    public void setAvailablePort(List<Integer> availablePort) {
        this.availablePort = availablePort;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("workerId", workerId)
                .add("host", host)
                .add("port", port)
                .add("cores", cores)
                .add("memory", memory)
                .add("availablePort", availablePort)
                .toString();
    }
}