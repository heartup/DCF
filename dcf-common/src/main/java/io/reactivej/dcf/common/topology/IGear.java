package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Map;

/***
 * 计算逻辑的一个环节，在集群中并行执行：输入一个Tuple，输出一个或若干干Tuple到输出流
 * 辅助工具：
 * OutputSender：在计算逻辑中通过OutputSender将数据(Tuple)发送给输出流
 * DAGContext：计算逻辑可以通过此对象获取自己在整个计算逻辑中的位置
 * conf: 计算逻辑的配置参数
 * 
 * @author heartup@gmail.com
 *
 * @date: 2015年8月11日 上午10:31:02
 */
public interface IGear extends IComponent {
	
	/**
	 * 辅助工具
	 * 
	 * @param conf 配置参数
	 * @param context 数据发送工具
	 */
	public void prepare(Map<String, Serializable> conf, ITopologyContext context) throws Exception;
	
	/**
	 * 具体的根据输入Tuple的计算逻辑
	 * 
	 * InterruptedException应该被抛出，以支持Topology的终止操作
	 * 
	 * @param input 输入tuple
	 */
	public void execute(ITuple input) throws Exception;
	
    /**
     * 当计算任务被终止是被调用，可以在此做清理工作
     */
	public void close();
}
