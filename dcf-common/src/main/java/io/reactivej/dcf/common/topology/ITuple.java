package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * 
 * @ClassName: DataBlock
 * 
 * @Description: 包含拓扑信息的数据块接口 节点和节点间传输的数据块 该数据块是最小粒度 一个数据块和一个Fields能运算出所需要的数据
 * 
 * @author heartup@gmail.com
 * 
 * @date: 2015年8月11日 下午3:03:14
 */
public interface ITuple extends IDataBlock {

	public List<Serializable> getRootIds();

	/**
	 * 
	 * 
	 * @Title: getMessageId
	 * 
	 * @Description: 得到该数据块所属的消息id
	 * 
	 * @return
	 * 
	 * @return: MessageId
	 */
	public MessageId getMessageId();

	/**
	 * 
	 * 
	 * @Title: getStreamId
	 * 
	 * @Description: 得到此block source流的id
	 * 
	 * @return
	 * 
	 * @return: String
	 */
	public String getStreamId();

	/**
	 * 
	 * 
	 * @Title: getSourceTaskId
	 * 
	 * @Description: 得到发出此 Block的任务id
	 * 
	 * @return
	 * 
	 * @return: long
	 */
	public long getSourceTaskId();
	
	
	/**
	 * 接受此data的task
	 * @return
	 */
	public long getTargetTaskId();

	public long getTimeout();
}
