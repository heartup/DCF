package io.reactivej.dcf.task;

import io.reactivej.dcf.common.component.ClusterRootComponent;
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
public class TaskSupervisor extends ClusterRootComponent {

    @Override
    public void onSupervise(SystemMessage msg) {
        super.onSupervise(msg);

        if (msg instanceof Failure) {
            Serializable failedProcessMsg = ((Failure) msg).getEnvelope().getMessage();
            if (failedProcessMsg instanceof PrepareTask) {
                // PrepareTask的消息处理失败，作业需要立即终止
                PrepareTask prepareTaskMsg = (PrepareTask) failedProcessMsg;
                GlobalTopologyId topoId = prepareTaskMsg.getTopology().getTopologyId();
                FinishTopology finishTopoMsg = new FinishTopology(topoId,
                        SerializationUtils.serialize("由于[" + ((Failure) msg).getEnvelope().getReceiver() + "]的以下原因作业被终止：" + ((Failure) msg).getCause().getMessage()));
                getClusterClient().tell(new ClusterClient.ClusterMessage("leader",
                        finishTopoMsg), getSelf());
            }
        }
    }
}
