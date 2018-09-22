package io.reactivej.dcf.node.log;


import org.eclipse.jetty.server.Server;

public class LogServer extends Server {

    public LogServer(int port, String logBase) {
        super(port);
        setHandler(new LogPage(logBase));
    }

}
