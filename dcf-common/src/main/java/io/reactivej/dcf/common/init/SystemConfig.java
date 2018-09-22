package io.reactivej.dcf.common.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 配置
 *
 * @author heartup@gmail.com
 */
public class SystemConfig {

    private static final Logger log = LoggerFactory.getLogger(SystemConfig.class);

    public static final String SYSTEM_PORT = "SYSTEM_PORT";
    public static final String WORKER_PORT = "WORKER_PORT";
    public static final String MEMORY_NEEDED = "MEMORY_NEEDED";
    public static final String TASK_ID = "TASK_ID";
    public static final String FAIL_STRATEGY = "FAIL_STRATEGY";
    public static final String PERSIST_ID = "PERSIST_ID";

    // zk集群配置
    public static final String zk_servers = "zk.servers";
    public static final String zk_namespace = "zk.namespace";

    // r2m 配置
    public static final String r2m_zk_address = "r2m.zk.address";
    public static final String r2m_id = "r2m.id";

    public static final String node_port = "node.port";

    public static final String leader_host = "leader.host";
    public static final String leader_port = "leader.port";
    public static final String leader_java_debug = "leader.java.debug";
    public static final String leader_java_xms = "leader.java.xms";
    public static final String leader_java_xmx = "leader.java.xmx";

    public static final String worker_port = "worker.port";
    public static final String worker_java_debug = "worker.java.debug";
    public static final String worker_java_xms = "worker.java.xms";
    public static final String worker_java_xmx = "worker.java.xmx";
    public static final String heartbeat_interval = "heartbeat.interval";

    public static final String task_port_min = "task.port.min";
    public static final String task_port_max = "task.port.max";
    public static final String task_debug = "task.debug";

    public static final String acker_host = "acker.host";
    public static final String acker_port = "acker.port";
    public static final String acker_java_debug = "acker.java.debug";
    public static final String acker_java_xms = "acker.java.xms";
    public static final String acker_java_xmx = "acker.java.xmx";

    // 保存系统属性的地方
    private static final Properties Config_properties = new Properties();

    public static void tryPut(String key, String value) {
        if (!Config_properties.containsKey(key)) {
            Config_properties.setProperty(key, value);
        }
    }

    public static boolean containsKey(String key) {
        return Config_properties.containsKey(key);
    }

    /**
     * 替换系统属性，返回旧值
     *
     * @param key
     * @param value
     * @return
     */
    public static String setValue(String key, String value) {
        String oldVal = null;
        if (Config_properties.containsKey(key)) {
            oldVal = Config_properties.getProperty(key);
        }
        // 设置
        Config_properties.setProperty(key, value);

        return oldVal;
    }

    public static String getValue(String key) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            return Config_properties.getProperty(key);
        } else {
            throw new RuntimeException("配置" + key + "不存在");
        }
    }

    public static String getValue(String key, String defaultVal) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            return Config_properties.getProperty(key, defaultVal);
        } else {
            return defaultVal;
        }
    }

    public static int getIntValue(String key) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            return Integer.parseInt(Config_properties.getProperty(key));
        } else {
            throw new RuntimeException("配置" + key + "不存在");
        }
    }

    public static long getLongValue(String key) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            return Long.parseLong(Config_properties.getProperty(key));
        } else {
            throw new RuntimeException("配置" + key + "不存在");
        }
    }

    public static long getLongValue(String key, long defaultVal) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            return Long.parseLong(Config_properties.getProperty(key));
        } else {
            return defaultVal;
        }
    }

    public static int getIntValue(String key, int defaultVal) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            String value = Config_properties.getProperty(key);
            return Integer.parseInt(value);
        } else {
            return defaultVal;
        }
    }

    public static boolean getBoolValue(String key) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            return Boolean.parseBoolean(Config_properties.getProperty(key));
        } else {
            throw new RuntimeException("配置" + key + "不存在");
        }
    }

    public static boolean getBoolValue(String key, boolean defaultVal) {
        // 先取文件配置，如果没有则从数据库表中获取
        if (Config_properties.containsKey(key)) {
            return Boolean.parseBoolean(Config_properties.getProperty(key));
        } else {
            return defaultVal;
        }
    }

    public static Properties getConfig_properties() {
        return Config_properties;
    }

    // 设置到系统属性当中，供其它模块调用
    public static void setSystemProperties() {
        log.info("设置系统属性...");
    }

    public static void setDefaultValues() {
        log.info("设置系统属性...");

        // 系统通信端口
        if (System.getProperty(SYSTEM_PORT) == null) {
            System.setProperty(SYSTEM_PORT, getValue(node_port));
        }
    }
}
