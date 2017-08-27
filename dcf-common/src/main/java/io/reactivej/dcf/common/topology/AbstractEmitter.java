package io.reactivej.dcf.common.topology;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/***
 * Emitter的基础实现，建议所有的Emitter从此类派生
 * @author heartup@gmail.com
 *
 */
public abstract class AbstractEmitter implements IEmitter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6950510611265947698L;

	private Map<String, Serializable> conf;
	private ITopologyContext context;
	
	@Override
	public void open(Map<String, Serializable> conf, ITopologyContext sender) throws Exception {
		this.conf  = conf;
		this.context = sender;
	}
	
	public ITopologyContext getContext() {
		return context;
	}
	
	public Map<String, Serializable> getConf() {
		return conf;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
	}

	private void readObject(ObjectInputStream in) throws IOException {
	}
}
