package io.reactivej.dcf.common.component;

import io.reactivej.dcf.common.protocol.leader.FailMessage;
import io.reactivej.*;
import org.apache.commons.lang3.SerializationUtils;

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
