package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Map;

/***
 * @author heartup@gmail.com
 */
public interface IComponent extends Serializable {

    public Map<String, Serializable> getComponentConfigration();

    /**
     *

     * @Title: defineOutputFields

     * @Description: 定义流的输出field,用于流的分组
     * 根据传入的streamId, 找到对应的fields
     * send的时候，会根据该流的fields值，算出hash发给不同的worker
     * 如果不指定，则随机选一个worker发送一次
     * @return: void
     */
    public void defineOutputFields(IOutputFieldsDeclarer declarer);
}
