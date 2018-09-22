package io.reactivej.dcf.common.component;

import io.reactivej.dcf.common.protocol.leader.FailMessage;
import io.reactivej.dcf.common.protocol.leader.FinishTopology;
import io.reactivej.dcf.common.protocol.leader.KillTopology;
import io.reactivej.dcf.common.protocol.task.PrepareTask;
import io.reactivej.*;
import io.reactivej.dcf.common.topology.GlobalTopologyId;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/***
 * 支持将错误汇报给Leader的Root组件
 *
 * @author heartup@gmail.com
 */
public class ClusterRootComponent extends RootComponent {

    private ReactiveRef clusterClient;

    @Override
    public void preStart() {
        super.preStart();
        clusterClient = getContext().createChild("clusterClient", true, ClusterClient.class.getName());
    }

    @Override
    public void onSupervise(SystemMessage msg) {
        super.onSupervise(msg);
        if (msg instanceof Failure) {
            this.clusterClient.tell(new ClusterClient.ClusterMessage("leader",
                    new FailMessage(SerializationUtils.serialize(msg))), getSelf());
        }
    }

    public ReactiveRef getClusterClient() {
        return clusterClient;
    }
}
