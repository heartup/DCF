package io.reactivej.dcf.common.topology;

/***
 * 
 * @see OutputFieldsDeclarer
 * 
 * @author heartup@gmail.com
 *
 */
public interface IOutputFieldsDeclarer {

	public void declare(String streamId, Fields fields);
	
	public void declare(Fields fields);
}
