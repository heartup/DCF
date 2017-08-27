package io.reactivej.dcf.common.topology;

import java.io.Serializable;
import java.util.Map;



/***
 * Gear的基础实现，所有的Gear都应该从此类派生
 * @author heartup@gmail.com
 *
 */
public abstract class AbstractGear implements IGear {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7351817832145780683L;
	
	private Map<String, Serializable> conf;
	private ITopologyContext context;
	
	@Override
	public void prepare(Map<String, Serializable> conf, ITopologyContext context) throws Exception {
		this.conf  = conf;
		this.context = context;
	}
	
	public ITopologyContext getContext() {
		return context;
	}

	
	public Map<String, Serializable> getConf() {
		return conf;
	}
}
