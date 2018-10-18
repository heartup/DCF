package io.reactivej.dcf.node;

import io.reactivej.dcf.common.component.INode;
import io.reactivej.dcf.common.container.ContainerFactory;
import io.reactivej.dcf.common.container.IContainer;
import io.reactivej.dcf.common.container.process.SubProcArgConfig;
import io.reactivej.dcf.common.container.process.SubProcessExecuteParams;
import io.reactivej.dcf.common.container.process.SubProcessMain;
import io.reactivej.dcf.common.init.ServerConfig;
import io.reactivej.dcf.common.init.SystemConfig;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ReactiveComponent;
import io.reactivej.SystemMessage;
import io.reactivej.util.HostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public class ReactiveNode extends ReactiveComponent implements INode {
    Logger logger = LoggerFactory.getLogger(ReactiveNode.class);

    private IContainer leaderProcess;
    private IContainer workerProcess;
    private IContainer ackerProcess;

    @Override
    public void preStart() {
        super.preStart();
        // 启动Leader和Worker进程
        try {
            workerProcess = ContainerFactory.createJavaProcessContainer(createWorkerProcParams());
            workerProcess.startContainer();

            String localIp = HostUtil.getSuitLocalAddress();
            if (localIp.equals(SystemConfig.getValue(SystemConfig.leader_host))) {
                leaderProcess = ContainerFactory.createJavaProcessContainer(createLeaderProcParams());
                leaderProcess.startContainer();
            }
            if (localIp.equals(SystemConfig.getValue(SystemConfig.acker_host))) {
                ackerProcess = ContainerFactory.createJavaProcessContainer(createAckerProcParams());
                ackerProcess.startContainer();
            }
        } catch (IOException e) {
            logger.error("启动失败", e);
            throw new RuntimeException("启动失败", e);
        }

    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {

            }
        };
    }

    /***
     * 接受Leader和Worker进程的心跳消息，并在必要的时候重启进程
     * @throws Exception
     */


    private SubProcessExecuteParams createCommonProcParams() {
        SubProcessExecuteParams params = new SubProcessExecuteParams();
        params.setMainClass(SubProcessMain.class.getName());
        params.setWorkingDirectory(".");
        params.getSystemArguments().put(SubProcArgConfig.DCF_ISLOG,  "true");

        return params;
    }

    private SubProcessExecuteParams createLeaderProcParams() {
        SubProcessExecuteParams params = createCommonProcParams();
        params.setJavaDebugOpts(SystemConfig.getValue(SystemConfig.leader_java_debug));
        params.setStartingHeapSizeInMegabytes(SystemConfig.getIntValue(SystemConfig.leader_java_xms));
        params.setMaximumHeapSizeInMegabytes(SystemConfig.getIntValue(SystemConfig.leader_java_xmx));
        params.getSystemArguments().put(SystemConfig.SYSTEM_PORT, SystemConfig.getValue(SystemConfig.leader_port));

        params.addArgument(ServerConfig.leaderSpringConfig);
        params.getSystemArguments().put(SubProcArgConfig.DCF_ARGNAME,  "leader");

        return params;
    }

    private SubProcessExecuteParams createAckerProcParams() {
        SubProcessExecuteParams params = createCommonProcParams();
        params.setJavaDebugOpts(SystemConfig.getValue(SystemConfig.acker_java_debug));
        params.setStartingHeapSizeInMegabytes(SystemConfig.getIntValue(SystemConfig.acker_java_xms));
        params.setMaximumHeapSizeInMegabytes(SystemConfig.getIntValue(SystemConfig.acker_java_xmx));
        params.getSystemArguments().put(SystemConfig.SYSTEM_PORT, SystemConfig.getValue(SystemConfig.acker_port));

        params.addArgument(ServerConfig.ackerSpringConfig);
        params.getSystemArguments().put(SubProcArgConfig.DCF_ARGNAME,  "acker");

        return params;
    }

    private SubProcessExecuteParams createWorkerProcParams() {
        SubProcessExecuteParams params = createCommonProcParams();
        params.setJavaDebugOpts(SystemConfig.getValue(SystemConfig.worker_java_debug));
        params.setStartingHeapSizeInMegabytes(SystemConfig.getIntValue(SystemConfig.worker_java_xms));
        params.setMaximumHeapSizeInMegabytes(SystemConfig.getIntValue(SystemConfig.worker_java_xmx));
        params.getSystemArguments().put(SystemConfig.SYSTEM_PORT, SystemConfig.getValue(SystemConfig.worker_port));

        params.addArgument(ServerConfig.workerSpringConfig);
        params.getSystemArguments().put(SubProcArgConfig.DCF_ARGNAME,  "worker");

        return params;
    }
}
