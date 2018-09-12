package lv.div.locator.servlet;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.dao.BSSIDDao;
import lv.div.locator.dao.StateDao;
import lv.div.locator.healthcheck.AlertSender;
import lv.div.locator.model.BSSIDdata;
import lv.div.locator.model.GPSData;
import lv.div.locator.model.State;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Main Entry point for ThingSpeak signals
 */
public class BSSIDReceiverServlet extends HttpServlet {

    // Set the timezone:
    // rhc env-set JAVA_OPTS_EXT=" -Duser.timezone=Europe/Riga " --app locator2

    @EJB
    private BSSIDDao bssidDao;

    private Logger log = Logger.getLogger(BSSIDReceiverServlet.class.getName());

    @EJB
    private StateDao stateDao;

    @EJB
    private AlertSender alertSender;

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String data = req.getParameter("data");

        if (!StringUtils.isBlank(data)) {
            log.info("Http GET: BSSID received. " + data.length() + " bytes.");

            String device = StringUtils.EMPTY;
            String latitude = StringUtils.EMPTY;
            String longitude = StringUtils.EMPTY;
            String devicename = StringUtils.EMPTY;
            String battery = StringUtils.EMPTY;

            String wifiData = StringUtils.EMPTY;
            String safeZone = StringUtils.EMPTY;
            String accuracy = StringUtils.EMPTY;

            try {

                JSONParser j = new JSONParser();
                JSONObject o = (JSONObject) j.parse(data);

                device = (String) o.get("device");
                devicename = (String) o.get("devicename");
                latitude = (String) o.get("latitude");
                longitude = (String) o.get("longitude");
                accuracy = (String) o.get("accuracy");
                battery = (String) o.get("battery");

                // Register last signal from device:
                stateDao.registerLatestSignalFromDevice(device);

                JSONArray jsonMainArr = (JSONArray) o.get("bssids");
                StringBuffer wifiStringData = new StringBuffer();
                String comma = "";
                for (Object arrayElement : jsonMainArr) {

                    final JSONObject element = (JSONObject) arrayElement;
                    final String networkName = (String) element.get("name");
                    final String networkBSSID = (String) element.get("bssid");

                    BSSIDdata bssiDdata = new BSSIDdata();

                    bssiDdata.setDeviceId(device);
                    bssiDdata.setDeviceName(devicename);
                    bssiDdata.setLatitude(latitude);
                    bssiDdata.setLongitude(longitude);

                    if (latitude.trim().length() > GPSData.LAT_LON_DATA_LEN) {
                        bssiDdata.setLatitude(latitude.trim().substring(0, GPSData.LAT_LON_DATA_LEN));
                    } else {
                        bssiDdata.setLatitude(latitude);
                    }

                    if (longitude.trim().length() > GPSData.LAT_LON_DATA_LEN) {
                        bssiDdata.setLongitude(longitude.trim().substring(0, GPSData.LAT_LON_DATA_LEN));
                    } else {
                        bssiDdata.setLongitude(longitude);
                    }

                    final Long accuValue = Long.valueOf(accuracy);
                    bssiDdata.setAccuracy(accuValue);
                    bssiDdata.setWifiname(networkName);
                    bssiDdata.setBssid(networkBSSID);

                    wifiStringData.append(comma);
                    wifiStringData.append(networkName);
                    comma = "; ";

                    bssidDao.save(bssiDdata);

                }

                GPSData gpsData = new GPSData();
                gpsData.setDeviceId(device);
                gpsData.setDeviceName(devicename);
                final Long battery1 = Long.valueOf(battery);
                gpsData.setBattery(battery1);

                gpsData.setSpeed(StringUtils.EMPTY);

                if (latitude.trim().length() > GPSData.LAT_LON_DATA_LEN) {
                    gpsData.setLatitude(latitude.trim().substring(0, GPSData.LAT_LON_DATA_LEN));
                } else {
                    gpsData.setLatitude(latitude);
                }

                if (longitude.trim().length() > GPSData.LAT_LON_DATA_LEN) {
                    gpsData.setLongitude(longitude.trim().substring(0, GPSData.LAT_LON_DATA_LEN));
                } else {
                    gpsData.setLongitude(longitude);
                }

                String wifiDataSource = StringUtils.replace(wifiStringData.toString().trim(), "'", "`");
                if (wifiDataSource.length() > GPSData.DB_MAX_STRING_LEN) {
                    gpsData.setWifi(wifiDataSource.substring(0, GPSData.DB_MAX_STRING_LEN));
                } else {
                    gpsData.setWifi(wifiDataSource);
                }

                gpsData.setSafeNetwork("n/a");
                final Long accuValue = Long.valueOf(accuracy);
                gpsData.setAccuracy(accuValue);

                bssidDao.save(gpsData);

                resp.getWriter().print(device);
                resp.getWriter().flush();

            } catch (ParseException e) {
                log.warning("Parsing error! Cannot get BSSID data from JSON." + new Date());
            }

        } else {
//            log.log(Level.WARNING, "Empty BSSID data detected!");
        }

    }

}
