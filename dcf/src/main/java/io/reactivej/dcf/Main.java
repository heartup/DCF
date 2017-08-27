package io.reactivej.dcf;

import io.reactivej.dcf.common.container.process.SubProcArgConfig;
import io.reactivej.dcf.common.init.InitSystem;
import io.reactivej.dcf.common.init.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by heartup@gmail.com on 8/27/17.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // spring容器
    static private ClassPathXmlApplicationContext springContainer;

    public static void main(String[] args) {
        System.setProperty(SubProcArgConfig.DCF_ARGNAME, "node");  // used for logger

        try {
            InitSystem.initialized();
            String springConfigFile = ServerConfig.nodeSpringConfig;
            logger.info("配置路径：" + springConfigFile);

            // 启动spring容器
            springContainer = new ClassPathXmlApplicationContext(springConfigFile);
        } catch (Throwable e) {
            logger.warn("启动时发生未知异常", e);
        }
    }
}
