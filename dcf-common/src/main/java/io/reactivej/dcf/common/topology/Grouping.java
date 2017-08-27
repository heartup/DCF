package io.reactivej.dcf.common.topology;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/***
 * 数据流的分组方式描述：
 * groupingStrategy：分组策略，可以是hash或随机
 * fields：如果是hash分组策略，那么具有相同fields的数据块需要聚合到同一个Worker上，这个fields是Stream.fields的一个子集
 * 
 * @author heartup@gmail.com
 *
 */
public class Grouping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1290597719500935710L;
	
	private GroupingStrategy groupingStrategy;
	private Fields fields;
	
	public Grouping() {
	}
	
	private Grouping(GroupingStrategy groupingStrategy, Fields fields) {
		this.groupingStrategy = groupingStrategy;
		this.fields = fields;
	}
	
	public static Grouping fieldGrouping(Fields fields) {
		return new Grouping(GroupingStrategy.fieldGrouping, fields);
	}
	
	public static Grouping randomGrouping() {
		return new Grouping(GroupingStrategy.randomGrouping, null);
	}

	public static Grouping meanGrouping() {
		return new Grouping(GroupingStrategy.meanGrouping, null);
	}
	
	public static Grouping globalGrouping() {
		return new Grouping(GroupingStrategy.globalGrouping, null);
	}
	
	public GroupingStrategy getGroupingStrategy() {
		return groupingStrategy;
	}
	
	public Fields getFields() {
		return fields;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("groupingStrategy", groupingStrategy)
				.add("fields", fields)
				.toString();
	}
}
