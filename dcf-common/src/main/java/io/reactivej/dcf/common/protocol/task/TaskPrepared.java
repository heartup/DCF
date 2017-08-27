package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;

import java.io.Serializable;

public class TaskPrepared implements Serializable {

    private long taskId;

    public TaskPrepared(long taskId) {
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