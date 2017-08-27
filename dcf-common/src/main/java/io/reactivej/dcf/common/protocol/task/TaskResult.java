package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;

import java.io.Serializable;

/**
 * @author heartup@gmail.com on 8/20/16.
 */
public class TaskResult implements Serializable {
    // this is task result
    private final TaskInfo taskInfo;
    // this is job result
    private final byte[] result;

    public TaskResult(TaskInfo info, byte[] result) {
        this.taskInfo = info;
        this.result = result;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public byte[] getResult() {
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskInfo", taskInfo)
                .toString();
    }
}
