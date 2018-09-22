package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * 
 * @ClassName: IDataBlock
 * 
 * @Description: 数据块接口
 * 
 * @author: Wang Xiao Tian
 * 
 * @date: 2015年8月11日 上午11:36:36
 */
public interface IDataBlock extends Serializable {
	/**
	 * 
	 * 
	 * @Title: size
	 * 
	 * @Description: 得到fields的数量
	 * 
	 * @return
	 * 
	 * @return: int
	 */
	public int size();

	/**
	 * 
	 * 
	 * @Title: contains
	 * 
	 * @Description: 是否包含某个field
	 * 
	 * @param field
	 * @return
	 * 
	 * @return: boolean
	 */
	public boolean contains(String field);

	/**
	 * 
	 * 
	 * @Title: fieldIndex
	 * 
	 * @Description: 得到某个field的位置
	 * 
	 * @param field
	 * 
	 * @return: int
	 */
	public int fieldIndex(String field);

	public byte[] getBinary(int i);

	public byte[] getBinaryByField(String field);

	public Boolean getBoolean(int i);

	public Boolean getBooleanByField(String field);

	public Byte getByte(int i);

	public Byte getByteByField(String field);

	public Double getDouble(int i);

	public Double getDoubleByField(String field);

	/**
	 * 
	 * 
	 * @Title: getFields
	 * 
	 * @Description: 得到该DataBlock的Fields
	 * 
	 * @return
	 * 
	 * @return: Fields
	 */
	public Fields getFields();

	public Float getFloat(int i);

	public Float getFloatByField(String field);

	public Integer getInteger(int i);

	public Integer getIntegerByField(String field);

	public Long getLong(int i);

	public Long getLongByField(String field);

	public Short getShort(int i);

	public Short getShortByField(String field);

	public String getString(int i);

	public String getStringByField(String field);

	public Serializable getValue(int i);

	public Serializable getValueByField(String field);

	public List<Serializable> getValues();
}
