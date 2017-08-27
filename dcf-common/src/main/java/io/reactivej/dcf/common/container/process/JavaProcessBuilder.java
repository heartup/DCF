package io.reactivej.dcf.common.container.process;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/***
 * Java 进程构建器
 */
public class JavaProcessBuilder {
    private static final Logger logger = LoggerFactory.getLogger(JavaProcessBuilder.class);
    private SubProcessExecuteParams execParams;

    public JavaProcessBuilder(SubProcessExecuteParams execParams) {
        this.execParams = execParams;
    }

    public Process startProcess() throws IOException {
        List<String> argumentsList = new ArrayList<String>();
        argumentsList.add(execParams.getJavaRuntime());
        // 调试参数
        if (StringUtils.isNotEmpty(execParams.getJavaDebugOpts())) {
            argumentsList.add(execParams.getJavaDebugOpts());
        }
        argumentsList.add(MessageFormat.format("-Xms{0}M", String.valueOf(execParams.getStartingHeapSizeInMegabytes())));
        argumentsList.add(MessageFormat.format("-Xmx{0}M", String.valueOf(execParams.getMaximumHeapSizeInMegabytes())));

        //系统参数，子进程配置
        for (Entry<String, String> sysArg : execParams.getSystemArguments().entrySet()) {
            argumentsList.add("-D" + sysArg.getKey() + "=" + sysArg.getValue());
        }

        argumentsList.add("-cp");
        argumentsList.add(execParams.getClasspath());
        argumentsList.add(execParams.getMainClass());
        for (String arg : execParams.getArgument()) {
            argumentsList.add(arg);
        }
        // 转换成数组
        String[] cmdLineArray = argumentsList.toArray(new String[argumentsList.size()]);

        String cmdLine = StringUtils.join(cmdLineArray, " ");
        logger.info(cmdLine);

        try {
            Process ps = Runtime.getRuntime().exec(cmdLine);
            return ps;
        } catch (IOException e) {
            logger.warn("启动子进程发生未知异常", e);
            throw e;
        }
    }

    /**
     * 启动子进程后是否跟踪输出日志
     *
     * @return
     */
    public boolean isLogging() {
        String strIsLog = this.execParams.getSystemArguments().get(SubProcArgConfig.DCF_ISLOG);
        return strIsLog.equalsIgnoreCase("true");
    }

    public String getProccessName() {
        return this.execParams.getSystemArguments().get(SubProcArgConfig.DCF_ARGNAME);
    }
}