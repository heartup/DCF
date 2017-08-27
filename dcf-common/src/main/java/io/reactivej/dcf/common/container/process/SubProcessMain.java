package io.reactivej.dcf.common.container.process;

import io.reactivej.dcf.common.init.InitSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 子进程引导主类
 * 
 * @author heartup@gmail.com
 * 
 */
public class SubProcessMain {
	private static final Logger logger = LoggerFactory.getLogger(SubProcessMain.class);

	// spring容器
	static private ClassPathXmlApplicationContext springContainer;

	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				System.err.println("未设置启动参数，必须传入子进程Spring配置文件！");
				return;
			}

			InitSystem.initialized();

			String springConfigFile = args[0];

			logger.info("子进程配置路径：" + springConfigFile);

			// 启动spring容器
			springContainer = new ClassPathXmlApplicationContext(springConfigFile);
			logger.info("进程启动完毕: " + System.getProperty(SubProcArgConfig.DCF_ARGNAME));
		} catch (Throwable e) {
			logger.warn("启动子进程时发生未知异常", e);
		}
	}

}
