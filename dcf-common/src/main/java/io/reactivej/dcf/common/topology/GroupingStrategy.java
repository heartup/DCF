package io.reactivej.dcf.common.topology;

/***
 * 数据流的分组策略。数据在集群的各个计算节点之间流动，计算节点把数据分组后进行处理
 * @author heartup@gmail.com
 *
 */
public enum GroupingStrategy {
	
	/**
	 * 每个Tuple随机分组
	 */
	randomGrouping, 
	
	/**
	 * hash分组策略
	 * 每个Tuple包含若干Field，所有的Tuple按照其中的一个或几个Field进行分组处理
	 * 在指定Field上具有相同value的Tuple会被组合在同一个计算节点上进行处理
	 */
	fieldGrouping, 
	
	directGrouping, 
	
	/**
	 * 所有的Tuple被集群中全局唯一的计算逻辑处理
	 */
	globalGrouping,

	/**
	 * 所有Tuple按尽量平均的方式分组
	 */
	meanGrouping
}
