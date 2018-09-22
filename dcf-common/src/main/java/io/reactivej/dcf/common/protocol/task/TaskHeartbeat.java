package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.SystemMessage;

import java.io.Serializable;

/**
 * Created by heartup@gmail.com on 8/20/16.
 */
public class TaskHeartbeat extends SystemMessage {
    private final TaskInfo taskInfo;

    public TaskHeartbeat(TaskInfo info) {
        this.taskInfo = info;
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
