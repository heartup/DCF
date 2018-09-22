package io.reactivej.dcf.acker;

import io.reactivej.dcf.common.component.ClusterRootComponent;
import io.reactivej.dcf.common.protocol.acker.AckerInfoUpdated;
import io.reactivej.Failure;
import io.reactivej.MessageCannotSend;
import io.reactivej.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by heartup@gmail.com on 2017/2/8.
 */
public class AckerSupervisor extends ClusterRootComponent {

    private static Logger logger = LoggerFactory.getLogger(AckerSupervisor.class);

    @Override
    public void onSupervise(SystemMessage msg) {
        super.onSupervise(msg);

        if (msg instanceof Failure) {
            logger.error("", ((Failure) msg).getCause());
            Throwable cause = ((Failure) msg).getCause();
            if (cause instanceof MessageCannotSend) {
                MessageCannotSend networkFailure = (MessageCannotSend) cause;
                Serializable theCannotSendMsg = networkFailure.getEnvelope().getMessage();
                if (theCannotSendMsg instanceof AckerInfoUpdated) {
                    // acker 监控的另一端（web）被关闭，此时将acker的监控设置为空
                    ReactiveAcker acker = (ReactiveAcker) getContext().getChild("acker").getCell().getComponent();
                    acker.setAckerMonitor(null);
                }
            }
        }
    }
}
