package io.reactivej.dcf.common.component;

import io.reactivej.dcf.common.protocol.leader.KillTopology;
import io.reactivej.dcf.common.protocol.worker.ScheduleTopology;
import io.reactivej.dcf.common.protocol.task.TaskCreated;
import io.reactivej.dcf.common.protocol.task.TaskPrepared;
import io.reactivej.dcf.common.protocol.worker.TaskKilled;

/***
 * @author heartup@gmail.com
 *
 */
public interface IWorker {

	public void onScheduleTopology(ScheduleTopology command);

	public void onKillTopology(KillTopology cmd);

	public void onTaskCreated(TaskCreated command);

	public void onTaskPrepared(TaskPrepared command);

	public void onTaskKilled(TaskKilled command);
}
