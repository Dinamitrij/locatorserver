package lv.div.locator.servlet.cron;

import lv.div.locator.dao.MLSDataDao;
import lv.div.locator.dao.StateDao;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Cleanup tables cron job
 */
public class CleanupTables extends HttpServlet {

    @EJB
    private StateDao stateDao;

    @EJB
    private MLSDataDao mlsDataDao;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        stateDao.cleanupAllStates();
        mlsDataDao.cleanupAllData();
        resp.getWriter().print("OK");
        resp.getWriter().flush();
    }

}
