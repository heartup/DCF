package io.reactivej.dcf.demo;

import io.reactivej.dcf.common.topology.AbstractGear;
import io.reactivej.dcf.common.topology.IOutputFieldsDeclarer;
import io.reactivej.dcf.common.topology.ITuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

public class DataMigrationMockGear extends AbstractGear {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2975019706382456875L;

	private static Logger logger = LoggerFactory.getLogger(DataMigrationMockGear.class);

	@Override
	public void execute(ITuple input) throws Exception {
		Integer index = input.getInteger(0);

//		Random rand = new Random(new Date().getTime());
//		boolean throwExcep = rand.nextBoolean();
//		if (throwExcep) {
//			logger.info("迁移任务:" + index + "将超时");
//			Thread.currentThread().sleep(6000);
//		}
//		else {
			logger.info("执行迁移任务: " + index);
//		}
		
//		getProgressCollector().reportProgress(shardNum);
	}
	
	@Override
	public Map<String, Serializable> getComponentConfigration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void defineOutputFields(IOutputFieldsDeclarer declarer) {

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
