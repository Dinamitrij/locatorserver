package lv.div.locator.servlet.cron;

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

//    private Logger log = Logger.getLogger(StateWatch.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        log.info("CRON StateWatch called at " + (new Date()));

//        em.clear();
//        em.getTransaction().begin();
//        final Query query = em.createNamedQuery("State.truncate");
//        query.executeUpdate();
//        em.getTransaction().commit();

        resp.getWriter().print("OK " + new Date());
        resp.getWriter().flush();

    }

}
