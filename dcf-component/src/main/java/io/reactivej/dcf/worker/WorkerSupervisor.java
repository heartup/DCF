package io.reactivej.dcf.worker;

import io.reactivej.dcf.common.component.ClusterRootComponent;
import io.reactivej.dcf.common.protocol.leader.*;
import io.reactivej.dcf.common.protocol.task.TaskStateUpdated;
import io.reactivej.dcf.common.protocol.worker.WorkerStateUpdated;
import io.reactivej.*;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by heartup@gmail.com on 2017/2/8.
 */
public class WorkerSupervisor extends ClusterRootComponent {

    private static Logger logger = LoggerFactory.getLogger(WorkerSupervisor.class);

    @Override
    public void onSupervise(SystemMessage msg) {
        super.onSupervise(msg);

        if (msg instanceof Failure) {
            logger.error("", ((Failure) msg).getCause());
            Throwable cause = ((Failure) msg).getCause();
            if (cause instanceof MessageCannotSend) {
                MessageCannotSend networkFailure = (MessageCannotSend) cause;
                Serializable theCannotSendMsg = networkFailure.getEnvelope().getMessage();
                if (theCannotSendMsg instanceof WorkerStateUpdated) {
                    // leader 监控的另一端（web）被关闭，此时将leader的监控设置为空
                    ReactiveWorker worker = (ReactiveWorker) getContext().getChild("worker").getCell().getComponent();
                    worker.setMonitor(null);
                }
            }
        }
    }
}
