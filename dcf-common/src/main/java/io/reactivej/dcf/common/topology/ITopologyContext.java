package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import io.reactivej.dcf.common.topology.MessageId;
import io.reactivej.dcf.common.topology.ITuple;

/**
 * 计算逻辑（Emitter或Gear）通过IOutputSender将业务数据（tuple）传递给下一个计算逻辑
 * 在发送数据的过程中需要指定从那个Stream输出（同时也决定了传送给哪些Gear）
 * 如果没有指定Stream从默认的Stream传出（DEFAULT标志的Stream）
 * 在发送数据的过程中可以指定anchor(一个或多个tuple)，anchor的意思是当被发送的tuple被ack以后，anchor会自动被ack，最终Emitter的ack会被调用
 * fail的过程类同ack的过程
 * 
 * 同时提供控制（请求停止）Topology的功能
 * 
 * @author heartup@gmail.com
 * 
 * @date: 2015年8月10日 下午8:25:37
 */
public interface ITopologyContext {

	public GlobalTopologyId getTopologyId();
	/**
	 * 发送数据到指定的输出流
	 * 
	 * @param streamId 指定的输出流id
	 * @param newValues 需要发送的tuple的数据
	 * @return 接受被发送tuple的task列表
	 */
	public void send(String streamId, Collection<ITuple> anchors, List<Serializable> newValues);

	public void send(String streamId, ITuple anchor, List<Serializable> newValues);

	public void send(ITuple anchor, List<Serializable> newValues);

	public void send(Collection<ITuple> anchors, List<Serializable> newValues);

	public void send(String streamId, List<Serializable> newValues);

	public void send(String streamId, Serializable rootId, List<Serializable> newValues);

	/**
	 * 使用DEFAULT的Stream
	 * @param rootId
	 * @param newValues
     */
	public void send(Serializable rootId, List<Serializable> newValues);

	/**
	 * 不需要ack的数据
	 * @param newValues
     */
	public void send(List<Serializable> newValues);


	/**
	 * result表示topology执行的结, 返回后topology要结束掉
	 * @param result
     */
	public void returnResult(Serializable result);

	/**
	 * 可以通过msg来汇报进度,或异常信息等
	 * @param msg
     */
	public void report(Serializable msg);
}
