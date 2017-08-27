package io.reactivej.dcf.common.topology;

import java.io.Serializable;

/***
 * 数据流的描述：
 * Fields：数据的格式，包含哪些数据项
 * Grouping：数据的分组方式
 * 
 * @author heartup@gmail.com
 *
 */
public interface IStream extends Serializable {

	/**
	 * 这个Stream对应的数据项 
	 * 所有的fields
	 * @return
	 */
	public Fields getFields();
	
	public void setFields(Fields fields);
	
	
	/**
	 * 此Stream具体使用的合并策略
	 * @return
	 */
	public Grouping getGrouping();
	
	public void setGrouping(Grouping grouping);
}
