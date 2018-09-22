package io.reactivej.dcf.common.info;

import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by heartup@gmail.com on 4/5/16.
 */
public class AckerInfo implements Serializable {
    private long memoryMax;
    private long memoryUsed;
    private int pid;
    private long startTime;
    private Map<GlobalTopologyId, Long> pendingAckTupleCounts = new HashMap<>();

    private transient long lastHeartbeat;

    public AckerInfo() {}

    public long getMemoryMax() {
        return memoryMax;
    }

    public void setMemoryMax(long memoryMax) {
        this.memoryMax = memoryMax;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
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

    public Map<GlobalTopologyId, Long> getPendingAckTupleCounts() {
        return pendingAckTupleCounts;
    }

    public void setPendingAckTupleCounts(Map<GlobalTopologyId, Long> pendingAckTupleCounts) {
        this.pendingAckTupleCounts = pendingAckTupleCounts;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Override
    public String toString() {
        return "AckerInfo{" +
                "memoryMax=" + memoryMax +
                ", memoryUsed=" + memoryUsed +
                ", pid=" + pid +
                ", startTime=" + startTime +
                ", pendingAckTupleCounts=" + pendingAckTupleCounts +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}
