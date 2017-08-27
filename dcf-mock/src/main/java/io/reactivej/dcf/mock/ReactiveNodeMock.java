package io.reactivej.dcf.mock;

import io.reactivej.dcf.acker.ReactiveAcker;
import io.reactivej.dcf.common.component.ClusterRootComponent;
import io.reactivej.dcf.common.component.INode;
import io.reactivej.AbstractComponentBehavior;
import io.reactivej.ReactiveComponent;
import io.reactivej.ReactiveSystem;
import io.reactivej.dcf.leader.ReactiveLeader;
import io.reactivej.dcf.worker.ReactiveWorker;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public class ReactiveNodeMock extends ReactiveComponent implements INode {

    private final String workerId;

    public ReactiveNodeMock(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerId() {
        return workerId;
    }

    @Override
    public void preStart() {
        super.preStart();

        createLeader();
        createWorker();
        createAcker();
    }

    private void createAcker() {
        ReactiveSystemMock sys = new ReactiveSystemMock();
        sys.setSystemId("acker");
        sys.setPort(getContext().getSystem().getPort() + 3);
        sys.setCluster(((ReactiveSystemMock)getContext().getSystem()).getCluster());
        sys.setRootComponentClass(ClusterRootComponent.class.getName());
        sys.init();

        Map<String, Object> config = new HashMap<>();
        Map<String, Object> childrenConfig = new HashMap<>();
        Map<String, Object> ackerConfig = new HashMap<>();
        ackerConfig.put(ReactiveSystem.CONFIG_SINGLETON, "true");
        ackerConfig.put(ReactiveSystem.CONFIG_DISPATCHER, "global");
        ackerConfig.put(ReactiveSystem.CONFIG_CLASS, ReactiveAcker.class.getName());
        childrenConfig.put("acker", ackerConfig);
        config.put(ReactiveSystem.CONFIG_CHILDREN, childrenConfig);
        sys.setConfig(config);

        sys.createReactiveComponent("acker");
    }

    private void createWorker() {
        ReactiveSystemMock sys = new ReactiveSystemMock();
        sys.setSystemId("worker[" + getWorkerId() + "]");
        sys.setPort(getContext().getSystem().getPort() + 2);
        sys.setCluster(((ReactiveSystemMock)getContext().getSystem()).getCluster());
        sys.setRootComponentClass(ClusterRootComponent.class.getName());
        sys.init();

        Map<String, Object> config = new HashMap<>();
        Map<String, Object> childrenConfig = new HashMap<>();
        Map<String, Object> workerConfig = new HashMap<>();
        workerConfig.put(ReactiveSystem.CONFIG_SINGLETON, "false");
        workerConfig.put(ReactiveSystem.CONFIG_DISPATCHER, "global");
        workerConfig.put(ReactiveSystem.CONFIG_JOURNAL, "local");
        workerConfig.put(ReactiveSystem.CONFIG_CLASS, ReactiveWorker.class.getName());
        workerConfig.put(ReactiveSystem.CONFIG_PARAMS, Arrays.asList(getWorkerId()));

        Map<String, Object> workerChildrenConfig = new HashMap<>();
        Map<String, Object> taskMgrConfig = new HashMap<>();
        taskMgrConfig.put(ReactiveSystem.CONFIG_SINGLETON, "false");
        taskMgrConfig.put(ReactiveSystem.CONFIG_DISPATCHER, "global");
        taskMgrConfig.put(ReactiveSystem.CONFIG_JOURNAL, "local");
        taskMgrConfig.put(ReactiveSystem.CONFIG_CLASS, TaskManagerMock.class.getName());

        workerChildrenConfig.put("taskManager", taskMgrConfig);
        workerConfig.put(ReactiveSystem.CONFIG_CHILDREN, workerChildrenConfig);
        childrenConfig.put("worker", workerConfig);
        config.put(ReactiveSystem.CONFIG_CHILDREN, childrenConfig);
        sys.setConfig(config);

        sys.createReactiveComponent("worker");
    }

    private void createLeader() {
        ReactiveSystemMock sys = new ReactiveSystemMock();
        sys.setSystemId("leader");
        sys.setPort(getContext().getSystem().getPort() + 1);
        sys.setCluster(((ReactiveSystemMock)getContext().getSystem()).getCluster());
        sys.setRootComponentClass(ClusterRootComponent.class.getName());
        sys.init();

        Map<String, Object> config = new HashMap<>();
        Map<String, Object> childrenConfig = new HashMap<>();
        Map<String, Object> leaderConfig = new HashMap<>();
        leaderConfig.put(ReactiveSystem.CONFIG_SINGLETON, "true");
        leaderConfig.put(ReactiveSystem.CONFIG_DISPATCHER, "global");
        leaderConfig.put(ReactiveSystem.CONFIG_CLASS, ReactiveLeader.class.getName());
        childrenConfig.put("leader", leaderConfig);
        config.put(ReactiveSystem.CONFIG_CHILDREN, childrenConfig);
        sys.setConfig(config);

        sys.createReactiveComponent("leader");
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {

            }
        };
    }
}
