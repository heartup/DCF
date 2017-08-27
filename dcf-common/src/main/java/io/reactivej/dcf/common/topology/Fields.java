package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * 
 * @ClassName: Fields
 * 
 * @Description: 数据块的数据项定义，每个数据块包含多个数据项（Field）
 * 
 * @author heartup@gmail.com
 * 
 * @date: 2015年8月11日 下午2:02:01
 */
public class Fields implements Iterable<String>, Serializable {

	/**
	 * 
	 * @fieldName: serialVersionUID
	 * 
	 * @fieldType: long
	 * 
	 * @Description: TODO
	 */
	private static final long serialVersionUID = 7390991014547912594L;

	private String[] fields;

	public Fields(List<String> fields) {
		this.fields = fields.toArray(new String[] {});
	}

	public Fields(String... fields) {
		this.fields = fields;
	}

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
	public boolean contains(String field) {
		for (String f : fields) {
			if (f.equals(field)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * 
	 * @Title: fieldIndex
	 * 
	 * @Description: 返回一个field的位置 不存在则返回-1
	 * 
	 * @param field
	 * @return
	 * 
	 * @return: int
	 */
	public int fieldIndex(String field) {
		for (int i = 0, l = fields.length; i < l; i++) {
			if (fields[i].equals(field)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 
	 * 
	 * @Title: get
	 * 
	 * @Description: 根据索引得到一个 field 索引越界则返回null
	 * 
	 * @param index
	 * @return
	 * 
	 * @return: String
	 */
	public String get(int index) {
		if (index < fields.length && index >= 0) {
			return fields[index];
		}
		return null;
	}

	@Override
	public Iterator<String> iterator() {
		return Arrays.asList(fields).iterator();
	}

	/**
	 * 
	 * 
	 * @Title: size
	 * 
	 * @Description: fields的数量
	 * 
	 * @return
	 * 
	 * @return: int
	 */
	public int size() {
		return fields.length;
	}

	/**
	 * 
	 * 
	 * @Title: toList
	 * 
	 * @Description: fields转化成列表
	 * 
	 * @return
	 * 
	 * @return: List<String>
	 */
	public List<String> toList() {
		return Arrays.asList(fields);
	}

	/**
	 * 得到序列化之后的Fields
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		for (int i = 0, l = fields.length; i < l; i++) {
			builder.append(i);
			builder.append(":");
			builder.append(fields[i]);
			builder.append(",");
		}
		builder.substring(0, builder.length() - 1);
		builder.append("}");
		return builder.toString();
	}
}
