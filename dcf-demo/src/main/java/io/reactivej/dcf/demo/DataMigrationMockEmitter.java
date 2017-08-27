package io.reactivej.dcf.demo;

import io.reactivej.dcf.common.topology.ITopologyContext;
import io.reactivej.dcf.common.topology.AbstractEmitter;
import io.reactivej.dcf.common.topology.Fields;
import io.reactivej.dcf.common.topology.IOutputFieldsDeclarer;
import io.reactivej.dcf.common.topology.ITuple;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class DataMigrationMockEmitter extends AbstractEmitter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -848677463866383184L;

	private static Logger logger = LoggerFactory.getLogger(DataMigrationMockEmitter.class);

	private boolean emitted;

	private Integer totalNum = -1;
	private Integer emitterDelay = 0;

	private BlockingQueue<Integer> shards;
	private Map<Integer, Integer> shardsStatus = new ConcurrentHashMap<>();
	private Map<Integer, Long> shardBegins = new ConcurrentHashMap<>();
	private Map<Integer, Long> shardFinishes = new ConcurrentHashMap<>();

	public void emitTask(Integer taskId) {
		List<Serializable> values = new ArrayList<Serializable>();
		values.add(new Integer(taskId));
		values.add(new Integer(totalNum));

		logger.info("产生迁移任务切片: " + Arrays.toString(values.toArray()));

		shardBegins.put(taskId, System.nanoTime());
		getContext().send("jobShard", taskId, values);
	}
	
	@Override
	public boolean nextTuple() throws Exception {
		if (emitterDelay > 0) {
			Thread.currentThread().sleep(emitterDelay);
		}

		Integer taskId = shards.take();
		emitTask(taskId);

		return true;
	}

	@Override
	public void ack(ITuple tuple) {
		Integer msgId = (Integer) tuple.getRootIds().get(0);
		logger.info("接受到任务分片[{}]的ack", msgId);
		if (shardsStatus.get(msgId) == 0) {
			shardsStatus.put(msgId, 2);
			shardFinishes.put(msgId, System.nanoTime());
			if (shardsStatus.size() == totalNum)
				checkDone();
		}
	}

	private void checkDone() {
		for (Integer status : shardsStatus.values()) {
			if (status != 2)
				return;
		}

		long total = 0;
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		for (int shard : shardsStatus.keySet()) {
			long s = shardBegins.get(shard);
			long e = shardFinishes.get(shard);

			long p = e - s;
			total += p;
			if (min > p)
				min = p;

			if (max < p)
				max = p;
		}
		long mean = total / totalNum;
		getContext().returnResult("最大值为: " + max + " 最小值为: " + min + " 平均值: " + mean);
	}

	private void resendTuple(ITuple tuple) {
		Integer taskId = (Integer) tuple.getRootIds().get(0);
		List<Serializable> theValues = tuple.getValues();

		logger.info("重新产生迁移任务切片: " + Arrays.toString(theValues.toArray()));

		getContext().send("jobShard", taskId, theValues);
	}

	@Override
	public void fail(ITuple tuple, Throwable cause) {
		Integer msgId = (Integer) tuple.getRootIds().get(0);
		logger.info("任务" + msgId + "由于原因" + cause.getMessage() + "处理失败");
		// wait and try
		try {
			Thread.currentThread().sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		resendTuple(tuple);
	}

	@Override
	public void timeout(ITuple tuple) {
		Integer msgId = (Integer) tuple.getRootIds().get(0);
		logger.info("任务" + msgId + "的处理超时");
		resendTuple(tuple);
	}

	@Override
	public void open(Map<String, Serializable> conf, ITopologyContext context) throws Exception {
		super.open(conf, context);
		totalNum  = (Integer)conf.get(DMDemoJobBuilder.totalNumKey);
		emitterDelay = (Integer)conf.get(DMDemoJobBuilder.emitterDelay);

		shards = new ArrayBlockingQueue<Integer>(totalNum);

		for (int i = 1; i <= totalNum; i ++) {
			shards.put(new Integer(i));
			shardsStatus.put(i, 0);  // 初始化消息队列, 初始状态都是0
		}

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Serializable> getComponentConfigration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void defineOutputFields(IOutputFieldsDeclarer declarer) {
		declarer.declare("jobShard", new Fields("shard", "totalNum"));
	}

}
