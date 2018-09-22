package io.reactivej.dcf.common.topology.client;

import io.reactivej.dcf.common.protocol.acker.RemoveAckerMonitor;
import io.reactivej.dcf.common.protocol.acker.RemoveAckerTopology;
import io.reactivej.dcf.common.protocol.acker.SetAckerMonitor;
import io.reactivej.dcf.common.protocol.leader.*;
import io.reactivej.dcf.common.protocol.worker.RemoveWorkerMonitor;
import io.reactivej.dcf.common.protocol.worker.SetWorkerMonitor;
import io.reactivej.ReactiveRef;
import io.reactivej.ReactiveSystem;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import io.reactivej.dcf.common.topology.Topology;

import java.util.ArrayList;
import java.util.List;

/***
 * @author heartup@gmail.com
 */
public class DCFManager {

    private ReactiveSystem system;
    private ReactiveRef endpoint;

    private boolean monitoringLeader;
    private boolean monitoringAcker;
    private List<String> monitoringWorkers = new ArrayList<>();
    private List<GlobalTopologyId> monitoringTopologys = new ArrayList<>();

    public DCFManager(ReactiveSystem system) {
        this.system = system;
    }

    public ReactiveSystem getSystem() {
        return system;
    }

    public void setListener(DCFListener listener, boolean monitorLeader) {
        endpoint = system.createReactiveComponent("endpoint", true, DCFEndpoint.class.getName(), listener);
        if (monitorLeader) {
            endpoint.tell(new SetLeaderMonitor(), null);
        }
    }

    public boolean isMonitoringLeader() {
        return monitoringLeader;
    }

    public boolean isMonitoringAcker() {
        return monitoringAcker;
    }

    public void startMonitorLeader() {
        monitoringLeader = true;
        endpoint.tell(new SetLeaderMonitor(), null);
    }

    public void endMonitorLeader() {
        monitoringLeader = false;
        endpoint.tell(new RemoveLeaderMonitor(), null);
    }

    public void startMonitorAcker() {
        monitoringAcker = true;
        endpoint.tell(new SetAckerMonitor(), null);
    }

    public void endMonitorAcker() {
        monitoringAcker = false;
        endpoint.tell(new RemoveAckerMonitor(), null);
    }

    public void submitTopology(Topology topology) {
         endpoint.tell(new SubmitTopology(topology), null);
    }

    public void startTopology(GlobalTopologyId topologyId) {
        endpoint.tell(new StartTopology(topologyId), null);
    }

    public void killTopology(GlobalTopologyId topologyId) {
        endpoint.tell(new KillTopology(topologyId), null);
    }

    public List<String> getMonitoringWorkers() {
        return monitoringWorkers;
    }

    public void startMonitorWorker(String workerId, String host, int port) {
        this.monitoringWorkers.add(workerId);
        endpoint.tell(new SetWorkerMonitor(workerId, host, port), null);
    }

    public void endMonitorWorker(String workerId, String host, int port) {
        this.monitoringWorkers.remove(workerId);
        endpoint.tell(new RemoveWorkerMonitor(workerId, host, port), null);
    }

    public List<GlobalTopologyId> getMonitoringTopologys() {
        return monitoringTopologys;
    }

    public void startMonitorTopology(GlobalTopologyId globalTopologyId) {
        this.monitoringTopologys.add(globalTopologyId);
        endpoint.tell(new SetTopologyMonitor(globalTopologyId), null);
    }

    public void endMonitorTopology(GlobalTopologyId globalTopologyId) {
        this.monitoringTopologys.remove(globalTopologyId);
        endpoint.tell(new RemoveTopologyMonitor(globalTopologyId), null);
    }

    public void resetLeader() {
        endpoint.tell(new ResetLeader(), null);
    }

    public void removeAckerTopology(GlobalTopologyId id) {
        endpoint.tell(new RemoveAckerTopology(id), null);
    }
}
