package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @Description: 拓扑结构里的模块定义，抽象了公共的行为
 * Emitter和Gear都可以看做是拓扑图里的模块
 * 
 * DAG里面每个结点的的共同属性：
 * 输出流：由StreamId确定，此Stream的GlobalStreamId就是这个component的componentId加上StreamId
 * 输入流：由GlobalSteamId确定，GlobalStreamId包括componentId和StreamId，因为不同的component可以具有相同的StreamId
 * 
 * GlobalStreamId
 * (componentId,streamId)             streamId
 * ————————--------------> component ———----->

 * @author: Wang Xiao Tian, heartup@gmail.com

 * @date: 2015年8月11日 上午9:44:37
 */
public interface IComponentDescription extends Serializable {

	/**
	 * emitter或gear的具体实现类的名字
	 * @return
     */
	public Serializable getComponentDefinition();

	/**
	 * 描述所需内存量
	 * @return
     */
	public long getMemoryNeeded();

	public void setMemoryNeeded(long mem);

	/**
	 * 大概占用的CPU load
	 * @return
     */
	public int getCoreNeeded();

	public void setCoreNeeded(int core);

	/**
	 * 如果指定了location，那么该component要运行在指定的id节点上。loc目前用worker id
	 * @return
	 */
	public String getLocation();

	public void setLocation(String loc);
	
	/**
	 * @param id
	 * @return 输入流定义
	 */
	public IStream getInputStream(GlobalStreamId id);
	
	/**
	 * 所有的输入流
	 * @return
	 */
	public Map<GlobalStreamId, IStream> getInputStreams();
}
