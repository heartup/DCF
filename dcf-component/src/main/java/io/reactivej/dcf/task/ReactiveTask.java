package io.reactivej.dcf.task;

import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.proxy.CglibProxyProvider;
import org.xeustechnologies.jcl.proxy.ProxyProviderFactory;
import io.reactivej.dcf.common.info.TaskInfo;
import io.reactivej.dcf.common.info.TaskState;
import io.reactivej.dcf.common.init.SystemConfig;
import io.reactivej.dcf.common.protocol.leader.FinishTopology;
import io.reactivej.dcf.common.protocol.leader.TopologyMessage;
import io.reactivej.dcf.common.protocol.task.*;
import io.reactivej.dcf.common.protocol.tuple.*;
import io.reactivej.dcf.common.protocol.worker.FinishTask;
import io.reactivej.dcf.common.protocol.worker.TopologyScheduleChanged;
import io.reactivej.*;
import io.reactivej.persist.PersistentReactiveComponent;
import io.reactivej.persist.RecoveryComplete;
import io.reactivej.persist.SnapshotOffer;
import io.reactivej.dcf.common.topology.*;
import io.reactivej.dcf.common.util.ConsistentHashUtil;
import io.reactivej.dcf.common.util.SerializeUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by heartup@gmail.com on 8/6/16.
 */
public class ReactiveTask extends PersistentReactiveComponent implements ITask, ITopologyContext {
    private static Logger logger = LoggerFactory.getLogger(ReactiveTask.class);

    private final int workerPort;
    private TaskState state = new TaskState();

    private ReactiveRef workerRef;
    private ReactiveRef clusterClient;

    private DAGContext dagContext;
    /**
     * component对应的所有的taskid
     */
    private volatile Map<String, List<Long>> componentTasks;
    private Map<String, Integer> meanGroupingLastTaskIndex = new HashMap<>();
    private String[] tupleProcessChildren;
    private int lastChildUsed;
    private ReactiveRef ackChildRef;  // ack 和 emitter 公用，避免并发竞争

    /***
     * persistId 不为空，表示component状态应该存储，
     * 否则利用taskId作为persistId，表示状态每次运行都不同，不用存储
     *
     * @param workerPort
     * @param taskId
     * @param persistId
     */
    public ReactiveTask(String workerPort, String taskId, String persistId) {
        this.workerPort = Integer.parseInt(workerPort);
        this.state.setTaskId(Long.parseLong(taskId));
        if (StringUtils.isNotEmpty(persistId)) {
            this.state.setPersistId(persistId);
        }
        else {
            this.state.setPersistId(taskId);
        }
    }

    @Override
    public Serializable getPersistentId() {
        return state.getPersistId();
    }

    @Override
    public void onSupervise(SystemMessage msg) {
        super.onSupervise(msg);
        if (msg instanceof Failure) {
            Serializable failedMessage = ((Failure) msg).getEnvelope().getMessage();
            // this is gear's logic
            if (failedMessage instanceof ReceiveTuple) {
                // 处理tuple的component不再kill，保持一定的线程度
                // getSender().tell(new Poison(), null); // kill the child component
                ITuple theTuple = ((ReceiveTuple) failedMessage).getTuple();
                if (theTuple.getRootIds().size() > 0) {
                    // 告知emitter，tuple处理fail
                    clusterClient.tell(new ClusterClient.ClusterMessage("acker",
                                    new FailTuple(state.getTopology().getTopologyId(), theTuple.getRootIds(),
                                            SerializationUtils.serialize(theTuple),
                                            SerializationUtils.serialize(((Failure) msg).getCause()))),
                            getSelf());
                }
            }
            // this is emitter's logic
            else if (failedMessage instanceof NextTuple ||
                    failedMessage instanceof FailTuple ||
                    failedMessage instanceof TimeoutTuple) {

                if (failedMessage instanceof NextTuple) {
                    // 继续处理下一条消息
                    getSender().tell(new NextTuple(), getSelf());
                }
                else {
                    getSender().tell(new Poison(), null); // kill the child component
                }

                if (((Failure) msg).getCause() instanceof MessageCannotSend) {
                    Envelope envelope = ((MessageCannotSend) ((Failure) msg).getCause()).getEnvelope();
                    if (envelope.getMessage() instanceof ReceiveTuple) {
                        ITuple theTuple = ((ReceiveTuple) envelope.getMessage()).getTuple();
                        if (theTuple.getRootIds().size() > 0) {
                            clusterClient.tell(new ClusterClient.ClusterMessage("acker",
                                    new FailTuple(state.getTopology().getTopologyId(), theTuple.getRootIds(),
                                            SerializationUtils.serialize(theTuple),
                                            SerializationUtils.serialize(((Failure) msg).getCause()))), getSelf());
                        }
                    }
                }
            }
        }
    }

    @Override
    public AbstractComponentBehavior getRecoverBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof RecoveryComplete) {
                    onRecoveryComplete((RecoveryComplete) msg);
                }
                else if (msg instanceof SnapshotOffer) {
                    onSnapshotOffer((SnapshotOffer) msg);
                }
            }
        };
    }

    private void onSnapshotOffer(SnapshotOffer msg) {

    }

    public void onRecoveryComplete(RecoveryComplete msg) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long startTime = runtimeMXBean.getStartTime();
        String name = runtimeMXBean.getName();
        Integer pid = Integer.valueOf(name.split("@")[0]);
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        clusterClient = getContext().createChild("clusterClient", true, ClusterClient.class.getName());
        workerRef = getContext().findComponent(getContext().getSystem().getHost(), this.workerPort, ReactiveSystem.componentSplitter + "worker");
        workerRef.tell(new TaskCreated(this.state.getTaskId(), pid, startTime, usedMemory), getSelf());
    }

    @Override
    public AbstractComponentBehavior getDefaultBehavior() {
        return new AbstractComponentBehavior(this) {
            @Override
            public void onMessage(Serializable msg) throws Exception {
                if (msg instanceof PrepareTask) {
                    onPrepareTask((PrepareTask) msg);
                } else if (msg instanceof StartTask) {
                    onStartTask((StartTask) msg);
                } else if (msg instanceof FinishTask) {
                    onFinishTask((FinishTask) msg);
                } else if (msg instanceof ReceiveTuple) {
                    onRecieveTuple((ReceiveTuple) msg);
                } else if (msg instanceof TupleProcessed) {
                    onTupleProcessed((TupleProcessed) msg);
                } else if (msg instanceof NextTupleCreated) {
                    onNextTupleCreated((NextTupleCreated) msg);
                } else if (msg instanceof CheckTupleTimeout) {
                    onCheckTupleTimeout((CheckTupleTimeout) msg);
                } else if (msg instanceof TupleTimeouted) {
                    onTupleTimeouted((TupleTimeouted) msg);
                } else if (msg instanceof FinishTuple) {
                    onFinishTuple((FinishTuple) msg);
                } else if (msg instanceof FailTuple) {
                    onFailTuple((FailTuple) msg);
                } else if (msg instanceof TimeoutTuple) {
                    onTimeoutTuple((TimeoutTuple) msg);
                } else if (msg instanceof TupleFinished) {
                    onTupleFinished((TupleFinished) msg);
                } else if (msg instanceof TupleFailed) {
                    onTupleFailed((TupleFailed) msg);
                } else if (msg instanceof UpdateTaskInfo) {
                    onUpdateTaskInfo((UpdateTaskInfo) msg);
                } else if (msg instanceof TopologyScheduleChanged) {
                    onTopologyScheduleChanged((TopologyScheduleChanged) msg);
                } else if (msg instanceof SuspendTuple) {
                    onSuspendTuple((SuspendTuple) msg);
                } else if (msg instanceof ResumeTuple) {
                    onResumeTuple((ResumeTuple) msg);
                }
            }
        };
    }

    private void onResumeTuple(ResumeTuple msg) {
        state.getTaskInfo().setStatus(TaskInfo.TaskStatus.STARTED);
        for (ReceiveTuple tuple : state.getSuspendedReceiveTuples()) {
            dispatchTuple(tuple);
        }
        state.getSuspendedReceiveTuples().clear();

        for (ReactiveRef target : state.getSuspendedNextTuples()) {
            target.tell(new NextTuple(), getSelf());
        }
        state.getSuspendedNextTuples().clear();
    }

    private void onSuspendTuple(SuspendTuple msg) {
        state.getTaskInfo().setStatus(TaskInfo.TaskStatus.SUSPENDED);
    }

    private void onFinishTask(FinishTask msg) {
        getSender().tell(new TaskResult(state.getTaskInfo(), msg.getResult()), getSelf());
    }

    private void onTopologyScheduleChanged(TopologyScheduleChanged msg) {
        this.state.setTopologyTasks(msg.getSchedule().getTopologyTasks());
        this.componentTasks = createComponentTasksMap();
    }

    private Map<String, List<Long>> createComponentTasksMap() {
        Map<String, List<Long>> componentTasks = new HashMap<>();
        for (TaskInfo taskInfo : this.state.getTopologyTasks().values()) {
            String compId = taskInfo.getComponentId();
            if (componentTasks.get(compId) == null) {
                componentTasks.put(compId, new ArrayList<Long>());
            }
            componentTasks.get(compId).add(taskInfo.getTaskId());
        }

        for (List<Long> l : componentTasks.values()) {
            Collections.sort(l);
        }

        return componentTasks;
    }

    private void onUpdateTaskInfo(UpdateTaskInfo msg) {
        Runtime rt = Runtime.getRuntime();
        long totalMemory = rt.totalMemory();
        long freeMemory = rt.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        state.getTaskInfo().setMemoryUsed(usedMemory);

        workerRef.tell(new TaskHeartbeat(state.getTaskInfo()), getSelf());
    }

    private void onFinishTuple(FinishTuple msg) {
        if (msg.getTopologyId().equals(state.getTopology().getTopologyId())) {
            long acked = state.getTaskInfo().getTupleInfo().getAcked();
            state.getTaskInfo().getTupleInfo().setAcked(acked + 1);

            if (msg.getTuple() != null) {
//                ReactiveRef tupleHandler = getContext().createChild("ack-" + UUID.randomUUID().toString(), true, TupleHandler.class.getName(), this.state.getExecutableComponent());
                ackChildRef.tell(msg, getSelf());
            }
        }
    }

    private void onTimeoutTuple(TimeoutTuple msg) {
        if (msg.getTopologyId().equals(state.getTopology().getTopologyId())) {
            long timeouted = state.getTaskInfo().getTupleInfo().getTimeouted();
            state.getTaskInfo().getTupleInfo().setTimeouted(timeouted + 1);

            if (msg.getTuple() != null) {
                ReactiveRef tupleHandler = getContext().createChild("timeout-" + UUID.randomUUID().toString(), true, TupleHandler.class.getName(), this.state.getExecutableComponent());
                tupleHandler.tell(msg, getSelf());
            }
        }
    }

    private void onFailTuple(FailTuple msg) {
        if (msg.getTopologyId().equals(state.getTopology().getTopologyId())) {
            long failed = state.getTaskInfo().getTupleInfo().getFailed();
            state.getTaskInfo().getTupleInfo().setFailed(failed + 1);

            if (msg.getTuple() != null) {
//                ReactiveRef tupleHandler = getContext().createChild("fail-" + UUID.randomUUID().toString(), true, TupleHandler.class.getName(), this.state.getExecutableComponent());
                ackChildRef.tell(msg, getSelf());
            }
        }
    }

    private void onTupleFinished(TupleFinished msg) {
        state.getPendingTuples().remove(msg.getRootId().toString());
//        getSender().tell(new Poison(), null); 使用emitter来接受ack消息，不再销毁
    }

    private void onTupleFailed(TupleFailed msg) {
        state.getPendingTuples().remove(msg.getRootId().toString());
        getSender().tell(new Poison(), null);
    }

    private void onTupleTimeouted(TupleTimeouted msg) {
        state.getPendingTuples().remove(msg.getRootId().toString());
        getSender().tell(new Poison(), null);
    }

    private void onCheckTupleTimeout(CheckTupleTimeout msg) {
        ITuple tuple = state.getPendingTuples().get(msg.getRootId());
        if (tuple != null) {
            ReactiveRef tupleHandler = getContext().createChild("timeout-" + UUID.randomUUID().toString(), true, TupleHandler.class.getName(), this.state.getExecutableComponent());
            tupleHandler.tell(new TimeoutTuple(null, SerializationUtils.serialize(tuple)), getSelf());
        }
    }

    public void onNextTupleCreated(NextTupleCreated msg) {
        if (state.getTaskInfo().getStatus() != TaskInfo.TaskStatus.SUSPENDED) {
            getSender().tell(new NextTuple(), getSelf());
        }
        else {
            state.getSuspendedNextTuples().add(getSender());
        }
    }

    public void onTupleProcessed(TupleProcessed msg) {
        long processed = state.getTaskInfo().getTupleInfo().getProcessed();
        state.getTaskInfo().getTupleInfo().setProcessed(processed + 1);

        ITuple tuple = msg.getTuple();
        state.getPendingTuples().remove(tuple.getMessageId().toString());

        // 处理tuple的child component不再kill，数量和设置的线程数相等
//        getSender().tell(new Poison(), null);

        if (tuple.getRootIds().size() > 0) {
            clusterClient.tell(new ClusterClient.ClusterMessage("acker",
                    new AckTuple(state.getTopology().getTopologyId(), tuple.getRootIds(), tuple.getMessageId())), getSelf());
        }

        if (state.getSuspendedTask().size() > 0 &&
                state.getPendingTuples().size() < state.getTopology().getTupleQueueSizeLow()) {
            resumeTuple();
        }
    }

    public void onRecieveTuple(final ReceiveTuple msg) {
        // too many tuples waiting to process, suspend receiving tuples
        if (state.getPendingTuples().size() >= state.getTopology().getTupleQueueSizeHigh()) {
            suspendTuple();
        }

        long recieved = state.getTaskInfo().getTupleInfo().getRecieved();
        state.getTaskInfo().getTupleInfo().setRecieved(recieved + 1);

        state.getPendingTuples().put(msg.getTuple().getMessageId().toString(), msg.getTuple());

        if (state.getTaskInfo().getStatus() == TaskInfo.TaskStatus.SUSPENDED) {
            state.getSuspendedReceiveTuples().add(msg);
        }
        else {
            dispatchTuple(msg);
        }
    }

    /***
     * 将详细提交给子组件处理
     * @param msg
     */
    private void dispatchTuple(final ReceiveTuple msg) {
        String child = this.tupleProcessChildren[this.lastChildUsed];
        if (child == null) {
            child = "tuple-" + msg.getTuple().getMessageId().toString();
            ReactiveRef tupleHandler = getContext().createChild(child, true, TupleHandler.class.getName(), ReactiveTask.this.state.getExecutableComponent());
            this.tupleProcessChildren[this.lastChildUsed] = child;
            tupleHandler.tell(msg, getSelf());
        } else {
            ReactiveRef tupleHandler = getContext().getChild(child);
            tupleHandler.tell(msg, getSelf());
        }

        this.lastChildUsed = (this.lastChildUsed + 1) % this.tupleProcessChildren.length;
    }

    private void suspendTuple() {
        state.getSuspendedTask().add(getSender());
        getSender().tell(new SuspendTuple(), getSelf());
    }

    private void resumeTuple() {
        for (ReactiveRef task : state.getSuspendedTask()) {
            task.tell(new ResumeTuple(), getSelf());
        }
        state.getSuspendedTask().clear();
    }

    public void onStartTask(StartTask msg) {
        if (state.getExecutableComponent() instanceof IEmitter) {
            Integer parallelism = state.getTopology().getThreadParallelism().get(state.getTaskInfo().getComponentId());
            while (parallelism > 0) {
                parallelism --;
                ReactiveRef emitterHandler = getContext().createChild("emitter-" + UUID.randomUUID().toString(), true, TupleHandler.class.getName(), this.state.getExecutableComponent());
                if (ackChildRef == null) {
                    ackChildRef = emitterHandler;
                }
                emitterHandler.tell(new NextTuple(), getSelf());
            }
        }

        getSender().tell(new TaskStarted(state.getTaskId()), getSelf());

        int heartbeat = SystemConfig.getIntValue(SystemConfig.heartbeat_interval);
        getContext().getSystem().getScheduler().schedule(heartbeat, heartbeat, getSelf(), new UpdateTaskInfo(), null);
    }

    public void onPrepareTask(PrepareTask msg) {
        this.state.setTopology(msg.getTopology());
        TaskInfo thisTaskInfo = msg.getTopologyTasks().get(msg.getTaskId());
        this.state.setTaskInfo(thisTaskInfo);
        this.state.setTopologyTasks(msg.getTopologyTasks());

        Integer threadNum = msg.getTopology().getThreadParallelism().get(thisTaskInfo.getComponentId());
        this.tupleProcessChildren = new String[threadNum];
        this.lastChildUsed = 0;

        prepareTask();
        getSender().tell(new TaskPrepared(state.getTaskId()), getSelf());
    }

    private IComponent createExecutableComponent(Topology topology, String componentId)
            throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IComponentDescription component = topology.getDag().getComponent(componentId);

        JarClassLoader jcl = new JarClassLoader();
        for (String url : topology.getResourceUrls()) {
            jcl.add(new URL(url));
        }

        getContext().getSystem().setSystemClassLoader(jcl);
        Thread.currentThread().setContextClassLoader(jcl);

        ProxyProviderFactory.setDefaultProxyProvider( new CglibProxyProvider() );

        JclObjectFactory factory = JclObjectFactory.getInstance(false);
        String compClass = (String) component.getComponentDefinition();

        IComponent comp = (IComponent) factory.create(jcl, compClass);

        if (this.state.getTaskInfo().getFailStrategy() != TaskInfo.FailStrategy.NONE) {
            logger.info("恢复" + topology.getTopologyId().getTopologyId() + "-" + componentId + "的状态");
            Class compClz = jcl.loadClass(compClass);
            Method mtd = compClz.getMethod("restore");
            mtd.invoke(comp);
        }

        return comp;
    }

    private void prepareTask() {
        Topology topology = state.getTopology();
        try {
            IComponent executableComponent = createExecutableComponent(topology, state.getTaskInfo().getComponentId());
            state.setExecutableComponent(executableComponent);
            OutputFieldsDeclarer declare = new OutputFieldsDeclarer();
            executableComponent.defineOutputFields(declare);
            dagContext = new DAGContext(topology.getDag(), declare.getOutputStreams(), topology.getConfig(), state.getTaskInfo().getComponentId());
            this.componentTasks = createComponentTasksMap();

            if (executableComponent instanceof IEmitter) {
                ((IEmitter) executableComponent).open(topology.getConfig(), this);
            }
            else if (executableComponent instanceof IGear) {
                ((IGear) executableComponent).prepare(topology.getConfig(), this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void preStart() {
        super.preStart();
    }

    @Override
    public void send(ITuple anchor, List<Serializable> newValues) {
        send(IDAG.DEFAULT_STREAMID, anchor, newValues);
    }

    @Override
    public void send(Collection<ITuple> anchors, List<Serializable> newValues) {
        send(IDAG.DEFAULT_STREAMID, anchors, newValues);
    }

    @Override
    public void send(Serializable rootId, List<Serializable> newValues) {
        send(IDAG.DEFAULT_STREAMID, rootId, newValues);
    }

    @Override
    public void send(String streamId, ITuple anchor, List<Serializable> newValues) {
        List<ITuple> anchors = new ArrayList<>();
        if (anchor != null)
            anchors.add(anchor);

        send(streamId, anchors, newValues);
    }

    @Override
    public void send(String streamId, List<Serializable> newValues) {
        send(streamId, (ITuple) null, newValues);
    }

    @Override
    public void send(List<Serializable> newValues) {
        send((Serializable) null, newValues);
    }

    @Override
    public void send(String streamId, Serializable rootId, List<Serializable> newValues) {
        SimpleTuple tuple = new SimpleTuple();

        tuple.setMessageId(new MessageId());
        tuple.setStreamId(streamId);
        tuple.setValues(newValues);
        tuple.setSourceTaskId(state.getTaskId());
        tuple.setFields(dagContext.getOutputStreams().get(tuple.getStreamId()).getFields());
        tuple.setTimeout(state.getTopology().getTupleTimeout());
        if (rootId != null) {
            tuple.getRootIds().add(rootId);
            state.getPendingTuples().put(rootId.toString(), tuple);
            getContext().getSystem().getScheduler().scheduleOnce(tuple.getTimeout(), getSelf(), new CheckTupleTimeout(null, rootId), getSelf());
        }

        if (tuple.getRootIds().size() > 0) {
            SubmitTuple msg4Acker = new SubmitTuple(state.getTopology().getTopologyId(), tuple.getRootIds(), tuple.getMessageId(), tuple.getTimeout());
            msg4Acker.setTuple(SerializationUtils.serialize(tuple));
            clusterClient.tell(new ClusterClient.ClusterMessage("acker",
                    msg4Acker), getSelf());
        }

        send(tuple);
    }

    /**
     * 发送数据到指定的输出流
     *
     * @param streamId  指定的输出流id
     * @param newValues 需要发送的tuple的数据
     * @return 接受被发送tuple的task列表
     */
    @Override
    public void send(String streamId, Collection<ITuple> anchors, List<Serializable> newValues) {
        SimpleTuple tuple = new SimpleTuple();
        if (anchors != null) {
            for (ITuple anchor : anchors) {
                tuple.getRootIds().addAll(anchor.getRootIds());
            }
        }
        tuple.setMessageId(new MessageId());
        tuple.setStreamId(streamId);
        tuple.setValues(newValues);
        tuple.setSourceTaskId(state.getTaskId());
        tuple.setFields(dagContext.getOutputStreams().get(tuple.getStreamId()).getFields());

        if (tuple.getRootIds().size() > 0) {
            clusterClient.tell(new ClusterClient.ClusterMessage("acker",
                    new SubmitTuple(state.getTopology().getTopologyId(), tuple.getRootIds(), tuple.getMessageId(), tuple.getTimeout())), getSelf());
        }

        send(tuple);
    }

    private void send(ITuple tuple) {
        state.getTaskInfo().getTupleInfo().getProduced().incrementAndGet();

        List<Long> targetTasks = new ArrayList<Long>();

        // 从DAG上下文获取指定输出Stream的目的component以及相应的分组方式
        Map<String, Grouping> t = dagContext.getTargets(tuple.getStreamId());
        // 分别处理每个目的component
        for (Map.Entry<String, Grouping> entry : t.entrySet()) {
            Long targetTaskId = null;

            String targetCompId = entry.getKey();
            GroupingStrategy grouping = entry.getValue().getGroupingStrategy();

            List<Long> taskIds = componentTasks.get(targetCompId);

            switch (grouping) {
                case randomGrouping:
                    Random r = new Random(new Date().getTime());
                    int task = r.nextInt(taskIds.size());
                    targetTaskId = taskIds.get(task);
                    break;
                case directGrouping:
                    break;
                case fieldGrouping:
                    targetTaskId = selectTaskBasedOnField(entry.getValue().getFields(), taskIds, tuple);
                    break;
                case globalGrouping:
                    targetTaskId = selectTaskOfLowestId(taskIds);
                    break;
                case meanGrouping:
                    targetTaskId = selectTaskRoundRobin(targetCompId, taskIds);
                    break;
                default:
                    break;
            }

            if (targetTaskId != null) {
                targetTasks.add(targetTaskId);
                ReactiveRef targetTask = this.state.getTopologyTasks().get(targetTaskId).getEndpoint();
                if (targetTask == null) {
                    TaskInfo taskInfo = this.state.getTopologyTasks().get(targetTaskId);
                    targetTask = getContext().findComponent(taskInfo.getLocation().getHost(), taskInfo.getLocation().getPort(), "/task");
                    taskInfo.setEndpoint(targetTask);
                }
                targetTask.tell(new ReceiveTuple(tuple), getSelf());
            }
        }
    }

    private Long selectTaskRoundRobin(String targetCompId, List<Long> taskIds) {
        Integer lastIndex = meanGroupingLastTaskIndex.get(targetCompId);
        if (lastIndex == null)
            lastIndex = 0;
        else
            lastIndex = (lastIndex + 1) % taskIds.size();

        meanGroupingLastTaskIndex.put(targetCompId, lastIndex);
        return taskIds.get(lastIndex);
    }

    private Long selectTaskOfLowestId(List<Long> selectFromTasks) {
        return selectFromTasks.get(0);
    }

    /**
     * 根据hash值进行task的选择，用于根据Field进行分组的策略
     *
     * @param basedOnField    根据那些field进行分组
     * @param selectFromTasks 所有备选task, 已经按照hash值排序
     * @return
     */
    private Long selectTaskBasedOnField(Fields basedOnField, List<Long> selectFromTasks, ITuple tuple) {
        long hash = 0;
        for (int i = 0; i < tuple.getFields().size(); i++) {
            String field = tuple.getFields().get(i);
            if (basedOnField.contains(field)) {
                hash = 31 * hash + (tuple.getValue(i) != null ? hashValue(tuple.getValue(i)) : 0);
            }
        }

        int task = (int)(hash & 0xffffffffL) % selectFromTasks.size();
        return selectFromTasks.get(task);
    }

    private long hashValue(Serializable value) {
        return ConsistentHashUtil.hash(ConsistentHashUtil.computeMd5(value.toString()), 0);
    }

    @Override
    public void returnResult(Serializable result) {
        byte[] data = SerializationUtils.serialize(result);
        if (data.length > 1024 * 1000) {
            data = SerializationUtils.serialize("返回值长度[" + data.length + "]过大");
        }
        clusterClient.tell(new ClusterClient.ClusterMessage("leader",
                new FinishTopology(state.getTopology().getTopologyId(), data)), getSelf());
    }

    @Override
    public void report(Serializable msg) {
        clusterClient.tell(new ClusterClient.ClusterMessage("leader",
                new TopologyMessage(state.getTopology().getTopologyId(), SerializationUtils.serialize(msg))), getSelf());
    }

    @Override
    public GlobalTopologyId getTopologyId() {
        return this.state.getTopology().getTopologyId();
    }
}
