package io.reactivej.dcf.demo.test;


import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;
import io.reactivej.dcf.demo.PiJobBuilder;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public class PiTest extends DCFDemo {

    public static final int ITER_NO = 2000;

    @Test
    public void test() throws InterruptedException, SQLException {
        submitTopologyTest();
    }

    protected Topology createTestTopology() {
        PiJobBuilder jobBuilder = new PiJobBuilder();
        Map<String, String> props = new HashMap<>();

        props.put(PiJobBuilder.totalKey, Integer.toString(ITER_NO));

        Topology topo = jobBuilder.createJobTopology(props);
        topo.setTopologyId(new GlobalTopologyId("piJob", 1));

        return topo;
    }
}
