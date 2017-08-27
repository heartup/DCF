package io.reactivej.dcf.worker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/***
 * @author heartup@gmail.com
 */
public class WorkerIDGen {
    private static final Logger logger = LoggerFactory.getLogger(WorkerIDGen.class);

    public static String WORKER_ID_CONF_FILE_PATH = System.getProperty("user.home") + System.getProperty("file.separator") + "dcf.id";

    private String workerId;

    public WorkerIDGen() {
        try {
            this.workerId = readFirstLineFromFile(WORKER_ID_CONF_FILE_PATH);
        } catch (IOException e) {
            logger.error("获取Worker ID异常", e);
            System.exit(0);
        }
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    private String readFirstLineFromFile(String path) throws IOException {
        try (BufferedReader br =
                     new BufferedReader(new FileReader(path))) {
            return br.readLine();
        }
    }
}
