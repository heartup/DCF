package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Map;

/**

 * @See DAG

 * @Description: Job拓扑结构的有向无环图描述

 * @author: Wang Xiao Tian, heartup@gmail.com

 * @date: 2015年8月10日 下午7:13:55
 */
public interface IDAG extends Serializable {
	
	public static final String DEFAULT_STREAMID = "DEFAULT";
	
	/**
	 * DAG中Emitter的集合，key是Emitter的id
	 * @return
	 */
	public Map<String, IComponentDescription> getEmitters();
	
	/**
	 * DAG中Gear的集合，key是Gear的id
	 * 
	 * @return
	 */
	public  Map<String, IComponentDescription> getGears();
	
	/**
	 * 根据id返回emitter或者gear, 用于不用区分Emitter或Gear的操作
	 * 
	 * @param id
	 * @return
	 */
	public IComponentDescription getComponent(String id);
	
	
	public Map<String, IComponentDescription> getComponents();
	
}
