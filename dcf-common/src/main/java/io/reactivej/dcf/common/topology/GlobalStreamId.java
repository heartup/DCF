package io.reactivej.dcf.common.topology;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * 
 * 
 * @ClassName: GlobalStreamId
 * 
 * @Description: 全局流id 一个流可以用componentId + streamId 唯一定义一条输入到该component的流
 * 
 * @author: Wang Xiao Tian
 * 
 * @date: 2015年8月11日 下午3:14:02
 */
public class GlobalStreamId  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6511202534156706750L;
	/**
	 * 流id
	 */
	private String streamId;
	/**
	 * 模块id
	 */
	private String componentId;

	public GlobalStreamId() {

	}

	public GlobalStreamId(String componentId, String streamId ) {
		this.componentId = componentId;
		this.streamId = streamId;
	}

	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (null != obj && obj instanceof GlobalStreamId) {
			GlobalStreamId id = (GlobalStreamId) obj;
			return componentId.equals(id.getComponentId()) && streamId.equals(id.getStreamId());
		}
		else 
			return false;
	}
	
	@Override
	public int hashCode() {
		return (getComponentId() + "-" + getStreamId()).hashCode();
	}

	@Override
	public String toString() {
		return getComponentId() + "-" + getStreamId();
	}
}
