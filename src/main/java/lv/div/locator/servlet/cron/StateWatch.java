package lv.div.locator.servlet.cron;

import lv.div.locator.report.MLSReportSender;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Watcher for state [not]changing
 */
public class StateWatch extends HttpServlet {

    @Inject
    private Logger log;

    @EJB
    private MLSReportSender mlsReportSender;

//    private Logger log = Logger.getLogger(StateWatch.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mlsReportSender.sendMLSReportIfNeeded();
        resp.getWriter().print("OK " + new Date());
        resp.getWriter().flush();
    }

}
