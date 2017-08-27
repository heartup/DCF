package io.reactivej.dcf.common.topology;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @ClassName: DataBlockImpl
 * 
 * @Description: 数据块实现
 * 
 * @author heartup@gmail.com
 * 
 * @date: 2015年8月10日 下午7:48:37
 */
public class SimpleTuple implements ITuple {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 932117717449773498L;

	/**
	 * 数据块hold的值
	 */
	private List<Serializable> values;

	private long sourceTaskId;
	/**
	 * 任务id
	 */
	private long targetTaskId;

	private List<Serializable> rootIds = new ArrayList<Serializable>();
	/**
	 * 消息id
	 */
	private MessageId messageId;

	/**
	 * 流id
	 */
	private String streamId;

	/**
	 * 输出fields
	 */
	private Fields fields;
	
	private long timeout;

	public SimpleTuple() {
	}

	public void setFields(Fields fields) {
		this.fields = fields;
	}

	@Override
	public int size() {
		if (null != fields) {
			return fields.size();
		}
		return -1;
	}

	@Override
	public boolean contains(String field) {
		if (null != fields) {
			return fields.contains(field);
		}
		return false;
	}

	@Override
	public int fieldIndex(String field) {
		if (null != fields) {
			return fields.fieldIndex(field);
		}
		return -1;
	}

	@Override
	public byte[] getBinary(int i) {
		return (byte[]) values.get(i);
	}

	@Override
	public byte[] getBinaryByField(String field) {
		return (byte[]) values.get(fields.fieldIndex(field));
	}

	@Override
	public Boolean getBoolean(int i) {
		return (Boolean) values.get(i);
	}

	@Override
	public Boolean getBooleanByField(String field) {
		return (Boolean) values.get(fields.fieldIndex(field));
	}

	@Override
	public Byte getByte(int i) {
		return (Byte) values.get(i);
	}

	@Override
	public Byte getByteByField(String field) {
		return (Byte) values.get(fields.fieldIndex(field));
	}

	@Override
	public Double getDouble(int i) {
		return (Double) values.get(i);
	}

	@Override
	public Double getDoubleByField(String field) {
		return (Double) values.get(fields.fieldIndex(field));
	}

	@Override
	public Fields getFields() {
		return fields;
	}

	@Override
	public Float getFloat(int i) {
		return (Float) values.get(i);
	}

	@Override
	public Float getFloatByField(String field) {
		return (Float) values.get(fields.fieldIndex(field));
	}

	@Override
	public Integer getInteger(int i) {
		return (Integer) values.get(i);
	}

	@Override
	public Integer getIntegerByField(String field) {
		return (Integer) values.get(fields.fieldIndex(field));
	}

	@Override
	public Long getLong(int i) {
		return (Long) values.get(i);
	}

	@Override
	public Long getLongByField(String field) {
		return (Long) values.get(fields.fieldIndex(field));
	}

	@Override
	public Short getShort(int i) {
		return (Short) values.get(i);
	}

	@Override
	public Short getShortByField(String field) {
		return (Short) values.get(fields.fieldIndex(field));
	}

	@Override
	public String getString(int i) {
		return (String) values.get(i);
	}

	@Override
	public String getStringByField(String field) {
		return (String) values.get(fields.fieldIndex(field));
	}

	@Override
	public Serializable getValue(int i) {
		return values.get(i);
	}

	@Override
	public Serializable getValueByField(String field) {
		return values.get(fields.fieldIndex(field));
	}

	@Override
	public List<Serializable> getValues() {
		return values;
	}

	public void setValues(List<Serializable> values) {
		this.values = values;
	}

	@Override
	public List<Serializable> getRootIds() {
		return rootIds;
	}

	public void setRootIds(List<Serializable> rootIds) {
		this.rootIds = rootIds;
	}

	@Override
	public MessageId getMessageId() {
		return messageId;
	}

	public void setMessageId(MessageId messageId) {
		this.messageId = messageId;
	}

	/**
	 * 沿着哪条stream 发射
	 */
	public String getStreamId() {
		return streamId;
	}

	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}

	@Override
	public long getSourceTaskId() {
		return sourceTaskId;
	}

	public void setSourceTaskId(long sourceTaskId) {
		this.sourceTaskId = sourceTaskId;
	}

	@Override
	public long getTargetTaskId() {
		return targetTaskId;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("values", values)
				.add("sourceTaskId", sourceTaskId)
				.add("targetTaskId", targetTaskId)
				.add("rootIds", rootIds)
				.add("messageId", messageId)
				.add("streamId", streamId)
				.add("fields", fields)
				.add("timeout", timeout)
				.toString();
	}
}
