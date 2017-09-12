package io.reactivej.dcf.demo.test;


import io.reactivej.dcf.common.component.ClusterRootComponent;
import io.reactivej.dcf.common.info.LeaderInfo;
import io.reactivej.dcf.common.info.LeaderState;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.info.WorkerState;
import io.reactivej.dcf.common.init.SystemConfig;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;
import io.reactivej.dcf.common.topology.client.DCFListener;
import io.reactivej.dcf.common.topology.client.DCFManager;
import io.reactivej.dcf.mock.ClusterMock;
import io.reactivej.dcf.mock.JobClusterMock;
import io.reactivej.dcf.mock.ReactiveSystemMock;

import java.io.Serializable;

import java.util.concurrent.CountDownLatch;

/***
 * @author heartup@gmail.com
 */
public abstract class DCFDemo {

    public void submitTopologyTest() throws InterruptedException {
        SystemConfig.setValue(SystemConfig.task_port_min, "9000");
        SystemConfig.setValue(SystemConfig.task_port_max, "9200");
        SystemConfig.setValue(SystemConfig.heartbeat_interval, "10000");
        ClusterMock cluster = JobClusterMock.start(1);

        ReactiveSystemMock system = new ReactiveSystemMock();
        system.setCluster(cluster);
        system.setSystemId("client");
        system.setPort(8998);
        system.setRootComponentClass(ClusterRootComponent.class.getName());
        system.init();

        final CountDownLatch waiter = new CountDownLatch(1);

        final DCFManager manager = new DCFManager(system);

        manager.setListener(new DCFListener() {
            @Override
            public void onTopologySubmitted(Topology topology) {
                System.out.println(topology.getTopologyId() + "提交成功");
                if (manager != null)
                    manager.startTopology(topology.getTopologyId());
            }

            @Override
            public void onTopologyStarted(Topology topology) {
                System.out.println(topology.getTopologyId() + "启动成功");
            }

            @Override
            public void onTopologyKilled(Topology topology) {

            }

            @Override
            public void onTopologyFinished(Topology topology, Serializable result) {
                System.out.println(topology.getTopologyId() + "执行结果: " + result.toString());
                waiter.countDown();
            }

            @Override
            public void onTopologyTerminated(Topology topology) {

            }

            @Override
            public void onException(Throwable e) {

            }

            @Override
            public void onTopologyMessage(GlobalTopologyId topologyId, Serializable message) {

            }

            @Override
            public void onLeaderStateUpdated(LeaderState state) {

            }

            @Override
            public void onLeaderInfoUpdated(LeaderInfo info) {

            }

            @Override
            public void onWorkerStateUpdated(WorkerState state) {

            }

            @Override
            public void onTaskStateUpdated(TaskInfo taskInfo) {

            }
        }, false);

        Thread.currentThread().sleep(3000);
        manager.submitTopology(createTestTopology());

        waiter.await();
    }

    protected abstract Topology createTestTopology();
}
