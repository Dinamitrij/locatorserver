package lv.div.locator.healthcheck;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.commons.conf.Const;
import lv.div.locator.conf.Conf;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.dao.ConfigurationDao;
import lv.div.locator.model.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Servlet for sending the "pings".
 * Used mostly for debug purposes, to see the device is still up and running.
 */
public class PingServlet extends HttpServlet {

    // Set the timezone:
    // rhc env-set JAVA_OPTS_EXT=" -Duser.timezone=Europe/Riga " --app locator

    @EJB
    private ConfigurationDao configuration;

    @EJB
    private ConfigurationManager configurationManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String deviceId = req.getParameter(Const.DEVICE_ID_HTTP_PARAMETER);
        String pingText = req.getParameter(Const.TEXT_OUTPUT_HTTP_PARAMETER);

        if (StringUtils.isBlank(deviceId)) {
            return; // TRASH ping...
        }

        if (StringUtils.isBlank(pingText)) {
            pingText = "Ping.";
        }

        configurationManager.loadDeviceSpecificConfigIfNeeded(deviceId);

        if (Conf.getInstance().deviceValues.get(deviceId) == null) {
            return;
        }
        final Map<ConfigurationKey, Configuration> config =
            Conf.getInstance().deviceValues.get(deviceId);

        final String urlMask = config.get(ConfigurationKey.DEVICE_PING_ADDRESS).getValue();

        final String afterMinutes =
            Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.DEVICE_PING_MINUTES)
                .getValue();

        DateTime now = new DateTime();
        final int updateHour = now.plusMinutes(Integer.parseInt(afterMinutes)).getHourOfDay();
        final int updateMinute = now.plusMinutes(Integer.parseInt(afterMinutes)).getMinuteOfHour();
        String nextPing = ". Next after " + afterMinutes + "min., ~" +
                          String.format(Const.TIME_VALUE_FORMAT, updateHour) + ":" +
                          String.format(Const.TIME_VALUE_FORMAT, updateMinute);

        String text =
            config.get(ConfigurationKey.DEVICE_ALIAS).getValue().toUpperCase() + ": " + pingText + nextPing;

        AlertSender alertSender = new AlertSender();
        alertSender.sendAlert(urlMask, text);
        resp.getWriter().print(Const.DEFAULT_SERVLET_ANSWER + StringUtils.SPACE + new Date());
        resp.getWriter().flush();

    }
}
