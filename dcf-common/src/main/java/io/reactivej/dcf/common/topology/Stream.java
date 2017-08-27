package io.reactivej.dcf.common.topology;

import com.google.common.base.MoreObjects;

/***
 * @see IStream
 * @author heartup@gmail.com
 *
 */
public class Stream implements IStream {

	private static final long serialVersionUID = -5220787815871271536L;
	/**
	 * 这个数据流所有的Fields
	 */
	private Fields fields;
	/**
	 * 到某component的分组方式是? 看谁hold住了Stream的定义
	 */
	private Grouping grouping;
	
	public Stream() {
	}
	
	public Stream(Fields fields) {
		this.fields = fields;
	}
	
	public Stream(Fields fields, Grouping grouping) {
		this.fields = fields;
		this.grouping = grouping;
	}
	
	@Override
	public Fields getFields() {
		return fields;
	}

	public void setFields(Fields fields) {
		this.fields = fields;
	}
	
	@Override
	public Grouping getGrouping() {
		return grouping;
	}
	
	public void setGrouping(Grouping grouping) {
		this.grouping = grouping;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("fields", fields)
				.add("grouping", grouping)
				.toString();
	}
}
