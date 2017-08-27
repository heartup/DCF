package io.reactivej.dcf.common.init;

import java.util.Arrays;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitSystem {
	private static Logger log = LoggerFactory.getLogger(InitSystem.class);

	private static Boolean inited = false;
	private static Object lock = new Object();

	public static void initialized() {
		// 初始化
		synchronized (lock) {
			if (inited) {
				return;
			}

			// 单元测试的时候为null
			ConfigGetThread configThread = new ConfigGetThread();
			// 启动
			configThread.start();
			// 主线程等待，直到配置初始化完成
			// 等待thread执行完
			try {
				// 系统参数
				configThread.join();

				// 初始化完成
				inited = true;
				log.info("系统初始化完毕，开始启动");
			} catch (Exception e) {
				log.error("configThread.join() occur exception.", e);
				System.exit(0);
			}
		}
	}

	static class ConfigGetThread extends Thread {

		public ConfigGetThread() {
			setName("ConfigGetThread");
			this.setDaemon(true);
		}

		public void run() {
			log.info("ConfigGetThread is runing...");
			String msg;
			try {
				// 加载配置文件
				log.info("从config.properties 加载配置...");
				SystemConfig.getConfig_properties().load(ClassLoader.getSystemResourceAsStream(ServerConfig.config));

				// 设置默认值
				SystemConfig.setDefaultValues();

				String strConfig = joinConfigurations();
				log.info("初始化系统配置成功, 配置详细如下: {} ", strConfig);

				// 设置到系统属性当中，供其它模块调用
				SystemConfig.setSystemProperties();

			} catch (Exception e) {
				msg = "SystemConfigGetThread 发生异常，系统将强制退出.";
				log.error(msg, e);
				System.exit(0);
			}
		}
	}

	private static String joinConfigurations() {
		StringBuilder sb = new StringBuilder();
		// 先对键值排序,按顺序打印更直观
		Object[] keys = SystemConfig.getConfig_properties().keySet().toArray();
		Arrays.sort(keys, new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
		});

		for (int i = 0; i < keys.length; i++) {
			Object key = keys[i];
			sb.append("\n\t");
			sb.append(key);
			sb.append("=");
			sb.append(SystemConfig.getConfig_properties().get(key));
		}
		return sb.toString();
	}

	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		InitSystem.log = log;
	}

}
