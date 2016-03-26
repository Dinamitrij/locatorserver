package lv.div.locator.servlet.cron;

import lv.div.locator.report.MLSReportSender;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Watcher for state/location [not]changing.
 * Code for (each minute) CRON task.
 */
public class StateWatch extends HttpServlet {

    @EJB
    private MLSReportSender mlsReportSender;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mlsReportSender.sendMLSReportIfNeeded();
        resp.getWriter().print("OK " + new Date());
        resp.getWriter().flush();
    }

}
