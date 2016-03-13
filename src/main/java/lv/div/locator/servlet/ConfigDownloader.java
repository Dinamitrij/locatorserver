package lv.div.locator.servlet;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.commons.conf.Const;
import lv.div.locator.conf.Conf;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.dao.ConfigurationDao;
import lv.div.locator.healthcheck.AlertSender;
import lv.div.locator.model.Configuration;
import org.apache.commons.lang3.StringUtils;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Configuration file creator and downloader for particular device
 */
public class ConfigDownloader extends HttpServlet {

    private static final int BUFFER_SIZE = 1024;

    @EJB
    private ConfigurationDao configurationDao;

    @EJB
    private AlertSender alertSender;

    @EJB
    private ConfigurationManager configurationManager;

    private Logger log = Logger.getLogger(ConfigDownloader.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String deviceId = req.getParameter(Const.DEVICE_ID_HTTP_PARAMETER);

        if (!StringUtils.isBlank(deviceId)) {

            final Map<ConfigurationKey, Configuration> configForDevice =
                Conf.getInstance().deviceValues.get(deviceId);

            if (null != configForDevice) {
                configForDevice.clear();
            }

            resp.setContentType("text/plain");
            StringBuffer sb = new StringBuffer();

            if (StringUtils.isBlank(req.getParameter(Const.TEXT_OUTPUT_HTTP_PARAMETER))) {
                sb.append("# Locator properties ");
                sb.append(new Date());
                sb.append("\n\n");
                resp.setHeader("Content-Disposition", "attachment;filename=" + deviceId + ".conf");
            }

            final List<Configuration> resultList = configurationDao.listConfigByDeviceId(deviceId);

            try {

                if (null != resultList && !resultList.isEmpty()) {

                    configurationManager.loadDeviceSpecificConfiguration(deviceId, resultList);

                    for (Configuration configuration : resultList) {
                        // ATTENTION! Only STRING values will be exported!
                        sb.append(configuration.getKey());
                        sb.append(" = ");
                        sb.append(configuration.getValue());
                        sb.append("\n");
                    }
                }
            } catch (Exception e) {
                log.severe(e.getMessage());
            }

            InputStream input = new ByteArrayInputStream(sb.toString().getBytes("UTF8"));

            int read = 0;
            byte[] bytes = new byte[BUFFER_SIZE];
            OutputStream os = resp.getOutputStream();

            while ((read = input.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

//            final String alias =
//                Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.DEVICE_ALIAS).getValue();
//
//            alertSender.sendAlert(Conf.getInstance().globals.get(ConfigurationKey.ADMIN_ALERT_ADDRESS).getValue(),
//                                  Const.ADMIN_ALERT_TEXT_PREFIX + " Device \"" + alias +
//                                  "\" has loaded configuration.");

        }
    }

}
