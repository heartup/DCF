package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;

import java.io.Serializable;

public class CreateTask implements Serializable {
    private final TaskInfo taskInfo;

    public CreateTask(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskInfo", taskInfo)
                .toString();
    }
}