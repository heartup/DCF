package io.reactivej.dcf.common.info;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by heartup@gmail.com on 4/1/16.
 */
public class LeaderState implements Serializable {

    private Map<String, WorkerInfo> workers = new HashMap<String, WorkerInfo>();
    private Map<GlobalTopologyId, Topology> topologys = new HashMap<>();
    private Map<Long, TaskInfo> tasks = new HashMap<>();

    private ScheduleInfo schedule = new ScheduleInfo();

    private LeaderInfo leaderInfo = new LeaderInfo();

    private long lastTaskId = 1;

    public Map<String, WorkerInfo> getWorkers() {
        return workers;
    }

    public void setWorkers(Map<String, WorkerInfo> workers) {
        this.workers = workers;
    }

    public Map<GlobalTopologyId, Topology> getTopologys() {
        return topologys;
    }

    public void setTopologys(Map<GlobalTopologyId, Topology> topologys) {
        this.topologys = topologys;
    }

    public Map<Long, TaskInfo> getTasks() {
        return tasks;
    }

    public void setTasks(Map<Long, TaskInfo> tasks) {
        this.tasks = tasks;
    }

    public ScheduleInfo getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleInfo schedule) {
        this.schedule = schedule;
    }

    public LeaderInfo getLeaderInfo() {
        return leaderInfo;
    }

    public void setLeaderInfo(LeaderInfo leaderInfo) {
        this.leaderInfo = leaderInfo;
    }

    public long getLastTaskId() {
        return lastTaskId;
    }

    public void setLastTaskId(long lastTaskId) {
        this.lastTaskId = lastTaskId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("workers", "...")
                .add("topologys", "...")
                .add("tasks", "...")
                .add("schedule", "...")
                .add("leaderInfo", "...")
                .add("lastTaskId", lastTaskId)
                .toString();
    }
}
