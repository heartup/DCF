package io.reactivej.dcf.common.info;

import com.google.common.base.MoreObjects;
import io.reactivej.ReactiveRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author heartup@gmail.com on 4/5/16.
 */
public class LeaderInfo implements Serializable {
    private long memoryMax;
    private long memoryUsed;
    private int pid;
    private long startTime;

    private transient long lastHeartbeat;

    public LeaderInfo() {}

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

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Override
    public String toString() {
        return "LeaderInfo{" +
                "memoryMax=" + memoryMax +
                ", memoryUsed=" + memoryUsed +
                ", pid=" + pid +
                ", startTime=" + startTime +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}
