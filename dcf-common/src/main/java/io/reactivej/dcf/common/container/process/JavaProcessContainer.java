package io.reactivej.dcf.common.container.process;

import io.reactivej.dcf.common.container.IContainer;

import java.io.IOException;

/***
 * @author heartup@gmail.com
 */
public class JavaProcessContainer implements IContainer {

    private final SubProcessExecuteParams params;
    private volatile Process process;
    private volatile Thread loggerThread;
    private String processName;

    public JavaProcessContainer(SubProcessExecuteParams params) {
        this.params = params;
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void startContainer() throws IOException {
        JavaProcessBuilder builder = new JavaProcessBuilder(params);
        process = builder.startProcess();
        processName = builder.getProccessName();
        if(builder.isLogging()){
            // 子进程执行日志监控
            String logName = "subproc." + processName;
            loggerThread = new SubProcessLogger(this, logName);
            loggerThread.start();
        }
    }

    @Override
    public void killContainer() {
        process.destroy();
    }

    @Override
    public String toString() {
        return "Java Process-" + processName;
    }
}
