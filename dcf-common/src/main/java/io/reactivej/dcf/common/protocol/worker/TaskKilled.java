package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class TaskKilled implements Serializable {
    private long taskId;

    public TaskKilled(long taskId) {
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskId", taskId)
                .toString();
    }
}