package io.reactivej.dcf.node.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LogTailer {

    public static final Logger logger = LoggerFactory.getLogger(LogTailer.class);

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private final File file;
    private Tailer tailer;
    private ScheduledFuture stopFuture;
    private List<String> lines = new ArrayList<>(1000);
    private final TailerListener listener = new TailerListenerAdapter() {
        @Override
        public void handle(String line) {
            synchronized (LogTailer.this) {
                if (lines.size() >= 1000) {
                    lines.clear();
                }

                lines.add(line);
            }
        }
    };

    public LogTailer(File file) {
        this.file = file;
    }

    public synchronized void start() {
        if (tailer == null) {
            logger.info("启动Tailer: " + file.getName());
            tailer = Tailer.create(file, listener, 3000, true);

            if (stopFuture != null) {
                stopFuture.cancel(true);
            }
            stopFuture = executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            }, 30, TimeUnit.SECONDS);
        }
    }

    public synchronized void stop() {
        if (tailer != null) {
            logger.info("停止Tailer: " + file.getName());
            tailer.stop();
            tailer = null;
            lines.clear();
        }
    }

    public synchronized List<String> getLog() {
        if (stopFuture != null) {
            stopFuture.cancel(true);
        }
        stopFuture = executorService.schedule(new Runnable() {
            @Override
            public void run() {
                stop();
            }
        }, 30, TimeUnit.SECONDS);

        if (tailer == null) {
            return Arrays.asList("网络异常，请刷新页面。");
        }

        List<String> cached = lines;
        lines = new ArrayList<>(1000);

        return cached;
    }
}
