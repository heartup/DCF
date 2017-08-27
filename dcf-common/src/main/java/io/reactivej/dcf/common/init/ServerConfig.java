package io.reactivej.dcf.common.init;



/**
 * @author heartup@gmail.com
 *
 */
public class ServerConfig {

	public static String config="config.properties";

	public static String nodeSpringConfig = "META-INF/applicationContext-node.xml";
	public static String leaderSpringConfig = "META-INF/applicationContext-leader.xml";
	public static String ackerSpringConfig = "META-INF/applicationContext-acker.xml";
	public static String workerSpringConfig = "META-INF/applicationContext-worker.xml";
	public static String taskSpringConfig = "META-INF/applicationContext-task.xml";
}
