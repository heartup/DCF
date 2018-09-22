package io.reactivej.dcf.common.info;

import com.google.common.base.MoreObjects;
import io.reactivej.ReactiveRef;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by heartup@gmail.com on 4/1/16.
 */
public class TaskInfo implements Serializable {
    public static enum TaskStatus {
        INIT, PREPARED, STARTED, SUSPENDED, MIGRATING, FINISHED, KILLED, STOPPED, TERMINATED
    }

    public static enum TaskType {
        EMITTER, GEAR
    }

    /**
     * TASK 失效后的策略，重启或是迁移到另一台worker执行
     */
    public static enum FailStrategy {
        /**
         * fail 后，通知job中的其他task，已经fail，不再给他发送tuple即可
         */
        NONE,
        /**
         * 状态存储在本地的task
         */
        RESTART,
        /**
         * 状态存储在共享存储的task
         */
        MIGRATE
    }

    public static class TaskLocation implements Serializable {
        private String workerId;
        private String host;
        private int port;
        private int pid;
        private long startTime;

        public String getWorkerId() {
            return workerId;
        }

        public void setWorkerId(String workerId) {
            this.workerId = workerId;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public int getPid() {
            return pid;
        }

        public void setPid(int pid) {
            this.pid = pid;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("workerId", workerId)
                    .add("host", host)
                    .add("port", port)
                    .add("pid", pid)
                    .add("startTime", startTime)
                    .toString();
        }
    }

    public static class TupleInfo implements Serializable {
        private long recieved;
        private long processed;
        // above is for gear
        private AtomicLong produced = new AtomicLong(0);   // both emitter and gear can produce tuple
        // below is for emitter
        private long acked;
        private long timeouted;
        private long failed;

        public long getRecieved() {
            return recieved;
        }

        public void setRecieved(long recieved) {
            this.recieved = recieved;
        }

        public long getProcessed() {
            return processed;
        }

        public void setProcessed(long processed) {
            this.processed = processed;
        }

        public AtomicLong getProduced() {
            return produced;
        }

        public void setProduced(AtomicLong produced) {
            this.produced = produced;
        }

        public long getAcked() {
            return acked;
        }

        public void setAcked(long acked) {
            this.acked = acked;
        }

        public long getTimeouted() {
            return timeouted;
        }

        public void setTimeouted(long timeouted) {
            this.timeouted = timeouted;
        }

        public long getFailed() {
            return failed;
        }

        public void setFailed(long failed) {
            this.failed = failed;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("recieved", recieved)
                    .add("processed", processed)
                    .add("produced", produced)
                    .add("acked", acked)
                    .add("timeouted", timeouted)
                    .add("failed", failed)
                    .toString();
        }
    }

    private TaskStatus status = TaskStatus.INIT;
    private long taskId;
    private TaskType taskType = TaskType.GEAR;
    private long memoryNeeded;
    private int coreNeeded;
    private long memoryUsed;
    private GlobalTopologyId topologyId;
    private String componentId;
    private TaskLocation location;
    private TupleInfo tupleInfo = new TupleInfo();
    // 默认fail策略设置为迁移
    private FailStrategy failStrategy = FailStrategy.NONE;
    private List<TaskLocation> failHistory = new ArrayList<>();

    private transient long lastHeartbeat;
    private transient ReactiveRef endpoint;

    public TaskInfo(long taskId, GlobalTopologyId topologyId, String componentId) {
        this.taskId = taskId;
        this.topologyId = topologyId;
        this.componentId = componentId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public long getMemoryNeeded() {
        return memoryNeeded;
    }

    public void setMemoryNeeded(long memoryNeeded) {
        this.memoryNeeded = memoryNeeded;
    }

    public int getCoreNeeded() {
        return coreNeeded;
    }

    public void setCoreNeeded(int coreNeeded) {
        this.coreNeeded = coreNeeded;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public GlobalTopologyId getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(GlobalTopologyId topologyId) {
        this.topologyId = topologyId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public TaskLocation getLocation() {
        return location;
    }

    public void setLocation(TaskLocation location) {
        this.location = location;
    }

    public TupleInfo getTupleInfo() {
        return tupleInfo;
    }

    public void setTupleInfo(TupleInfo tupleInfo) {
        this.tupleInfo = tupleInfo;
    }

    public FailStrategy getFailStrategy() {
        return failStrategy;
    }

    public void setFailStrategy(FailStrategy failStrategy) {
        this.failStrategy = failStrategy;
    }

    public List<TaskLocation> getFailHistory() {
        return failHistory;
    }

    public void setFailHistory(List<TaskLocation> failHistory) {
        this.failHistory = failHistory;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public ReactiveRef getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(ReactiveRef endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("taskId", taskId)
                .add("taskType", taskType)
                .add("memoryNeeded", memoryNeeded)
                .add("coreNeeded", coreNeeded)
                .add("memoryUsed", memoryUsed)
                .add("topologyId", topologyId)
                .add("componentId", componentId)
                .add("location", location)
                .add("tupleInfo", tupleInfo)
                .add("failStrategy", failStrategy)
                .add("failHistory", failHistory)
                .add("lastHeartbeat", lastHeartbeat)
                .add("endpoint", endpoint)
                .toString();
    }
}
