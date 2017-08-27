package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class TaskCreated implements Serializable {
    private long taskId;
    private final int pid;
    private final long startTime;
    private long memoryUsed;

    public TaskCreated(long taskId, int pid, long startTime, long usedMemory) {
        this.taskId = taskId;
        this.pid = pid;
        this.startTime = startTime;
        this.memoryUsed = usedMemory;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public int getPid() {
        return pid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskId", taskId)
                .add("pid", pid)
                .add("startTime", startTime)
                .add("memoryUsed", memoryUsed)
                .toString();
    }
}