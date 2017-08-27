package io.reactivej.dcf.mock;

import io.reactivej.*;
import io.reactivej.persist.NullJournal;
import io.reactivej.persist.NullStore;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * @author heartup@gmail.com
 */
public class ReactiveSystemMock  extends AbstractReactiveSystem {
    private static Logger logger = LoggerFactory.getLogger(DefaultReactiveSystem.class);

    private String systemId;

    private io.reactivej.dcf.mock.ClusterMock cluster;

    private Scheduler scheduler;

    private Dispatcher dispatcher;

    private AbstractTransporter transporter;

    static class ReactiveSystemThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public ReactiveSystemThreadFactory(String sysId) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = sysId + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    public ReactiveSystemMock() {
        System.out.println();
    }

    @Override
    public void init() {
        scheduler = new Scheduler(this);
        transporter = new TransporterMock(this);
        dispatcher = new Dispatcher(Executors.newCachedThreadPool(new ReactiveSystemThreadFactory(getSystemId())));

        ReactiveSystem prev = getCluster().getSystems().putIfAbsent(getPort(), this);
        if (prev != null) {
            logger.error("端口为" + getPort() + "的系统已经创建");
            System.exit(0);
        }

        try {
            RootComponent root = initRoot(getHost(), getPort(), getRootComponentClass());
            root.preStart();
        } catch (Exception e) {
            throw new ReactiveException(e);
        }

        createReactiveComponent(JOURNAL_NAME, true, NullJournal.class.getName());
        createReactiveComponent(STORE_NAME, true, NullStore.class.getName());
        createReactiveComponent(SHARED_JOURNAL_NAME, true, NullJournal.class.getName());
        createReactiveComponent(SHARED_STORE_NAME, true, NullStore.class.getName());
    }

    public ClusterMock getCluster() {
        return cluster;
    }

    public void setCluster(ClusterMock cluster) {
        this.cluster = cluster;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    @Override
    public String getHost() {
        return "127.0.0.1";
    }

    @Override
    public String getReactiveClusterId() {
        return getCluster().getId();
    }

    @Override
    public void createClusterSingleton(final String componentName, final boolean useGlobalDispatcher, final String className, final Object... params) {
        if (getCluster().getSingletons().putIfAbsent(componentName, getPort()) == null) {
            createReactiveComponent(componentName, useGlobalDispatcher, true, className, params);
        }
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public boolean isLocal(ReactiveRef ref) {
        return ref.getHost().equals(getHost()) && ref.getPort() == getPort();
    }

    @Override
    public void sendMessage(ReactiveRef receiver, Serializable message, ReactiveRef sender) {
        Envelope envlop = new Envelope(receiver, message, sender);
//        if (logger.isDebugEnabled()) {
//			if (!(envlop.getMessage() instanceof SystemMessage)
//					&& !(envlop.getMessage() instanceof ClusterClient.ClusterMessage))
            logger.debug("发送消息[" + envlop.toString() + "]");
//        }

        if (isLocal(receiver)) {
            Envelope newEnvelop = transportLocally(envlop);
            transporter.receiveMessage(receiver, newEnvelop);
        }
        else {
            transporter.sendMessage(receiver, envlop);
        }
    }

    private Envelope transportLocally(Envelope envlop) {
        return new Envelope(envlop.getReceiver(), (Serializable) SerializationUtils.clone(envlop.getMessage()), envlop.getSender());
    }

    @Override
    public ReactiveRef findSingleton(String singletonPath) {
        String peerId = getSingletonLocation(singletonPath);
        if (peerId == null)
            return null;

        String[] loc = peerId.split(":");
        return findComponent(loc[0], Integer.parseInt(loc[1]), componentSplitter + singletonPath);
    }

    @Override
    public String getSingletonLocation(String singletonPath) {
        Integer port = getCluster().getSingletons().get(singletonPath);
        if (port != null) {
            return "127.0.0.1:" + port;
        }
        else
            return null;
    }

    @Override
    public ReactiveRef findComponent(String host, int port, String path) {
        return super.findComponent(host, port, path);
    }

    @Override
    public AbstractTransporter getTransporter() {
        return transporter;
    }

    @Override
    public boolean supportSingleton() {
        return true;
    }

    @Override
    protected void createSharedJournalComponent() {

    }

    @Override
    protected void createSharedStoreComponent() {

    }
}
