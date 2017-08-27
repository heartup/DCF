package io.reactivej.dcf.mock;

import io.reactivej.dcf.common.component.ClusterRootComponent;

import java.util.Arrays;

/**
 * @author heartup@gmail.com on 11/3/16.
 */
public class JobClusterMock {

    public static ClusterMock start(int workerNum) {
        ClusterMock cluster = new ClusterMock();
        cluster.setId("mock");

        for (int i = 0; i < workerNum; i++) {
            ReactiveSystemMock sys = new ReactiveSystemMock();
            sys.setSystemId("node[" + i + "]");
            sys.setCluster(cluster);
            sys.setPort((5 + i) * 1000);
            sys.setRootComponentClass(ClusterRootComponent.class.getName());
            sys.init();

            sys.createReactiveComponent("node", true, false, ReactiveNodeMock.class.getName(), Integer.toString(i));
        }

        return cluster;
    }
}
