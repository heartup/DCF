package io.reactivej.dcf.worker;

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
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author heartup@gmail.com on 4/6/16.
 */
public class ProcessTaskManager extends ReactiveComponent {
    Logger logger = LoggerFactory.getLogger(ProcessTaskManager.class);

    private Map<Long, IContainer> taskContainers = new HashMap<>();

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
        IContainer container = taskContainers.get(msg.getTaskId());
        if (container != null) {
            container.killContainer();
            taskContainers.remove(msg.getTaskId());

            getSender().tell(new TaskFinished(msg.getTaskId(), msg.getResult()), getSelf());
        }
    }

    private void startTaskProcess(CreateTask msg) {
        IContainer taskProcess = ContainerFactory.createJavaProcessContainer(
                createTaskProcParams(msg.getTaskInfo().getTaskId(), msg.getTaskInfo().getMemoryNeeded(),
                        msg.getTaskInfo().getLocation().getPort(), msg.getTaskInfo().getTopologyId(),
                        msg.getTaskInfo().getComponentId(),
                        msg.getTaskInfo().getFailStrategy()));
        taskContainers.put(msg.getTaskInfo().getTaskId(), taskProcess);
        try {
            taskProcess.startContainer();
        } catch (IOException e) {
            logger.error("启动Task失败", e);
            throw new RuntimeException("启动task失败", e);
        }
    }

    private void killTaskProcess(KillTask msg) {
        IContainer container = taskContainers.get(msg.getTaskInfo().getTaskId());
        if (container != null) {
            container.killContainer();
            taskContainers.remove(msg.getTaskInfo().getTaskId());
        }
        else {
            killPid(msg.getLocation().getPid());
        }

        getSender().tell(new TaskKilled(msg.getTaskInfo().getTaskId()), getSelf());
    }

    private void killPid(int pid) {
        // kill and ensure pid is kill
    }

    private SubProcessExecuteParams createCommonProcParams() {
        SubProcessExecuteParams params = new SubProcessExecuteParams();
        params.setMainClass(SubProcessMain.class.getName());
        params.setWorkingDirectory(".");

        params.getSystemArguments().put(SubProcArgConfig.DCF_ISLOG,  "true");

        return params;
    }

    private SubProcessExecuteParams createTaskProcParams(long taskId, long memNeeded, int port, GlobalTopologyId topologyId, String componentId, TaskInfo.FailStrategy failStrategy) {
        memNeeded = memNeeded / (1024 * 1024);
        SubProcessExecuteParams params = createCommonProcParams();
        if (SystemConfig.getBoolValue(SystemConfig.task_debug)) {
            int taskDebugPort = Integer.parseInt("1" + Integer.toString(port));
            params.setJavaDebugOpts("-Djava.security.egd=file:/dev/./urandom -Xdebug -Xnoagent -Dfile.encoding=UTF-8 -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=" + taskDebugPort + ",server=y,suspend=n");
        }
        else {
            params.setJavaDebugOpts("-Djava.security.egd=file:/dev/./urandom");
        }
        params.setStartingHeapSizeInMegabytes(memNeeded);
        params.setMaximumHeapSizeInMegabytes(memNeeded);

        params.getSystemArguments().put(SystemConfig.SYSTEM_PORT, Integer.toString(port));
        params.getSystemArguments().put(SystemConfig.WORKER_PORT, Integer.toString(getContext().getSystem().getPort()));
        params.getSystemArguments().put(SystemConfig.TASK_ID, Long.toString(taskId));
        params.getSystemArguments().put(SystemConfig.MEMORY_NEEDED, Long.toString(memNeeded));
        params.getSystemArguments().put(SystemConfig.FAIL_STRATEGY, failStrategy.name());
        if (failStrategy != TaskInfo.FailStrategy.NONE) {
            params.getSystemArguments().put(SystemConfig.PERSIST_ID, topologyId.getTopologyId() + "_" + componentId);
        }

        params.addArgument(ServerConfig.taskSpringConfig);
        params.getSystemArguments().put(SubProcArgConfig.DCF_ARGNAME,  "task[" + taskId + "]-" + topologyId + "-" + componentId);

        return params;
    }
}
