package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class FinishTask implements Serializable {
    private final long taskId;
    private final byte[] result;

    public FinishTask(long taskId, byte[] result) {
        this.taskId = taskId;
        this.result = result;
    }

    public long getTaskId() {
        return taskId;
    }

    public byte[] getResult() {
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskId", taskId)
                .toString();
    }
}