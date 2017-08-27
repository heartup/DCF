package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.UUID;

/**
 * 

 * @ClassName: MessageId

 * @Description: 消息id

 * @author heartup@gmail.com

 * @date: 2015年8月11日 下午3:04:43
 */
public class MessageId implements Serializable {

	private static final long serialVersionUID = -5674677254878584279L;

	private String uuid = UUID.randomUUID().toString();

	public String getUuid() {
		return uuid;
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	@Override
	public String toString() {
		return uuid;
	}
}
