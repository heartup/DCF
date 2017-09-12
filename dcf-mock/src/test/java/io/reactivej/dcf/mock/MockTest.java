package io.reactivej.dcf.mock;

import io.reactivej.dcf.common.init.SystemConfig;
import org.junit.Test;

/**
 * @author heartup@gmail.com on 11/3/16.
 */
public class MockTest {

    @Test
    public void test() throws InterruptedException {
        SystemConfig.setValue(SystemConfig.task_port_min, "9000");
        SystemConfig.setValue(SystemConfig.task_port_max, "9200");
        SystemConfig.setValue(SystemConfig.heartbeat_interval, "10000");
        JobClusterMock.start(3);
        Thread.currentThread().join();
    }
}
