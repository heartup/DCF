package io.reactivej.dcf.common.protocol.task;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.info.TaskInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public class TaskMigrated implements Serializable {

    private Map<Long, TaskInfo> topologyTasks = new HashMap<>();

    public TaskMigrated() {
    }

    public Map<Long, TaskInfo> getTopologyTasks() {
        return topologyTasks;
    }

    public void setTopologyTasks(Map<Long, TaskInfo> topologyTasks) {
        this.topologyTasks = topologyTasks;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("topologyTasks", topologyTasks)
                .toString();
    }
}
