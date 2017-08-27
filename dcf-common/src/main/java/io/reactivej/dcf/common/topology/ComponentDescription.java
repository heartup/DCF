package io.reactivej.dcf.common.topology;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/***
 * Component的基础实现：
 * outputStream通过派生类的defineOutputFields方法定义
 * inputStream通过TopologyBuilder的GearInputStreamDeclarer接口来进行定义
 * 
 * @author heartup@gmail.com
 *
 */
public class ComponentDescription implements IComponentDescription {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6186273556239056636L;

	private Serializable componentDefinition;

	private long memoryNeeded;

	private int coreNeeded;

	private String location;

	private Map<GlobalStreamId, IStream> inputStreams;

	public ComponentDescription(Serializable componentDefinition, long memoryNeeded) {
		this(componentDefinition, memoryNeeded, 1);
	}

	public ComponentDescription(Serializable componentDefinition, long memoryNeeded, int coreNeeded) {
		this.componentDefinition = componentDefinition;
		this.memoryNeeded = memoryNeeded;
		this.coreNeeded = coreNeeded;
	}

	@Override
	public Serializable getComponentDefinition() {
		return componentDefinition;
	}

	@Override
	public long getMemoryNeeded() {
		return memoryNeeded;
	}

	@Override
	public void setMemoryNeeded(long memoryNeeded) {
		this.memoryNeeded = memoryNeeded;
	}

	@Override
	public int getCoreNeeded() {
		return coreNeeded;
	}

	@Override
	public void setCoreNeeded(int coreNeeded) {
		this.coreNeeded = coreNeeded;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public Map<GlobalStreamId, IStream> getInputStreams() {
		if (inputStreams == null) {
			inputStreams = new HashMap<>();
		}
		
		return inputStreams;
	}
	
	@Override
	public IStream getInputStream(GlobalStreamId id) {
		return getInputStreams().get(id);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("componentDefinition", componentDefinition)
				.add("memoryNeeded", memoryNeeded)
				.add("coreNeeded", coreNeeded)
				.add("inputStreams", inputStreams)
				.toString();
	}
}
