package io.reactivej.dcf.common.info;

import com.google.common.base.MoreObjects;
import io.reactivej.ReactiveRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhh on 4/5/16.
 */
public class WorkerInfo implements Serializable {

    public static enum WorkerStatus {
        INIT, REGISTERED, UN_REGISTERED
    }

    private WorkerStatus status = WorkerStatus.INIT;

    private String id;
    private String host;
    private int port;
    private int cores;
    private long memory;
    private long memoryFree;
    private double cpuLoad;
    private int pid;
    private long startTime;

    private List<Integer> availablePort = new ArrayList<>();

    private transient long lastHeartbeat;
    private transient ReactiveRef endpoint;

    public WorkerInfo() {}

    public WorkerInfo(String id, String host, int port, int cores, long memory) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.cores = cores;
        this.memory = memory;
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public void setStatus(WorkerStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getMemoryFree() {
        return memoryFree;
    }

    public void setMemoryFree(long memoryFree) {
        this.memoryFree = memoryFree;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public List<Integer> getAvailablePort() {
        return availablePort;
    }

    public void setAvailablePort(List<Integer> availablePort) {
        this.availablePort = availablePort;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public ReactiveRef getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(ReactiveRef endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("id", id)
                .add("host", host)
                .add("port", port)
                .add("cores", cores)
                .add("memory", memory)
                .add("memoryFree", memoryFree)
                .add("cpuLoad", cpuLoad)
                .add("pid", pid)
                .add("startTime", startTime)
                .add("availablePort",availablePort)
                .toString();
    }
}
