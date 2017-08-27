package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class TaskStarted implements Serializable {

    private long taskId;

    public TaskStarted(long taskId) {
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