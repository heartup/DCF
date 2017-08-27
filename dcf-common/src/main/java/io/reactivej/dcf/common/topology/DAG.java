package io.reactivej.dcf.common.topology;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * 
 * @ClassName: DAG
 * 
 * @Description: Job拓扑结构的有向无环图顶点描述实现
 * emitter是DAG的起点，Gear是DAG的其他顶点。Job的计算逻辑通过DAG来描述。
 * 
 * DAG由Emitter和Gear组成，每个Emitter或Gear有若干输出Stream，Emitter或Gear通过Stream将数据输出或传入DAG下一个节点
 * 每个Emitter和Gear可以通过inputstream属性获知其所需处理的数据是从哪个Emitter或Gear的那条Stream传入的
 * 
 * @author heartup@gmail.com
 * 
 * @date: 2015年8月12日 下午7:03:49
 */
public class DAG implements IDAG {

	private static final long serialVersionUID = -7751077570245754786L;
	
	/**
	 * DAG中的 Emitter，key作为Emitter的id
	 */
	private Map<String, IComponentDescription> emitters = new HashMap<>();

	/**
	 * DAG中的 Gear，key作为Gear的id
	 */
	private Map<String, IComponentDescription> gears = new HashMap<>();
	
	/**
	 * component是emitter和Gear的集合，为了方便无差别操作emitter或Gear
	 */
	private Map<String, IComponentDescription> components = new HashMap<>();
	
	/**
	 * DAG生成时间
	 */
	private long createdTime = new Date().getTime();

	@Override
	public Map<String, IComponentDescription> getEmitters() {
		return emitters;
	}

	public void putEmitter(String key, Serializable emitterDefinition, long memNeeded) {
		ComponentDescription emitterDesc = new ComponentDescription(emitterDefinition, memNeeded);
		emitters.put(key, emitterDesc);
		components.put(key, emitterDesc);
	}

	public void putGear(String key, Serializable gearDefinition, long memNeeded) {
		ComponentDescription gearDesc = new ComponentDescription(gearDefinition, memNeeded);
		gears.put(key, gearDesc);
		components.put(key, gearDesc);
	}

	@Override
	public Map<String, IComponentDescription> getGears() {
		return gears;
	}
	
	@Override
	public IComponentDescription getComponent(String id) {
		return components.get(id);
	}
	
	public Map<String, IComponentDescription> getComponents() {
		return components;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("emitters", emitters)
				.add("gears", gears)
				.add("components", components)
				.add("createdTime", createdTime)
				.toString();
	}
}
