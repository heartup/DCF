package io.reactivej.dcf.leader;

import io.reactivej.dcf.common.component.ClusterRootComponent;
import io.reactivej.dcf.common.protocol.leader.*;
import io.reactivej.dcf.common.protocol.task.TaskStateUpdated;
import io.reactivej.*;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by heartup@gmail.com1 on 2017/2/8.
 */
public class LeaderSupervisor extends ClusterRootComponent {

    private static Logger logger = LoggerFactory.getLogger(LeaderSupervisor.class);

    @Override
    public void onSupervise(SystemMessage msg) {
        if (msg instanceof Failure) {
            logger.error("", ((Failure) msg).getCause());
            Throwable cause = ((Failure) msg).getCause();
            if (cause instanceof MessageCannotSend) {
                MessageCannotSend networkFailure = (MessageCannotSend) cause;
                Serializable theCannotSendMsg = networkFailure.getEnvelope().getMessage();
                if (theCannotSendMsg instanceof LeaderInfoUpdated ||
                        theCannotSendMsg instanceof LeaderStateUpdated) {
                    // leader 监控的另一端（web）被关闭，此时将leader的监控设置为空
                    ReactiveLeader leader = (ReactiveLeader) getContext().getChild("leader").getCell().getComponent();
                    leader.setLeaderMonitor(null);
                }
                else if (theCannotSendMsg instanceof TopologyMessage ||
                        theCannotSendMsg instanceof FailMessage) {
                    // leader client端（web）被关闭，此时将leader的client设置为空
                    ReactiveLeader leader = (ReactiveLeader) getContext().getChild("leader").getCell().getComponent();
                    leader.setClientRef(null);
                }
                else if (theCannotSendMsg instanceof TaskStateUpdated) {
                    ReactiveLeader leader = (ReactiveLeader) getContext().getChild("leader").getCell().getComponent();
                    leader.setTaskMonitor(null);
                }
            }
            else {
                getClusterClient().tell(new ClusterClient.ClusterMessage("leader",
                        new FailMessage(SerializationUtils.serialize(msg))), getSelf());
            }
        }
    }
}
