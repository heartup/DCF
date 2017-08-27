package io.reactivej.dcf.common.container.process;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Java 子进程参数设置
 */
public class SubProcessExecuteParams {
    /**
     * 入口类
     */
    private String mainClass;
    /**
     * 初始堆大小
     */
    private long startingHeapSizeInMegabytes = 40;
    /**
     * 最大堆大小
     */
    private long maximumHeapSizeInMegabytes = 128;
    /**
     * 工作目录
     */
    private String workingDirectory;
    /**
     * 系统参数，通过-Ddcf.argname=argvalue 方式传入子进程中，参数名都定义在SubProcArgConfig中
     */
    private Map<String, String> systemArguments = new HashMap<String, String>();
    /**
     * class path
     */
    private List<String> classpathEntries = new ArrayList<String>();
    /**
     * 启动参数
     */
    private List<String> mainClassArguments = new ArrayList<String>();
    /**
     * java 运行环境命令
     */
    private String javaRuntime = "java";
    /**
     * java启动选项，用于调试
     */
    private String javaDebugOpts = null;

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public long getStartingHeapSizeInMegabytes() {
        return startingHeapSizeInMegabytes;
    }

    public void setStartingHeapSizeInMegabytes(long startingHeapSizeInMegabytes) {
        this.startingHeapSizeInMegabytes = startingHeapSizeInMegabytes;
    }

    public long getMaximumHeapSizeInMegabytes() {
        return maximumHeapSizeInMegabytes;
    }

    public void setMaximumHeapSizeInMegabytes(long maximumHeapSizeInMegabytes) {
        this.maximumHeapSizeInMegabytes = maximumHeapSizeInMegabytes;
    }

    public String getClasspath() {
        ClassLoader cl = SubProcessExecuteParams.class.getClassLoader();
        //递归获取类
        calClasspathEntries((URLClassLoader) cl);

        StringBuilder builder = new StringBuilder();
        int count = 0;
        final int totalSize = classpathEntries.size();
        for (String classpathEntry : classpathEntries) {
            builder.append(classpathEntry);
            count++;
            if (count < totalSize) {
                builder.append(System.getProperty("path.separator"));
            }
        }
        return builder.toString();
    }

    /**
     * 类路径
     */
    private void calClasspathEntries(URLClassLoader urlcl) {
        URL[] urls = urlcl.getURLs();
        for (URL url : urls) {
            addClasspathEntry(url.getFile());
        }
        if (urlcl.getParent() != null && urlcl.getParent() instanceof URLClassLoader) {
            calClasspathEntries((URLClassLoader) urlcl.getParent());
        }
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void addClasspathEntry(String classpathEntry) {
        this.classpathEntries.add(classpathEntry);
    }

    public void addArgument(String argument) {
        this.mainClassArguments.add(argument);
    }

    public List<String> getArgument() {
        return this.mainClassArguments;
    }

    /**
     * @return the systemArguments
     */
    public Map<String, String> getSystemArguments() {
        return systemArguments;
    }

    /**
     * @param systemArguments the systemArguments to set
     */
    public void setSystemArguments(Map<String, String> systemArguments) {
        this.systemArguments = systemArguments;
    }

    public void setJavaRuntime(String javaRuntime) {
        this.javaRuntime = javaRuntime;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public String getJavaRuntime() {
        return javaRuntime;
    }

    /**
     * @return the javaDebugOpts
     */
    public String getJavaDebugOpts() {
        return javaDebugOpts;
    }

    /**
     * @param javaDebugOpts the javaDebugOpts to set
     */
    public void setJavaDebugOpts(String javaDebugOpts) {
        this.javaDebugOpts = javaDebugOpts;
    }

}
