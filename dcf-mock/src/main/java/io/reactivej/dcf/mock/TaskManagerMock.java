package io.reactivej.dcf.mock;

import io.reactivej.dcf.acker.ReactiveAcker;
import io.reactivej.dcf.common.component.ClusterRootComponent;
import io.reactivej.dcf.common.container.ContainerFactory;
import io.reactivej.dcf.common.container.IContainer;
import io.reactivej.dcf.common.container.process.SubProcArgConfig;
import io.reactivej.dcf.common.container.process.SubProcessExecuteParams;
import io.reactivej.dcf.common.container.process.SubProcessMain;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.init.ServerConfig;
import io.reactivej.dcf.common.init.SystemConfig;
import io.reactivej.dcf.common.protocol.worker.*;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ReactiveComponent;
import io.reactivej.ReactiveSystem;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.task.ReactiveTask;
import io.reactivej.dcf.worker.ProcessTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heartup@gmail.com on 4/6/16.
 */
public class TaskManagerMock extends ReactiveComponent {
    Logger logger = LoggerFactory.getLogger(TaskManagerMock.class);

    private Map<Long, ReactiveSystem> taskContainers = new HashMap<>();

    @Override
    public void preStart() {
        super.preStart();
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof CreateTask) {
                    startTaskProcess((CreateTask) msg);
                } else if (msg instanceof KillTask) {
                    killTaskProcess((KillTask) msg);
                } else if (msg instanceof FinishTask) {
                    finishTaskProcess((FinishTask) msg);
                }
            }
        };
    }

    private void finishTaskProcess(FinishTask msg) {
        ReactiveSystem container = taskContainers.get(msg.getTaskId());
        if (container != null) {
            ((ReactiveSystemMock)getContext().getSystem()).getCluster().getSystems().remove(container.getPort());
            getSender().tell(new TaskFinished(msg.getTaskId(), msg.getResult()), getSelf());
        }
    }

    private void startTaskProcess(CreateTask msg) {
        ReactiveSystem taskSystem = createTaskContainer(msg.getTaskInfo().getTaskId(), msg.getTaskInfo().getMemoryNeeded(),
                        msg.getTaskInfo().getLocation().getPort(), msg.getTaskInfo().getTopologyId(),
                        msg.getTaskInfo().getComponentId(), msg.getTaskInfo().getFailStrategy() == TaskInfo.FailStrategy.MIGRATE);
        taskContainers.put(msg.getTaskInfo().getTaskId(), taskSystem);
    }

    private void killTaskProcess(KillTask msg) {
        ReactiveSystem container = taskContainers.get(msg.getTaskInfo().getTaskId());
        if (container != null) {
            ((ReactiveSystemMock)getContext().getSystem()).getCluster().getSystems().remove(container.getPort());
        }

        getSender().tell(new TaskKilled(msg.getTaskInfo().getTaskId()), getSelf());
    }


    private ReactiveSystem createTaskContainer(long taskId, long memNeeded, int port, GlobalTopologyId topologyId, String componentId, boolean canMigrate) {
        ReactiveSystemMock sys = new ReactiveSystemMock();
        sys.setSystemId("task[" + taskId + "]-" + topologyId + "-" + componentId);
        sys.setPort(port);
        sys.setCluster(((ReactiveSystemMock)getContext().getSystem()).getCluster());
        sys.setRootComponentClass(ClusterRootComponent.class.getName());
        sys.init();

        Map<String, Object> config = new HashMap<>();
        Map<String, Object> childrenConfig = new HashMap<>();
        Map<String, Object> taskConfig = new HashMap<>();
        taskConfig.put(ReactiveSystem.CONFIG_SINGLETON, "false");
        taskConfig.put(ReactiveSystem.CONFIG_DISPATCHER, "global");
        taskConfig.put(ReactiveSystem.CONFIG_JOURNAL, canMigrate ? "shared" : "local");
        taskConfig.put(ReactiveSystem.CONFIG_CLASS, ReactiveTask.class.getName());
        taskConfig.put(ReactiveSystem.CONFIG_PARAMS, Arrays.asList(Integer.toString(getContext().getSystem().getPort()), Long.toString(taskId)));
        childrenConfig.put("task", taskConfig);
        config.put(ReactiveSystem.CONFIG_CHILDREN, childrenConfig);
        sys.setConfig(config);

        sys.createReactiveComponent("task", true, ReactiveTask.class.getName(), Integer.toString(getContext().getSystem().getPort()), Long.toString(taskId), null);

        return sys;
    }
}
