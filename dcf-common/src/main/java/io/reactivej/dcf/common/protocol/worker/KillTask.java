package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;

import java.io.Serializable;

public class KillTask implements Serializable {
    private final TaskInfo taskInfo;
    private final TaskInfo.TaskLocation location;

    public KillTask(TaskInfo taskInfo, TaskInfo.TaskLocation location) {
        this.taskInfo = taskInfo;
        this.location = location;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public TaskInfo.TaskLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskInfo", taskInfo)
                .add("location", location)
                .toString();
    }
}