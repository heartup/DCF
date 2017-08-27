package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/***
 * @author heartup@gmail.com
 */
public class StartTask implements Serializable {

    private long taskId;

    public StartTask(long taskId) {
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
