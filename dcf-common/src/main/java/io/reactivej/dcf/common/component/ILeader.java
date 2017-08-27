package io.reactivej.dcf.common.component;

import io.reactivej.dcf.common.protocol.leader.FinishTopology;
import io.reactivej.dcf.common.protocol.leader.KillTopology;
import io.reactivej.dcf.common.protocol.leader.StartTopology;
import io.reactivej.dcf.common.protocol.leader.SubmitTopology;
import io.reactivej.dcf.common.protocol.worker.TaskKilled;
import io.reactivej.dcf.common.protocol.worker.WorkerHeartbeat;
import io.reactivej.dcf.common.protocol.worker.WorkerRegister;
import io.reactivej.dcf.common.protocol.task.TaskPrepared;
import io.reactivej.dcf.common.protocol.task.TaskStarted;

public interface ILeader {

	public void onWorkerRegister(WorkerRegister registerInfo);

	public void onWorkerHeartbeat(WorkerHeartbeat heartbeat);

	public void onStartTopology(StartTopology command);

	public void onSubmitTopology(SubmitTopology command);

	public void onKillTopology(KillTopology cmd);

	public void onFinishTopology(FinishTopology cmd);

	public void onTaskPrepared(TaskPrepared command);

	public void onTaskStarted(TaskStarted cmd);

	public void onTaskKilled(TaskKilled cmd);
}
