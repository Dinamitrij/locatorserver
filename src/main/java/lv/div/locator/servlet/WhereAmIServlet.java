package lv.div.locator.servlet;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.conf.Conf;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.dao.BSSIDDao;
import lv.div.locator.dao.MLSDataDao;
import lv.div.locator.healthcheck.AlertSender;
import lv.div.locator.model.Configuration;
import lv.div.locator.model.MLSData;
import lv.div.locator.model.mlsfences.JsonHelper;
import lv.div.locator.model.mlsfences.MlsFence;
import lv.div.locator.model.mlsfences.SafeAreas;
import lv.div.locator.model.mlsfences.polyline.LatLng;
import lv.div.locator.model.mlsfences.polyline.PolyUtil;
import lv.div.locator.mqueue.ZoneDataMQSender;
import org.apache.commons.lang3.StringUtils;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Determines device location according to last MLS position.
 * Detects is the mentioned device is in safe zone/area
 */
public class WhereAmIServlet extends HttpServlet {

    @EJB
    private BSSIDDao bssidDao;

    private Logger log = Logger.getLogger(WhereAmIServlet.class.getName());

    @EJB
    private AlertSender alertSender;

    @EJB
    private MLSDataDao mlsDataDao;

    @EJB
    private ZoneDataMQSender zoneDataMQSender;

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String deviceId = req.getParameter("device"); //device id

        if (StringUtils.isBlank(deviceId)) {
            emptyResult(resp);
            return;
        }
        log.info("Http GET: WhereAmI request received from " + deviceId);

        configurationManager.loadDeviceSpecificConfigIfNeeded(deviceId);
        final Map<ConfigurationKey, Configuration> globals = Conf.getInstance().globals;

        final Map<ConfigurationKey, Configuration> deviceConfig =
            Conf.getInstance().deviceValues.get(deviceId);
        final Configuration mlsFencesContainer =
            deviceConfig.get(ConfigurationKey.DEVICE_MLS_FENCES);

        final String jsonForSafeAreas = mlsFencesContainer.getValue();
        JsonHelper jsonHelper = new JsonHelper();
        final SafeAreas safeAreas = (SafeAreas) jsonHelper.buildPojo(jsonForSafeAreas, SafeAreas.class);
        final List<MlsFence> mlsFences = safeAreas.getMlsFences();

        final List<MLSData> mlsPoints = mlsDataDao.listTwoLastMLSPoints(deviceId);
        boolean deviceInSafeArea = false;
        String safeZoneName = StringUtils.EMPTY;

        if (mlsPoints.size() >= 1) {
            final MLSData mlsData = mlsPoints.get(0);

            for (MlsFence mlsFence : mlsFences) {
                final List<LatLng> safeArea = PolyUtil
                    .prepareCircleFromRadius(new LatLng(mlsFence.getLatitude(), mlsFence.getLongitude()),
                                             mlsFence.getRadiusInMeters(), mlsFence.getNumberOfPoints());

                deviceInSafeArea = PolyUtil.containsLocation(
                    new LatLng(Double.valueOf(mlsData.getLatitude()), Double.valueOf(mlsData.getLongitude())), safeArea,
                    true);
                if (deviceInSafeArea) {
                    safeZoneName = mlsFence.getName();
                    break;
                }

            }

        }


        StringBuffer sb = new StringBuffer(deviceConfig.get(ConfigurationKey.DEVICE_ALIAS).getValue());

//        if (message.indexOf(ENTERED_ZONE_TEXT_PREFIX) > -1) {
//        //                sb.append(" \uD83C\uDFC1 "); // Kletch flag sign
//                        sb.append(" \uD83C\uDFE0 "); // Domik
//                    } else {
//                        sb.append(" \uD83D\uDEA7 "); // Yellow construction zone
//                    }
//
        if (deviceInSafeArea) {
            sb.append(" \uD83C\uDFE0 "); // Domik
            sb.append(" in safe zone: ");
            sb.append(safeZoneName);
            alertSender.sendAlert(globals.get(ConfigurationKey.ADMIN_ALERT_ADDRESS).getValue(), sb.toString());
        } else {
            sb.append(" \uD83D\uDEA7 "); // Yellow construction zone
            sb.append(" NOT in safe zone");
            alertSender.sendAlert(globals.get(ConfigurationKey.ADMIN_ALERT_ADDRESS).getValue(), sb.toString());
        }


        sb.append(" +RabbitMQ!");
        zoneDataMQSender.sendData("MLSFences", deviceId, deviceId, sb.toString());



        if (mlsPoints.isEmpty()) {
            log.warning("No MLSData points loaded for deviceId = " + deviceId + ". (skip reporting)");
            emptyResult(resp);
            return;
        }

        resp.getWriter().print("OK");
        resp.getWriter().flush();

    }

    /**
     * Empty result reply
     *
     * @param resp
     *
     * @throws IOException
     */
    private void emptyResult(HttpServletResponse resp) throws IOException {
        resp.getWriter().print(StringUtils.EMPTY);
        resp.getWriter().flush();
    }

}
