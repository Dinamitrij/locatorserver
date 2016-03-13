package lv.div.locator.servlet;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.conf.Conf;
import lv.div.locator.dao.ConfigurationDao;
import lv.div.locator.model.Configuration;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ServerConfigurationLoader extends HttpServlet {

    private static final String MESSAGE = "Global configuration reloaded.";

    @EJB
    private ConfigurationDao configuration;

    private Logger log = Logger.getLogger(ServerConfigurationLoader.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

//        log.log(Level.INFO, "ATTENTION! Reloading all configurations!");

        final Map<ConfigurationKey, Configuration> gconf = Conf.getInstance().globals;

        gconf.clear();
        Conf.getInstance().deviceValues.clear();

        final List<Configuration> resultList = configuration.listConfigByDeviceId("*");
        for (Configuration conf : resultList) {
            log.info("ConfigurationLoader loaded::: " + conf.getKey().toString());
            gconf.put(conf.getKey(), conf);
        }

//        if (gconf.get(ConfigurationKey.DEVICE_ADMIN_ALERT_ENABLED).getIntValue() == 1) {
//            AlertSender alertSender = new AlertSender();
//
//            alertSender.sendAlert(gconf.get(ConfigurationKey.ADMIN_ALERT_ADDRESS).getValue(),
//                                  Const.ADMIN_ALERT_TEXT_PREFIX + MESSAGE);
//
//        }

//        log.log(Level.INFO, MESSAGE);

    }
}
