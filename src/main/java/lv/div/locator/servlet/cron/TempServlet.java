package lv.div.locator.servlet.cron;

import lv.div.locator.conf.GoogleMapReporter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Watcher for state [not]changing
 */
public class TempServlet extends HttpServlet {

    @EJB
    private GoogleMapReporter gmr;

//    private Logger log = Logger.getLogger(TempServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

//        log.info("!!! StateWatch called at " + (new Date()));
        gmr.reportGoogleMap("00000000-50ac-431e-a81e-80c878ff86b3"); // Dima

//        em.clear();
//        em.getTransaction().begin();
//        final Query query = em.createNamedQuery("State.truncate");
//        query.executeUpdate();
//        em.getTransaction().commit();

        resp.getWriter().print("OK " + new Date());
        resp.getWriter().flush();

    }

}
