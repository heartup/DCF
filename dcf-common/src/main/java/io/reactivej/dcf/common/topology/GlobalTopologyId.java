package io.reactivej.dcf.common.topology;

import java.io.Serializable;

public class GlobalTopologyId implements Serializable {
	/**
	 * 流id
	 */
	private final String topologyId;
	/**
	 * 模块id
	 */
	private final Long executionId;

	public GlobalTopologyId(String topologyId, long executionId) {
		this.topologyId = topologyId;
		this.executionId = executionId;
	}

	public String getTopologyId() {
		return topologyId;
	}

	public long getExecutionId() {
		return executionId;
	}

	@Override
	public boolean equals(Object obj) {
		if (null != obj && obj instanceof GlobalTopologyId) {
			GlobalTopologyId id = (GlobalTopologyId) obj;
			return topologyId.equals(id.getTopologyId()) && (executionId == id.getExecutionId());
		}
		else 
			return false;
	}
	
	@Override
	public int hashCode() {
		return (getTopologyId() + "-" + getExecutionId()).hashCode();
	}

	@Override
	public String toString() {
		return getTopologyId() + "-" + getExecutionId();
	}
}
