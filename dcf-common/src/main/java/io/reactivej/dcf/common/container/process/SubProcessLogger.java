package io.reactivej.dcf.common.container.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * 子进程控制台日志输出
 *
 * @author heartup@gmail.com
 */
public class SubProcessLogger extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(SubProcessLogger.class);

    /**
     * 子进程
     */
    private JavaProcessContainer subProc;
    private Logger subProcLogger;

    public SubProcessLogger(JavaProcessContainer subProc, String loggerName) {
        this.setName("Process-" + loggerName);
        this.subProc = subProc;
        this.subProcLogger = LoggerFactory.getLogger(loggerName);
    }

    public void run() {
        logger.info("子进程{}控制台日志输出...", this.subProc.toString());
        try {
            Scanner scanner = new Scanner(subProc.getProcess().getInputStream(), "utf-8");
            while (scanner.hasNextLine()) {
                subProcLogger.info(scanner.nextLine());
            }
            scanner.close();
        } catch (Exception e) {
            logger.warn("子进程控制台日志发生未知异常.", e);
        }
        logger.info("子进程{}控制台日志退出", this.subProc.toString());
    }

}
