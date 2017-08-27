package io.reactivej.dcf.common.info;

import com.google.common.base.MoreObjects;
import io.reactivej.dcf.common.topology.GlobalTopologyId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleInfo implements Serializable {

	/***
	 * topologyId -> topology schedule
	 */
	private Map<GlobalTopologyId, List<Long>> schedules = new HashMap<>();

	public ScheduleInfo() {
	}

	public Map<GlobalTopologyId, List<Long>> getSchedules() {
		return schedules;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("schedules", schedules)
				.toString();
	}
}
