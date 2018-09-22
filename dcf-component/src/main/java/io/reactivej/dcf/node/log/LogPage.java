package io.reactivej.dcf.node.log;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LogPage extends AbstractHandler {

    private final String logBase;
    private ConcurrentHashMap<String, LogTailer> tailers = new ConcurrentHashMap<>();

    public static final String VIEW_FILE_PATH = "/view/";
    public static final String MORE_FILE_PATH = "/more/";
    public static final String DL_FILE_PATH = "/dl/";

    public static final String MORE_JS = "function newLogs() {\n" +
            "\tvar pathname = window.location.pathname;\n" +
            "\t$.get('/more/' + pathname.split('/')[2], function(lines) {\n" +
            "\t\tvar divObj = document.scrollingElement;\n" +
            "\t\tvar autoScroll = divObj.scrollTop >= divObj.scrollHeight - divObj.clientHeight;\n" +
            "\t\t$('#logDiv-%s').append(lines);\n" +
            "\t\tif (autoScroll)\n" +
            "\t\t\tdivObj.scrollTop = divObj.scrollHeight - divObj.clientHeight;\n" +
            "\t\t$('#waitDiv-%s').html('.');\n" +
            "\t});\n" +
            "}\n" +
            "var MyInterval=setInterval('newLogs()',3000);\n" +
            "function waitLogs() {\n" +
            "\tvar dots = $('#waitDiv-%s').html() + '...';\n" +
            "\t$('#waitDiv-%s').html(dots);\n" +
            "}\n" +
            "var MyInterval=setInterval('waitLogs()',1000);";

    public LogPage(String logBase) {
        this.logBase = logBase;
    }

    public String getLogBase() {
        return logBase;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (target.startsWith(VIEW_FILE_PATH)) {
            String fileName = target.replace(VIEW_FILE_PATH, "");
            viewFile(fileName, baseRequest, request, response);
        } else if (target.startsWith(MORE_FILE_PATH)) {
            String fileName = target.replace(MORE_FILE_PATH, "");
            moreLog(fileName, baseRequest, request, response);
        }
        else if (target.startsWith(DL_FILE_PATH)) {
            String fileName = target.replace(DL_FILE_PATH, "");
            downloadLog(fileName, baseRequest, request, response);
        }
    }

    private void downloadLog(String fileName, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(getLogBase() + fileName))) {
            response.setContentType("application/octet-stream");
            baseRequest.setHandled(true);
            ServletOutputStream out = response.getOutputStream();
            IOUtils.copy(fis, out);
        }
    }

    private void viewFile(final String fileName, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (tailers.get(fileName) == null) {
            LogTailer tailer = new LogTailer(new File(getLogBase() + fileName));
            LogTailer oldTailer = tailers.putIfAbsent(fileName, tailer);
            if (oldTailer == null) {
                tailer.start();
            } else {
                oldTailer.stop();
                oldTailer.start();
            }
        } else {
            tailers.get(fileName).stop();
            tailers.get(fileName).start();
        }

        String randDivId = UUID.randomUUID().toString();
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<head><script type=\"text/javascript\" src=\"http://apps.bdimg.com/libs/jquery/1.10.1/jquery.min.js\"></script></head>");
        response.getWriter().println("<script type=\"text/javascript\">");
        response.getWriter().println(String.format(MORE_JS, randDivId, randDivId, randDivId, randDivId));
        response.getWriter().print("</script>");
        response.getWriter().println("<body><div id='logDiv-" + randDivId + "' style=\"font-size: small;\"></div><div id='waitDiv-" + randDivId + "'></div></body>");
    }

    private void moreLog(final String fileName, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        LogTailer tailer = tailers.get(fileName);
        if (tailer != null) {
            List<String> logs = tailer.getLog();
            for (String log : logs) {
                response.getWriter().println(log);
                response.getWriter().print("</br>");
            }
        }
    }
}
