package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Map;


/***
 * 计算逻辑的初始计算环节，在集群中并行执行：nextTuple会被循环调用，每次调用产生一次计算任务，
 * 在nextDataBlock中通过OutputSender将计算任务转交给下一个计算逻辑(Gear)
 * 辅助工具：
 * OutputSender：在计算逻辑中通过OutputSender将数据(Tuple)发送给输出流
 * DAGContext：计算逻辑可以通过此对象获取自己在整个计算逻辑中的位置
 * conf: 计算逻辑的配置参数
 * 
 * @author: Wang Xiao Tian, heartup@gmail.com
 *
 * @date: 2015年8月10日 下午7:37:43
 */
public interface IEmitter extends IComponent {
	
	/**
	 * 为计算逻辑提供工具对象
	 * 
	 * @param conf 配置参数
	 * @param context 计算逻辑的全局描述
	 */
	public void open(Map<String, Serializable> conf, ITopologyContext context) throws Exception;
	
	/**
	 * 产生一次计算任务，在集群中被并行调用，但是一般情况下的处理逻辑是做计算任务切分，在下一个计算逻辑中并行执行
	 * 在产生计算任务的时候可以指定id(使用OutputSender的 List<Long> send(List<Serializable> newValues, MessageId msgId))
	 * 方便在ack或fail中得知某个计算任务是否被正确处理完毕
	 * 
	 * InterruptedException应该被抛出，以支持Topology的终止操作
	 *
	 * 如果没有产生tuple则返回false，标志该数据流终止
	 */
	public boolean nextTuple() throws Exception;


	/**
	 * 通知计算逻辑指定的tuple（包含msgId，和values）已经被正确处理完毕
	 * 虽然Emitter在集群中并行执行，但是从某个Emitter发出的Tuple保证会由自身接受ack或fail通知
	 * 
	 * @param tuple
	 */
    public void ack(ITuple tuple);

	/**
	 * 通知计算逻辑指定的tuple（包含msgId，和values）已经处理超时或者出现错误
	 * 虽然Emitter在集群中并行执行，但是从某个Emitter发出的Tuple保证会由自身接受ack或fail通知
	 * 
	 * @param tuple
	 */
    public void fail(ITuple tuple, Throwable cause);

	public void timeout(ITuple tuple);
   
    
    /**
     * 当计算任务被终止是被调用，可以在此做清理工作
     */
    public void close();
}
