package io.reactivej.dcf.common.protocol.worker;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.info.WorkerInfo;
import io.reactivej.SystemMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WorkerHeartbeat extends SystemMessage {
    private WorkerInfo workerInfo;

    // 本机的task的信息
    private Map<Long, TaskInfo> tasks = new HashMap<>();

    public WorkerHeartbeat(WorkerInfo workerInfo) {
        this.workerInfo = workerInfo;
    }

    public WorkerInfo getWorkerInfo() {
        return workerInfo;
    }

    public void setWorkerInfo(WorkerInfo workerInfo) {
        this.workerInfo = workerInfo;
    }

    public Map<Long, TaskInfo> getTasks() {
        return tasks;
    }

    public void setTasks(Map<Long, TaskInfo> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("workerInfo", workerInfo)
                .add("tasks", tasks)
                .toString();
    }
}