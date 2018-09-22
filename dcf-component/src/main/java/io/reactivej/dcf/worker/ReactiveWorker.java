package io.reactivej.dcf.worker;

import io.reactivej.dcf.common.component.IWorker;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.info.TopologySchedule;
import io.reactivej.dcf.common.info.WorkerInfo;
import io.reactivej.dcf.common.info.WorkerState;
import io.reactivej.dcf.common.init.SystemConfig;
import io.reactivej.dcf.common.protocol.leader.FinishTopology;
import io.reactivej.dcf.common.protocol.leader.KillTopology;
import io.reactivej.dcf.common.protocol.task.*;
import io.reactivej.dcf.common.protocol.worker.*;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ClusterClient;
import io.reactivej.ReactiveSystem;
import io.reactivej.persist.PersistentReactiveComponent;
import io.reactivej.ReactiveRef;
import io.reactivej.persist.RecoveryComplete;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;
import io.reactivej.dcf.worker.util.SocketUtils;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by heartup@gmail.com on 4/1/16.
 */
public class ReactiveWorker extends PersistentReactiveComponent implements IWorker {

    public static Logger logger = LoggerFactory.getLogger(ReactiveWorker.class);

    private String id;
    private WorkerState state = new WorkerState();

    private ReactiveRef monitor;
    private ReactiveRef taskManager;
    private ReactiveRef clusterClient;
    private ScheduledFuture<?> registerTask;

    public ReactiveWorker(String workerId) {
        this.id = workerId;
    }

    @Override
    public Serializable getPersistentId() {
        return "worker";
    }

    @Override
    public void preStart() {
        super.preStart();
    }

    @Override
    public AbstractComponentBehavior getRecoverBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof RecoveryComplete) {
                    onRecoveryComplete((RecoveryComplete) msg);
                }
            }
        };
    }

    public void onRecoveryComplete(RecoveryComplete msg) {
        state.getWorkerInfo().setId(this.id);
        state.getWorkerInfo().setHost(getContext().getSystem().getHost());
        state.getWorkerInfo().setPort(getContext().getSystem().getPort());
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        state.getWorkerInfo().setCpuLoad(os.getSystemCpuLoad());
        state.getWorkerInfo().setCores(os.getAvailableProcessors());
        long totalMemory = os.getTotalPhysicalMemorySize();
        long freeMemory = os.getFreePhysicalMemorySize();
        state.getWorkerInfo().setMemory(totalMemory);
        state.getWorkerInfo().setMemoryFree(freeMemory);
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long startTime = runtimeMXBean.getStartTime();
        state.getWorkerInfo().setStartTime(startTime);
        String name = runtimeMXBean.getName();
        Integer pid = Integer.valueOf(name.split("@")[0]);
        state.getWorkerInfo().setPid(pid);

        // 根据配置创建taskManager
        this.taskManager = getContext().createChild("taskManager");
        this.clusterClient = getContext().createChild("clusterClient", true, ClusterClient.class.getName());

        WorkerRegister registerMsg = new WorkerRegister(id, getContext().getSystem().getHost(),
                getContext().getSystem().getPort(), 4, 8000);
        SortedSet<Integer> ports = SocketUtils.findAvailableTcpPorts(100, SystemConfig.getIntValue(SystemConfig.task_port_min), SystemConfig.getIntValue(SystemConfig.task_port_max));
        registerMsg.getAvailablePort().addAll(ports);

        state.getWorkerInfo().getAvailablePort().clear();
        state.getWorkerInfo().getAvailablePort().addAll(ports);

        registerTask = getContext().getSystem().getScheduler().schedule(0L, 3000L, clusterClient,
                new ClusterClient.ClusterMessage("leader",
                        registerMsg), getSelf());
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof RegisterReceived) {
                    registerTask.cancel(true);
                    state.getWorkerInfo().setStatus(WorkerInfo.WorkerStatus.REGISTERED);
                    int heartbeat = SystemConfig.getIntValue(SystemConfig.heartbeat_interval);
                    getContext().getSystem().getScheduler().schedule(0, heartbeat, getSelf(), new UpdateWorkerInfo(), null);
                } else if (msg instanceof ScheduleTopology) {
                    onScheduleTopology((ScheduleTopology) msg);
                } else if (msg instanceof TopologyScheduleChanged) {
                    onTopologyScheduleChanged((TopologyScheduleChanged) msg);
                } else if (msg instanceof KillTopology) {
                    onKillTopology((KillTopology) msg);
                } else if (msg instanceof FinishTopology) {
                    onFinishTopology((FinishTopology) msg);
                } else if (msg instanceof TaskCreated) {
                    onTaskCreated((TaskCreated) msg);
                } else if (msg instanceof TaskPrepared) {
                    onTaskPrepared((TaskPrepared) msg);
                } else if (msg instanceof TaskKilled) {
                    onTaskKilled((TaskKilled) msg);
                } else if (msg instanceof TaskFinished) {
                    onTaskFinished((TaskFinished) msg);
                } else if (msg instanceof TaskHeartbeat) {
                    onTaskHeartbeat((TaskHeartbeat) msg);
                } else if (msg instanceof UpdateWorkerInfo) {
                    onUpdateWorkerInfo((UpdateWorkerInfo) msg);
                } else if (msg instanceof CreateTask) {
                    onCreateTask((CreateTask) msg);
                } else if (msg instanceof KillTask) {
                    // 在Task进行迁移的时候，task所在的老的worker会收到这种消息
                    onKillTask((KillTask) msg);
                } else if (msg instanceof SetWorkerMonitor) {
                    onSetMonitor((SetWorkerMonitor) msg);
                } else if (msg instanceof RemoveWorkerMonitor) {
                    onRemoveMonitor((RemoveWorkerMonitor) msg);
                } else if (msg instanceof TaskResult) {
                    onTaskResult((TaskResult) msg);
                }
            }
        };
    }

    private void onRemoveMonitor(RemoveWorkerMonitor msg) {
        setMonitor(null);
    }

    private void onSetMonitor(SetWorkerMonitor msg) {
        setMonitor(getSender());
        getMonitor().tell(new WorkerStateUpdated(state), getSelf());
    }

    private void onKillTask(KillTask msg) {
        TaskInfo taskInfo = msg.getTaskInfo();
        state.getTasks().put(taskInfo.getTaskId(), taskInfo);

        taskManager.tell(msg, getSelf());
    }

    /**
     * 当task fail的时候，由leader直接发来的请求
     * @param msg
     */
    private void onCreateTask(CreateTask msg) {
        TaskInfo taskInfo = msg.getTaskInfo();
        state.getTasks().put(taskInfo.getTaskId(), taskInfo);
        taskManager.tell(msg, getSelf());
    }

    public void onUpdateWorkerInfo(UpdateWorkerInfo msg) {
        OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        state.getWorkerInfo().setCpuLoad(os.getSystemCpuLoad());
        long totalMemory = os.getTotalPhysicalMemorySize();
        long freeMemory = os.getFreePhysicalMemorySize();
        state.getWorkerInfo().setMemory(totalMemory);
        state.getWorkerInfo().setMemoryFree(freeMemory);

        WorkerHeartbeat heartbeatMsg = new WorkerHeartbeat(state.getWorkerInfo());
        Map<Long, TaskInfo> tasks = new HashMap<>();
        for (Long taskId : state.getLocalTasks()) {
            tasks.put(taskId, state.getTasks().get(taskId));
        }
        heartbeatMsg.setTasks(tasks);

        clusterClient.tell(new ClusterClient.ClusterMessage("leader",
                        heartbeatMsg), getSelf());

        if (getMonitor() != null) {
            getMonitor().tell(new WorkerStateUpdated(state), getSelf());
        }
    }

    public void onTaskHeartbeat(TaskHeartbeat msg) {
        TaskInfo info = msg.getTaskInfo();
        info.setEndpoint(getSender());
        info.setLastHeartbeat(new Date().getTime());

        boolean shouldKill = true;
        for (List<Long> ts : state.getLocalTopologyTasks().values()) {
           if (ts.contains(info.getTaskId()))
               shouldKill = false;
        }

        if (shouldKill) {
            killTask(info);
            return;
        }

        state.getTasks().put(info.getTaskId(), info);
        if (!state.getLocalTasks().contains(info.getTaskId())) {
            state.getLocalTasks().add(info.getTaskId());
        }
    }

    private void killTask(TaskInfo task) {
        taskManager.tell(new KillTask(task, task.getLocation()), getSelf());
    }

    public void onTaskKilled(TaskKilled msg) {
        long taskId = msg.getTaskId();
        TaskInfo taskInfo = state.getTasks().remove(taskId);
        taskInfo.setStatus(TaskInfo.TaskStatus.KILLED);
        state.getLocalTasks().remove(taskId);

        GlobalTopologyId topoId = taskInfo.getTopologyId();
        state.getLocalTopologyTasks().get(topoId).remove(taskId);
        if (state.getLocalTopologyTasks().get(topoId).size() == 0) {
            state.getLocalTopologyTasks().remove(topoId);
            state.getTopologyTasks().remove(topoId);
            state.getTopologys().remove(topoId);
        }

        clusterClient.tell(new ClusterClient.ClusterMessage("leader",
                msg), getSelf());
    }

    public void onTaskFinished(TaskFinished msg) {
        long taskId = msg.getTaskId();
        TaskInfo taskInfo = state.getTasks().remove(taskId);
        taskInfo.setStatus(TaskInfo.TaskStatus.FINISHED);
        state.getLocalTasks().remove(taskId);

        GlobalTopologyId topoId = taskInfo.getTopologyId();
        state.getLocalTopologyTasks().get(topoId).remove(taskId);
        if (state.getLocalTopologyTasks().get(topoId).size() == 0) {
            state.getLocalTopologyTasks().remove(topoId);
            state.getTopologyTasks().remove(topoId);
            state.getTopologys().remove(topoId);
        }

        clusterClient.tell(new ClusterClient.ClusterMessage("leader",
                msg), getSelf());
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

    public void onFinishTopology(FinishTopology cmd) {
        List<Long> tasks = state.getLocalTopologyTasks().get(cmd.getTopologyId());
        for (Long taskId : tasks) {
            getTaskEndpoint(taskId).tell(new FinishTask(taskId, cmd.getResult()), getSelf());
        }
    }

    public void onTaskResult(TaskResult msg) {
        TaskInfo taskInfo = msg.getTaskInfo();
        taskInfo.setEndpoint(getSender());
        taskInfo.setLastHeartbeat(new Date().getTime());

        state.getTasks().put(taskInfo.getTaskId(), taskInfo);
        taskManager.tell(new FinishTask(taskInfo.getTaskId(), msg.getResult()), getSelf());
    }

    public void onKillTopology(KillTopology cmd) {
        List<Long> tasks = state.getLocalTopologyTasks().get(cmd.getTopologyId());
        if (tasks == null)
            return;

        for (Long taskId : tasks) {
            killTask(state.getTasks().get(taskId));
        }
    }

    public void onTaskPrepared(TaskPrepared command) {
        ReactiveRef taskRef = getSender();

        TaskInfo taskInfo = state.getTasks().get(command.getTaskId());
        taskInfo.setStatus(TaskInfo.TaskStatus.PREPARED);

        if (taskInfo != null) {
            taskInfo.setLastHeartbeat(new Date().getTime());
            clusterClient.tell(new ClusterClient.ClusterMessage("leader",
                    command), taskRef);
        }
    }

    public void onTaskCreated(TaskCreated command) {
        ReactiveRef taskRef = getSender();

        TaskInfo taskInfo = state.getTasks().get(command.getTaskId());

        if (taskInfo != null) {
            taskInfo.setLastHeartbeat(new Date().getTime());
            taskInfo.setMemoryUsed(command.getMemoryUsed());
            taskInfo.getLocation().setPid(command.getPid());
            taskInfo.getLocation().setStartTime(command.getStartTime());

            taskInfo.setEndpoint(taskRef);

            Topology topo = state.getTopologys().get(taskInfo.getTopologyId());
            PrepareTask msg = new PrepareTask(topo, taskInfo.getTaskId());
            for (Long taskId : state.getTopologyTasks().get(topo.getTopologyId())) {
                msg.getTopologyTasks().put(taskId, state.getTasks().get(taskId));
            }
            taskRef.tell(msg, getSelf());
        }
    }


    private void updateTopologySchedule(TopologySchedule schedule) {
        Topology topology = schedule.getTopology();
        GlobalTopologyId topologyId = topology.getTopologyId();

        state.getTopologys().put(topologyId, topology);
        state.getLocalTopologyTasks().put(topologyId, schedule.getTask4Worker());

        // 同步进程的killed状态
        for (Long tid: schedule.getTask4Worker()) {
            TaskInfo tInfo = schedule.getTopologyTasks().get(tid);
            if (TaskInfo.TaskStatus.KILLED == tInfo.getStatus()) {
                killTask(tInfo);
            }
        }

        List<Long> topoTasks = new ArrayList<Long>();
        topoTasks.addAll(schedule.getTopologyTasks().keySet());
        state.getTopologyTasks().put(topologyId, topoTasks);

        for (Long taskId : schedule.getTopologyTasks().keySet()) {
            TaskInfo taskInfo = schedule.getTopologyTasks().get(taskId);
            // 设置task心跳的初始值，以免误认为task已经死掉
            taskInfo.setLastHeartbeat(new Date().getTime());
            state.getTasks().put(taskId, taskInfo);
        }
    }

    public void onTopologyScheduleChanged(TopologyScheduleChanged command) {
        TopologySchedule schedule = command.getSchedule();
        updateTopologySchedule(schedule);

        for (Long taskId : schedule.getTask4Worker()) {
            getTaskEndpoint(taskId).tell(command, getSelf());
        }
    }

    @Override
    public void onScheduleTopology(ScheduleTopology command) {
        TopologySchedule schedule = command.getSchedule();
        updateTopologySchedule(schedule);

        // 处理创建task的逻辑
        ReactiveRef taskManagerRef = getContext().getChild("taskManager");
        for (Long taskId : schedule.getTask4Worker()) {
            TaskInfo taskInfo = schedule.getTopologyTasks().get(taskId);

            if (taskInfo.getStatus() == TaskInfo.TaskStatus.INIT) {
                CreateTask msg = new CreateTask(taskInfo);
                taskManagerRef.tell(msg, getSelf());
            }
        }
    }

    public ReactiveRef getMonitor() {
        return monitor;
    }

    public void setMonitor(ReactiveRef monitor) {
        this.monitor = monitor;
    }
}
