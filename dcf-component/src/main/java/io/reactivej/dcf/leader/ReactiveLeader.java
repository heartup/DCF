package io.reactivej.dcf.leader;

import io.reactivej.dcf.common.component.ILeader;
import io.reactivej.dcf.common.info.*;
import io.reactivej.dcf.common.init.SystemConfig;
import io.reactivej.dcf.common.protocol.leader.*;
import io.reactivej.dcf.common.protocol.task.TaskStateUpdated;
import io.reactivej.dcf.common.protocol.worker.*;
import io.reactivej.dcf.common.protocol.task.StartTask;
import io.reactivej.dcf.common.protocol.task.TaskPrepared;
import io.reactivej.dcf.common.protocol.task.TaskStarted;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ClusterClient;
import io.reactivej.ReactiveSystem;
import io.reactivej.persist.PersistentReactiveComponent;
import io.reactivej.ReactiveRef;
import io.reactivej.persist.Procedure;
import io.reactivej.persist.RecoveryComplete;
import io.reactivej.persist.SnapshotOffer;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.IComponentDescription;
import io.reactivej.dcf.common.topology.Topology;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;

/**
 * @author heartup@gmail.com on 4/1/16.
 */
public class ReactiveLeader extends PersistentReactiveComponent implements ILeader {

    public static Logger logger = LoggerFactory.getLogger(ReactiveLeader.class);

    private LeaderState state = new LeaderState();
    private ReactiveRef clusterClient;
    private ReactiveRef clientRef;
    private ReactiveRef leaderMonitor;
    private ReactiveRef taskMonitor;
    private List<Long> monitoringTasks = new ArrayList<>();

    @Override
    public Serializable getPersistentId() {
        return "leader";
    }

    @Override
    public AbstractComponentBehavior getRecoverBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof SnapshotOffer) {
                    ReactiveLeader.this.state = (LeaderState) ((SnapshotOffer) msg).getSnapshot();
                }
                else if (msg instanceof RecoveryComplete) {
                    onRecoveryComplete((RecoveryComplete) msg);
                }
            }
        };
    }

    /**
     * Worker heartbeat重置
     * @param msg
     */
    private void onRecoveryComplete(RecoveryComplete msg) {
        long heartbeat = new Date().getTime();
        for (WorkerInfo worker : state.getWorkers().values()) {
            worker.setLastHeartbeat(heartbeat);
        }

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long startTime = runtimeMXBean.getStartTime();
        String name = runtimeMXBean.getName();
        Integer pid = Integer.valueOf(name.split("@")[0]);
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        LeaderInfo leaderInfo = state.getLeaderInfo();
        leaderInfo.setLastHeartbeat(heartbeat);
        leaderInfo.setMemoryMax(rt.maxMemory());
        leaderInfo.setMemoryUsed(usedMemory);
        leaderInfo.setPid(pid);
        leaderInfo.setStartTime(startTime);

        int heartbeatInterval = SystemConfig.getIntValue(SystemConfig.heartbeat_interval);
        getContext().getSystem().getScheduler().schedule(0, heartbeatInterval, getSelf(), new UpdateLeaderInfo(), null);

        int timeout = heartbeatInterval * 3;
        getContext().getSystem().getScheduler().schedule(timeout, timeout, getSelf(), new CheckTimeout(), null);

        this.clusterClient = getContext().createChild("clusterClient", true, ClusterClient.class.getName());
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable cmd) throws Exception {
                if (cmd instanceof WorkerHeartbeat) {
                    onWorkerHeartbeat((WorkerHeartbeat) cmd);
                } else if (cmd instanceof WorkerRegister) {
                    onWorkerRegister((WorkerRegister) cmd);
                } else if (cmd instanceof SubmitTopology) {
                    onSubmitTopology((SubmitTopology) cmd);
                } else if (cmd instanceof StartTopology) {
                    onStartTopology((StartTopology) cmd);
                } else if (cmd instanceof KillTopology) {
                    onKillTopology((KillTopology) cmd);
                } else if (cmd instanceof FinishTopology) {
                    onFinishTopology((FinishTopology) cmd);
                } else if (cmd instanceof TaskPrepared) {
                    onTaskPrepared((TaskPrepared) cmd);
                } else if (cmd instanceof TaskStarted) {
                    onTaskStarted((TaskStarted) cmd);
                } else if (cmd instanceof TaskKilled) {
                    onTaskKilled((TaskKilled) cmd);
                } else if (cmd instanceof TaskFinished) {
                    onTaskFinished((TaskFinished) cmd);
                } else if (cmd instanceof CheckTimeout) {
                    onCheckTimeout((CheckTimeout) cmd);
                } else if (cmd instanceof SetLeaderMonitor) {
                    onSetLeaderMonitor((SetLeaderMonitor) cmd);
                } else if (cmd instanceof RemoveLeaderMonitor) {
                    onRemoveLeaderMonitor((RemoveLeaderMonitor) cmd);
                } else if (cmd instanceof SetTopologyMonitor) {
                    onSetTopologyMonitor((SetTopologyMonitor) cmd);
                } else if (cmd instanceof RemoveTopologyMonitor) {
                    onRemoveTopologyMonitor((RemoveTopologyMonitor) cmd);
                } else if (cmd instanceof FailMessage) {
                    onFailMessage((FailMessage) cmd);
                } else if (cmd instanceof TopologyMessage) {
                    onTopologyMessage((TopologyMessage) cmd);
                } else if (cmd instanceof UpdateLeaderInfo) {
                    onUpdateLeaderInfo((UpdateLeaderInfo) cmd);
                }
            }
        };
    }

    private void onUpdateLeaderInfo(UpdateLeaderInfo cmd) {
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        state.getLeaderInfo().setMemoryUsed(usedMemory);

        if (getLeaderMonitor() != null) {
            getLeaderMonitor().tell(new LeaderInfoUpdated(state.getLeaderInfo()), getSelf());
        }
    }

    private void onTopologyMessage(TopologyMessage msg) {
        if (getClientRef() != null) {
            getClientRef().tell(msg, getSelf());
        }
    }

    private void onFailMessage(FailMessage cmd) {
        if (getClientRef() != null) {
            getClientRef().tell(cmd, getSelf());
        }
    }

    private void onRemoveTopologyMonitor(RemoveTopologyMonitor cmd) {
        this.monitoringTasks.removeAll(state.getSchedule().getSchedules().get(cmd.getId()));
        if (this.monitoringTasks.isEmpty()) {
            setTaskMonitor(null);
        }
    }

    private void onSetTopologyMonitor(SetTopologyMonitor cmd) {
        List<Long> tasks = state.getSchedule().getSchedules().get(cmd.getId());
        this.monitoringTasks.addAll(tasks);
        setTaskMonitor(getSender());
        for (Long tid : tasks) {
            getTaskMonitor().tell(new TaskStateUpdated(this.state.getTasks().get(tid)), getSelf());
        }
    }

    private void onSetLeaderMonitor(SetLeaderMonitor cmd) {
        leaderMonitor = getSender();
        leaderMonitor.tell(new LeaderStateUpdated(state), getSelf());
    }

    private void onRemoveLeaderMonitor(RemoveLeaderMonitor cmd) {
        leaderMonitor = null;
    }

    private void onCheckTimeout(CheckTimeout cmd) {
        long curTime = new Date().getTime();
        int timeout = SystemConfig.getIntValue(SystemConfig.heartbeat_interval) * 3;
        for (WorkerInfo worker : state.getWorkers().values()) {
            if (worker.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                if (curTime - worker.getLastHeartbeat() > timeout) {
                    unregisterWorker(worker.getId());
                }
            }
        }

        for (TaskInfo task : state.getTasks().values()) {
            if (task.getStatus().ordinal() <= TaskInfo.TaskStatus.STARTED.ordinal()) {
                if (curTime - task.getLastHeartbeat() > timeout) {
                    disconnectTask(task);
                }
                else {
                    // 如果worker断开了连接，task也要disconnect
                    WorkerInfo worker = state.getWorkers().get(task.getLocation().getWorkerId());
                    if (worker.getStatus() != WorkerInfo.WorkerStatus.REGISTERED) {
                        disconnectTask(task);
                    }
                }
            }
        }
    }

    private void disconnectTask(TaskInfo task) {
        logger.info("Task[{}]断开连接", task.getTaskId());
        if (task.getFailStrategy() == TaskInfo.FailStrategy.RESTART) {
            restartTask(task);
        }
        else if (task.getFailStrategy() == TaskInfo.FailStrategy.MIGRATE) {
            migrateTask(task);
        }
        else if (task.getFailStrategy() == TaskInfo.FailStrategy.NONE) {
            failTask(task);
        }
    }

    /**
     *
     * @param task
     */
    private void failTask(final TaskInfo task) {
        final GlobalTopologyId topoId = task.getTopologyId();
        state.getTasks().get(task.getTaskId()).setStatus(TaskInfo.TaskStatus.KILLED);
        // failed task (心跳长时间未接收到）不再从schedule里remove，只是标识为killed。
        // 当worker收到schedule的update的时候去检查是否状态是killed。然后同步killed状态到进程
//        state.getSchedule().getSchedules().get(topoId).remove(task.getTaskId());
        takeSnapshot(state, new Procedure<Serializable>() {
            @Override
            public void apply(Serializable param) throws Exception {
                notifyTopologySchedule(topoId, false);
            }
        });
    }

    /**
     * newSchedule 为true表示第一次对job进行调度，如果newSchedule为false，只是通知相关task job的调度有更改
     * @param topologyId
     * @param newSchedule
     */
    private void notifyTopologySchedule(GlobalTopologyId topologyId, boolean newSchedule) {
        Set<String> workerNeed2Notify = new HashSet<>();

        for (Long taskId : state.getSchedule().getSchedules().get(topologyId)) {
            workerNeed2Notify.add(state.getTasks().get(taskId).getLocation().getWorkerId());
        }

        Topology topology = state.getTopologys().get(topologyId);

        for (String workerId : workerNeed2Notify) {
            ReactiveRef workerRef = state.getWorkers().get(workerId).getEndpoint();
            TopologySchedule schedule4Worker = new TopologySchedule();
            schedule4Worker.setWorkerId(workerId);
            schedule4Worker.setTopology(topology);

            List<Long> taskIds = state.getSchedule().getSchedules().get(topologyId);
            for (Long taskId : taskIds) {
                TaskInfo taskInfo = state.getTasks().get(taskId);
                schedule4Worker.getTopologyTasks().put(taskId, taskInfo);
                if (taskInfo.getLocation().getWorkerId().equals(workerId)) {
                    schedule4Worker.getTask4Worker().add(taskId);
                }
            }

            if (newSchedule) {
                workerRef.tell(new ScheduleTopology(schedule4Worker), getSelf());
            }
            else {
                workerRef.tell(new TopologyScheduleChanged(schedule4Worker), getSelf());
            }
        }
    }

    private void unregisterWorker(String workerId) {
        WorkerInfo worker = state.getWorkers().get(workerId);
        worker.setStatus(WorkerInfo.WorkerStatus.UN_REGISTERED);
    }

    /***
     * @param taskInfo
     */
    private void migrateTask(final TaskInfo taskInfo) {
        // 初始化各个Worker的负载
        Map<String, Long> workerMemoryLoads = new HashMap<String, Long>();
        for (WorkerInfo workerInfo : state.getWorkers().values()) {
            if (workerInfo.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                workerMemoryLoads.put(workerInfo.getId(), workerInfo.getMemoryFree());
            }
        }

        Map<String, Double> workerCPULoads = new HashMap<String, Double>();
        for (WorkerInfo workerInfo : state.getWorkers().values()) {
            if (workerInfo.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                workerCPULoads.put(workerInfo.getId(), workerInfo.getCpuLoad());
            }
        }

        final String newWorkerId = selectLowestLoadWorker(workerMemoryLoads, workerCPULoads, taskInfo.getMemoryNeeded(), taskInfo.getCoreNeeded());

        final TaskInfo.TaskLocation oldLoc = SerializationUtils.clone(taskInfo.getLocation());
        taskInfo.getFailHistory().add(oldLoc);
        taskInfo.getLocation().setWorkerId(newWorkerId);
        taskInfo.getLocation().setHost(state.getWorkers().get(newWorkerId).getHost());
        taskInfo.getLocation().setPort(getWorkerNextAvaliableTaskPort(newWorkerId));
        taskInfo.getLocation().setPid(0);
        taskInfo.getLocation().setStartTime(0);

        taskInfo.setStatus(TaskInfo.TaskStatus.MIGRATING);

        takeSnapshot(state, new Procedure<Serializable>() {
            @Override
            public void apply(Serializable param) {
                final WorkerInfo oldWorker = state.getWorkers().get(oldLoc.getWorkerId());
                if (oldWorker.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                    oldWorker.getEndpoint().tell(new KillTask(taskInfo, oldLoc), getSelf());
                }
                else {
                    final WorkerInfo newWorker = state.getWorkers().get(newWorkerId);
                    if (newWorker.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                        newWorker.getEndpoint().tell(new CreateTask(taskInfo), getSelf());
                    }
                }
            }
        });
    }

    private void restartTask(final TaskInfo taskInfo) {
        final WorkerInfo worker = state.getWorkers().get(taskInfo.getLocation().getWorkerId());
        if (worker.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
            final TaskInfo.TaskLocation oldLoc = SerializationUtils.clone(taskInfo.getLocation());
            taskInfo.getFailHistory().add(oldLoc);
            taskInfo.getLocation().setPort(getWorkerNextAvaliableTaskPort(taskInfo.getLocation().getWorkerId()));
            taskInfo.getLocation().setPid(0);
            taskInfo.getLocation().setStartTime(0);

            taskInfo.setStatus(TaskInfo.TaskStatus.MIGRATING);

            takeSnapshot(state, new Procedure<Serializable>() {
                @Override
                public void apply(Serializable param) {
                    worker.getEndpoint().tell(new KillTask(taskInfo, oldLoc), getSelf());
                }
            });
        }
    }

    private List<String> getWorkersRunningTopology(GlobalTopologyId topologyId) {
        List<Long> taskIds = state.getSchedule().getSchedules().get(topologyId);

        List<String> workers = new ArrayList<>();
        if (taskIds != null) {
            for (Long taskId : taskIds) {
                TaskInfo taskInfo = state.getTasks().get(taskId);
                if (!workers.contains(taskInfo.getLocation().getWorkerId()))
                    workers.add(taskInfo.getLocation().getWorkerId());
            }
        }

        return workers;
    }

    public void onFinishTopology(FinishTopology cmd) {
        List<String> workers = getWorkersRunningTopology(cmd.getTopologyId());

        for (String worker : workers) {
            state.getWorkers().get(worker).getEndpoint().tell(cmd, getSelf());
        }
    }

    public void onTaskKilled(TaskKilled cmd) {
        final TaskInfo taskInfo = state.getTasks().get(cmd.getTaskId());
        if (taskInfo.getStatus() == TaskInfo.TaskStatus.MIGRATING) {
            TaskInfo.TaskLocation oldLoc = taskInfo.getFailHistory().get(taskInfo.getFailHistory().size() - 1);
            WorkerInfo oldWorkerInfo = state.getWorkers().get(oldLoc.getWorkerId());
            if (!oldWorkerInfo.getAvailablePort().contains(oldLoc.getPort())) {
                oldWorkerInfo.getAvailablePort().add(oldLoc.getPort());
            }

            WorkerInfo newWorker = state.getWorkers().get(taskInfo.getLocation().getWorkerId());
            if (newWorker.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                newWorker.getEndpoint().tell(new CreateTask(taskInfo), getSelf());
            }
        }
        else {
            taskInfo.setStatus(TaskInfo.TaskStatus.KILLED);
            WorkerInfo workerInfo = state.getWorkers().get(taskInfo.getLocation().getWorkerId());
            if (!workerInfo.getAvailablePort().contains(taskInfo.getLocation().getPort())) {
                workerInfo.getAvailablePort().add(taskInfo.getLocation().getPort());
            }

            if (isTopologyKilled(taskInfo.getTopologyId())) {
                killTopology(taskInfo.getTopologyId());
            }
        }
    }

    private Topology removeTopologyInfo(final GlobalTopologyId topologyId) {
//        Topology topology = state.getTopologys().remove(topologyId);
//        List<Long> tasks = state.getSchedule().getSchedules().remove(topologyId);
//        if (tasks != null) {
//            for (Long taskId : tasks) {
//                state.getTasks().remove(taskId);
//            }
//        }

//        return topology;

        return state.getTopologys().get(topologyId);
    }

    private void killTopology(final GlobalTopologyId topologyId) {
        final Topology topology = removeTopologyInfo(topologyId);
        if (topology == null)
            return;

        topology.setState(Topology.TopologyState.KILLED);

        takeSnapshot(state, new Procedure<Serializable>() {
            @Override
            public void apply(Serializable param) throws Exception {
                getClusterClient().tell(new ClusterClient.ClusterMessage("acker", new TopologyKilled(topology)), getSelf());

                if (getClientRef() != null) {
                    getClientRef().tell(new TopologyKilled(topology), getSelf());
                }
                if (getLeaderMonitor() != null) {
                    getLeaderMonitor().tell(new TopologyKilled(topology), getSelf());
                    getLeaderMonitor().tell(new LeaderStateUpdated(state), getSelf());
                }
            }
        });
    }

    public void onTaskFinished(final TaskFinished cmd) {
        final TaskInfo taskInfo = state.getTasks().get(cmd.getTaskId());
        taskInfo.setStatus(TaskInfo.TaskStatus.FINISHED);
        WorkerInfo workerInfo = state.getWorkers().get(taskInfo.getLocation().getWorkerId());
        if (!workerInfo.getAvailablePort().contains(taskInfo.getLocation().getPort())) {
            workerInfo.getAvailablePort().add(taskInfo.getLocation().getPort());
        }

        if (isTopologyFinished(taskInfo.getTopologyId())) {
            final Topology topology = removeTopologyInfo(taskInfo.getTopologyId());
            if (topology == null)
                return;

            topology.setState(Topology.TopologyState.FINISHED);

            takeSnapshot(state, new Procedure<Serializable>() {
                @Override
                public void apply(Serializable param) throws Exception {
                    getClusterClient().tell(new ClusterClient.ClusterMessage("acker", new TopologyFinished(topology, cmd.getResult())), getSelf());

                    if (getClientRef() != null) {
                        getClientRef().tell(new TopologyFinished(topology, cmd.getResult()), getSelf());
                    }
                    if (getLeaderMonitor() != null) {
                        getLeaderMonitor().tell(new LeaderStateUpdated(state), getSelf());
                    }
                }
            });
        }
    }

    private boolean isTopologyKilled(GlobalTopologyId topologyId) {
        List<Long> sc = state.getSchedule().getSchedules().get(topologyId);
        if (sc == null)
            return true;

        for (Long taskId : sc) {
            if (!(state.getTasks().get(taskId).getStatus() == TaskInfo.TaskStatus.KILLED))
                return false;
        }

        return true;
    }

    private boolean isTopologyFinished(GlobalTopologyId topologyId) {
        for (Long taskId : state.getSchedule().getSchedules().get(topologyId)) {
            if (!(state.getTasks().get(taskId).getStatus() == TaskInfo.TaskStatus.FINISHED))
                return false;
        }

        return true;
    }

    public void onKillTopology(KillTopology cmd) {
        if (isTopologyKilled(cmd.getTopologyId())) {
            killTopology(cmd.getTopologyId());
            return;
        }

        List<String> workers = getWorkersRunningTopology(cmd.getTopologyId());

        for (String worker : workers) {
            state.getWorkers().get(worker).getEndpoint().tell(cmd, getSelf());
        }
    }

    public void onTaskStarted(TaskStarted cmd) {
        TaskInfo taskInfo = state.getTasks().get(cmd.getTaskId());
        taskInfo.setLastHeartbeat(new Date().getTime());

        if (taskInfo.getStatus() == TaskInfo.TaskStatus.MIGRATING) {
            taskInfo.setStatus(TaskInfo.TaskStatus.STARTED);
            // 新的task准备就绪，通知job中的其他task
            notifyTopologySchedule(taskInfo.getTopologyId(), false);
        }
        else {
            taskInfo.setStatus(TaskInfo.TaskStatus.STARTED);
            if (isTopologyStarted(taskInfo.getTopologyId())) {
                state.getTopologys().get(taskInfo.getTopologyId()).setState(Topology.TopologyState.EXECUTING);

                if (getClientRef() != null) {
                    getClientRef().tell(new TopologyStarted(state.getTopologys().get(taskInfo.getTopologyId())), getSelf());
                }
                if (getLeaderMonitor() != null) {
                    getLeaderMonitor().tell(new LeaderStateUpdated(state), getSelf());
                }
            }
        }
    }

    private boolean isTopologyStarted(GlobalTopologyId topologyId) {
        List<Long> taskIds = state.getSchedule().getSchedules().get(topologyId);
        for (Long taskId: taskIds) {
            TaskInfo taskInfo = state.getTasks().get(taskId);
            if (taskInfo.getStatus() != TaskInfo.TaskStatus.STARTED)
                return false;
        }

        return true;
    }

    public void onTaskPrepared(TaskPrepared cmd) {
        TaskInfo taskInfo = state.getTasks().get(cmd.getTaskId());
        taskInfo.setEndpoint(getSender());
        taskInfo.setLastHeartbeat(new Date().getTime());

        if (taskInfo.getStatus() == TaskInfo.TaskStatus.MIGRATING) {
            getTaskEndpoint(cmd.getTaskId()).tell(new StartTask(cmd.getTaskId()), getSelf());
        }
        else {
            taskInfo.setStatus(TaskInfo.TaskStatus.PREPARED);
            if (isTopologyPrepared(taskInfo.getTopologyId())) {
                startTopologyTasks(taskInfo.getTopologyId());
            }
        }
    }

    private boolean isTopologyPrepared(GlobalTopologyId topologyId) {
        List<Long> taskIds = state.getSchedule().getSchedules().get(topologyId);
        for (Long taskId: taskIds) {
            TaskInfo taskInfo = state.getTasks().get(taskId);
            if (taskInfo.getStatus() != TaskInfo.TaskStatus.PREPARED)
                return false;
        }

        return true;
    }

    private void startTopologyTasks(GlobalTopologyId topologyId) {
        List<Long> taskIds = state.getSchedule().getSchedules().get(topologyId);
        for (Long taskId : taskIds) {
            TaskInfo taskInfo = state.getTasks().get(taskId);
            taskInfo.getEndpoint().tell(new StartTask(taskInfo.getTaskId()), getSelf());
        }
    }

    public void onWorkerHeartbeat(WorkerHeartbeat heartbeat) {
        WorkerInfo workerInfo = heartbeat.getWorkerInfo();
        workerInfo.setEndpoint(getSender());
        workerInfo.setLastHeartbeat(System.currentTimeMillis());

        WorkerInfo oldWorkerInfo = state.getWorkers().get(workerInfo.getId());
        if (oldWorkerInfo != null)
            workerInfo.setAvailablePort(oldWorkerInfo.getAvailablePort());

        state.getWorkers().put(workerInfo.getId(), workerInfo);

        // task 的心跳,主要是心跳时间和内存使用量,task数据只可能比Leader少,不可能比Leader多.不应该存在的task会在Worker那一次层被kill
        for (TaskInfo task : heartbeat.getTasks().values()) {
            TaskInfo info = state.getTasks().get(task.getTaskId());
            info.setLastHeartbeat(System.currentTimeMillis());
            info.setMemoryUsed(task.getMemoryUsed());
            info.setTupleInfo(task.getTupleInfo());

            // task monitor
            if (this.monitoringTasks.contains(task.getTaskId())) {
                if (getTaskMonitor() != null) {
                    getTaskMonitor().tell(new TaskStateUpdated(task), getSelf());
                }
            }
        }
    }

    public void onWorkerRegister(WorkerRegister registerInfo) {
        final ReactiveRef workerRef = getSender();

        String workerId = registerInfo.getWorkerId();
        WorkerInfo workInfo = new WorkerInfo(workerId, registerInfo.getHost(), registerInfo.getPort(), registerInfo.getCores(), registerInfo.getMemory());
        workInfo.setAvailablePort(registerInfo.getAvailablePort());

//        ArrayList<Integer> ports = new ArrayList<Integer>();
//        ports.add(9005);
//        ports.add(9006);
//        ports.add(9007);
//        ports.add(9008);
//        ports.add(9009);
//        ports.add(9010);
//        ports.add(9011);
//        ports.add(9012);
//        ports.add(9013);
//        ports.add(9014);
//        ports.add(9015);
//        ports.add(9016);
//        ports.add(9017);
//        ports.add(9018);
//        ports.add(9019);
//        workInfo.setAvailablePort(ports);

        workInfo.setEndpoint(workerRef);
        workInfo.setStatus(WorkerInfo.WorkerStatus.REGISTERED);
        this.state.getWorkers().put(workerId, workInfo);
        takeSnapshot(SerializationUtils.clone(state), new Procedure<Serializable>() {
            @Override
            public void apply(Serializable param) {
                getSender().tell(new RegisterReceived(), getSelf());
            }
        });
    }


    public void onStartTopology(StartTopology command) {
        setClientRef(getSender());

        final GlobalTopologyId topologyId = command.getTopologyId();
        Topology topology = state.getTopologys().get(topologyId);
        topology.setState(Topology.TopologyState.PREPARING);

        scheduleTopology(topology);

        takeSnapshot(SerializationUtils.clone(state), new Procedure<Serializable>() {
            @Override
            public void apply(Serializable param) {
                notifyTopologySchedule(topologyId, true);
            }
        });
    }

    public void onSubmitTopology(SubmitTopology command) {
        final Topology topology = command.getTopology();
        topology.setState(Topology.TopologyState.INIT);
        state.getTopologys().put(topology.getTopologyId(), topology);
        takeSnapshot(SerializationUtils.clone(state), new Procedure<Serializable>() {
            @Override
            public void apply(Serializable param) {
                getSender().tell(new TopologySubmitted(topology), getSelf());
            }
        });
    }

    private void scheduleTopology(Topology topology) {
        // 初始化各个Worker的负载
        Map<String, Long> workerMemoryLoads = new HashMap<String, Long>();
        for (WorkerInfo workerInfo : state.getWorkers().values()) {
            if (workerInfo.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                workerMemoryLoads.put(workerInfo.getId(), workerInfo.getMemoryFree());
            }
        }

        Map<String, Double> workerCPULoads = new HashMap<String, Double>();
        for (WorkerInfo workerInfo : state.getWorkers().values()) {
            if (workerInfo.getStatus() == WorkerInfo.WorkerStatus.REGISTERED) {
                workerCPULoads.put(workerInfo.getId(), workerInfo.getCpuLoad());
            }
        }

        List<Long> topologyTasks = new ArrayList<Long>();

        // 先分配固定ip地址的
        for (String key : topology.getDag().getComponents().keySet()) {
            IComponentDescription compDef = topology.getDag().getComponent(key);
            if (compDef.getLocation() != null) {
                long memNeeded = compDef.getMemoryNeeded();
                int coreNeeded = compDef.getCoreNeeded();
                Integer parallelism = topology.getParallelism().get(key);
                for (int i = 0; i < parallelism; i++) {
                    String workerId = compDef.getLocation();
                    if (workerMemoryLoads.get(workerId) >= memNeeded) {
                        workerMemoryLoads.put(workerId, workerMemoryLoads.get(workerId) - memNeeded);
                        workerCPULoads.put(workerId, workerCPULoads.get(workerId) + coreNeeded);

                        // 设置taskInfo
                        long taskId = newTaskId();
                        TaskInfo taskInfo = new TaskInfo(taskId, topology.getTopologyId(), key);
                        taskInfo.setFailStrategy(TaskInfo.FailStrategy.RESTART);
                        TaskInfo.TaskLocation loc = new TaskInfo.TaskLocation();
                        loc.setWorkerId(workerId);
                        loc.setHost(state.getWorkers().get(workerId).getHost());
                        loc.setPort(getWorkerNextAvaliableTaskPort(workerId));
                        taskInfo.setLocation(loc);
                        taskInfo.setMemoryNeeded(memNeeded);
                        taskInfo.setCoreNeeded(coreNeeded);
                        if (topology.getDag().getEmitters().keySet().contains(key)) {
                            taskInfo.setTaskType(TaskInfo.TaskType.EMITTER);
                        }
                        else {
                            taskInfo.setTaskType(TaskInfo.TaskType.GEAR);
                        }

                        taskInfo.setLastHeartbeat(new Date().getTime());
                        state.getTasks().put(taskId, taskInfo);
                        topologyTasks.add(taskId);
                    }
                    else {
                        throw new RuntimeException("所需内存[" + memNeeded / (1024 * 1024) + "M], " + workerId + "节点剩余内存[" + workerMemoryLoads.get(workerId) / (1024 * 1024) + "M]");
                    }
                }
            }
        }

        for (String key : topology.getDag().getComponents().keySet()) {
            IComponentDescription compDef = topology.getDag().getComponent(key);
            if (compDef.getLocation() == null) {
                long memNeeded = compDef.getMemoryNeeded();
                int coreNeeded = compDef.getCoreNeeded();
                Integer parallelism = topology.getParallelism().get(key);
                for (int i = 0; i < parallelism; i++) {
                    String workerId = selectLowestLoadWorker(workerMemoryLoads, workerCPULoads, memNeeded, coreNeeded);
                    if (workerId != null) {
                        long taskId = newTaskId();
                        TaskInfo taskInfo = new TaskInfo(taskId, topology.getTopologyId(), key);
                        TaskInfo.TaskLocation loc = new TaskInfo.TaskLocation();
                        loc.setWorkerId(workerId);
                        loc.setHost(state.getWorkers().get(workerId).getHost());
                        loc.setPort(getWorkerNextAvaliableTaskPort(workerId));
                        taskInfo.setLocation(loc);
                        taskInfo.setMemoryNeeded(memNeeded);
                        taskInfo.setCoreNeeded(coreNeeded);
                        if (topology.getDag().getEmitters().keySet().contains(key)) {
                            taskInfo.setTaskType(TaskInfo.TaskType.EMITTER);
                        } else {
                            taskInfo.setTaskType(TaskInfo.TaskType.GEAR);
                        }

                        taskInfo.setLastHeartbeat(new Date().getTime());
                        state.getTasks().put(taskId, taskInfo);
                        topologyTasks.add(taskId);
                    } else {
                        throw new RuntimeException("所需内存[" + memNeeded / (1024 * 1024) + "M], 集群剩余内存[" + workerMemoryLoads.toString() + "]");
                    }
                }
            }
        }

        state.getSchedule().getSchedules().put(topology.getTopologyId(), topologyTasks);
    }

    private int getWorkerNextAvaliableTaskPort(String workerId) {
        List<Integer> avaliable = state.getWorkers().get(workerId).getAvailablePort();
        if (avaliable.size() > 0)
            return avaliable.remove(0);
        else {
            throw new RuntimeException(workerId + "没有可用的端口");
        }
    }

    private String selectLowestLoadWorker(Map<String, Long> workerMemoryLoads, Map<String, Double> workerCpuLoads, long memoryNeeded, int coreNeeded) {
        Set<String> memoryOkWorkers = new HashSet<>();
        for (String wid : workerMemoryLoads.keySet()) {
            long freeMem = workerMemoryLoads.get(wid);
            if (freeMem > memoryNeeded) {
                // 还需要检测端口是否还有可用的
                if (state.getWorkers().get(wid).getAvailablePort().size() > 0) {
                    memoryOkWorkers.add(wid);
                }
            }
        }

        String workerId = null;
        double cpuLoad = Integer.MAX_VALUE;
        for (String wid : memoryOkWorkers) {
            double wload = workerCpuLoads.get(wid);
            if (wload < cpuLoad) {
                workerId = wid;
                cpuLoad = wload;
            }
        }

        if (workerId != null) {
            workerMemoryLoads.put(workerId, workerMemoryLoads.get(workerId) - memoryNeeded);
            workerCpuLoads.put(workerId, workerCpuLoads.get(workerId) + coreNeeded);
        }

        return workerId;
    }

    private long newTaskId() {
        long taskId = state.getLastTaskId() + 1;
        state.setLastTaskId(taskId);
        return taskId;
    }


    private ReactiveRef getTaskEndpoint(Long taskId) {
        ReactiveRef endpoint = state.getTasks().get(taskId).getEndpoint();
        if (endpoint == null) {
            TaskInfo taskInfo = state.getTasks().get(taskId);
            endpoint = getContext().findComponent(taskInfo.getLocation().getHost(),
                    taskInfo.getLocation().getPort(), ReactiveSystem.componentSplitter + "task");
            taskInfo.setEndpoint(endpoint);
        }

        return endpoint;
    }

    public ReactiveRef getClientRef() {
        return clientRef;
    }

    public void setClientRef(ReactiveRef clientRef) {
        this.clientRef = clientRef;
    }

    public ReactiveRef getLeaderMonitor() {
        return leaderMonitor;
    }

    public void setLeaderMonitor(ReactiveRef leaderMonitor) {
        this.leaderMonitor = leaderMonitor;
    }

    public ReactiveRef getTaskMonitor() {
        return taskMonitor;
    }

    public void setTaskMonitor(ReactiveRef taskMonitor) {
        this.taskMonitor = taskMonitor;
    }

    public ReactiveRef getClusterClient() {
        return clusterClient;
    }
}
