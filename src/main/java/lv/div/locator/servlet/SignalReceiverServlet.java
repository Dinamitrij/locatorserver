package lv.div.locator.servlet;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.commons.conf.Const;
import lv.div.locator.conf.Conf;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.conf.GoogleMapReporter;
import lv.div.locator.dao.GPSDataDao;
import lv.div.locator.dao.MLSDataDao;
import lv.div.locator.dao.StateDao;
import lv.div.locator.healthcheck.AlertSender;
import lv.div.locator.model.Configuration;
import lv.div.locator.model.GPSData;
import lv.div.locator.model.State;
import org.apache.commons.lang3.StringUtils;
import javax.ejb.EJB;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main Entry point for mobile locator clients
 */
public class SignalReceiverServlet extends HttpServlet {

    // Set the timezone:
    // rhc env-set JAVA_OPTS_EXT=" -Duser.timezone=Europe/Riga " --app locator4

    @EJB
    private GPSDataDao gpsDataDao;

    @EJB
    private MLSDataDao mlsDataDao;

    @EJB
    private StateDao stateDao;

    private Logger log = Logger.getLogger(SignalReceiverServlet.class.getName());

    @EJB
    private AlertSender alertSender;

    @EJB
    private ConfigurationManager configurationManager;

    @EJB
    private GoogleMapReporter googleMapReporter;

    /**
     * Main signal receiver endpoint.
     * Here we're handling all requests from mobile client devices.
     *
     * @param req
     * @param resp
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        configurationManager.loadGlobalConfiguration();

        String latitude = req.getParameter(Const.GPS_LATITUDE_FIELD);
        String longitude = req.getParameter(Const.GPS_LONGITUDE_FIELD);
        String wifiData = req.getParameter(Const.WIFI_DATA_FIELD);
        String accuracy = req.getParameter(Const.ACCURACY_FIELD);
        String accelerometer = req.getParameter(Const.ACCELEROMETER_FIELD);
        String mlsData = req.getParameter(Const.MLS_FIELD);
        String safeZone = req.getParameter(Const.SAFE_ZONE_FIELD);
        String battery = req.getParameter(Const.BATTERY_LEVEL_FIELD);
        String speed = req.getParameter(Const.SPEED_FIELD);
        String deviceId = req.getParameter(Const.DEVICEID_FIELD);
        String deviceTimeMsec = req.getParameter(Const.DEVICETIME_FIELD);
        String line = StringUtils.EMPTY;

        log.info("Http GET: GPS coordinates received: " + latitude + ", " + longitude + ", deicetime=" + deviceTimeMsec);
        log.info("Http GET: MLS data received: " + mlsData);


        if (!StringUtils.EMPTY.equals(latitude) && !StringUtils.EMPTY.equals(longitude)) {

            configurationManager.loadDeviceSpecificConfigIfNeeded(deviceId);

            final String safeZoneName = isInSafeZone(deviceId, wifiData);

            if (!StringUtils.EMPTY.equals(safeZoneName)) {
                log.warning("@@@  " + getDeviceName(deviceId) + " IS IN safe zone. wifiData=" + wifiData);

                final State stateOfDevice = isInSafeZoneAlready(deviceId);
                if (null == stateOfDevice) { // 1st time entered Safe Zone
                    registerDeviceInSafeZone(deviceId, safeZoneName);
                } else {
                    updateDeviceInSafeZone(stateOfDevice, safeZoneName);
                }
            }

            if (StringUtils.EMPTY.equals(safeZoneName)) {

                log.warning("### " + getDeviceName(deviceId) + " NOT in safe zone!!! wifiData=" + wifiData);

                final State stateOfDevice = isOutOfSafeZoneAlready(deviceId);
                if (null == stateOfDevice) { // 1st time exited Safe Zone
                    registerDeviceOutOfSafeZone(deviceId);
                } else {
                    updateDeviceOutOfSafeZone(stateOfDevice);
                }

            }

            GPSData gpsData = new GPSData();
            gpsData.setDeviceId(deviceId);
            final Long battery1 = Long.valueOf(battery);
            gpsData.setBattery(battery1);
            gpsData.setDeviceTime(new Timestamp(Long.valueOf(deviceTimeMsec)));

            gpsData.setSpeed(speed);

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

            String wifiDataSource = StringUtils.replace(wifiData.trim(), "'", "`");
            if (wifiDataSource.length() > GPSData.DB_MAX_STRING_LEN) {
                gpsData.setWifi(wifiDataSource.substring(0, GPSData.DB_MAX_STRING_LEN));
            } else {
                gpsData.setWifi(wifiDataSource);
            }

            gpsData.setSafeNetwork(safeZoneName);

            if (StringUtils.isNumeric(accuracy)) {
                final Long accuValue = Long.valueOf(accuracy);
                gpsData.setAccuracy(accuValue);
            }

            if (StringUtils.isNumeric(accelerometer)) {
                final Long accelValue = Long.valueOf(accelerometer);
                gpsData.setAccelerometer(accelValue);
            }

            try {
                gpsDataDao.save(gpsData);

                if (!StringUtils.isBlank(mlsData)) {
                    StringBuffer sb = new StringBuffer();

                    final String[] mainMLSDataParts = StringUtils.split(mlsData, Const.HASH_SEPARATOR);

                    final String[] cellularData = StringUtils.split(mainMLSDataParts[0], Const.COMMA_SEPARATOR);

                    sb.append("{\"carrier\": \"BITE LV\",\"considerIp\": false");
                    sb.append(",\"homeMobileNetworkCode\": 1");
                    sb.append(",\"cellTowers\": [{");
                    sb.append("\"radioType\": \"");
                    sb.append(cellularData[1]);
                    sb.append("\"");
                    sb.append(",\"mobileCountryCode\": ");
                    sb.append(cellularData[4]);
                    sb.append(",\"mobileNetworkCode\": ");
                    sb.append(cellularData[5]);
                    sb.append(",\"locationAreaCode\": ");
                    sb.append(cellularData[3]);
                    sb.append(",\"cellId\": ");
                    sb.append(cellularData[2]);
                    sb.append(",\"age\": 1");
                    sb.append(",\"psc\": ");
                    sb.append(cellularData[6]);
                    sb.append(",\"signalStrength\": ");
                    sb.append(cellularData[7]);
                    sb.append(",\"timingAdvance\": 1");
                    sb.append("}],");


                    sb.append("\"wifiAccessPoints\": [");

                    if (mainMLSDataParts.length>1) {  // is there "mainMLSDataParts[1]" at all??

                        final String[] wifiMLSDataPart =
                            StringUtils.split(mainMLSDataParts[1], Const.WIFI_VALUES_SEPARATOR);

                        String valuesDivider = Const.EMPTY;
                        for (String wifi : wifiMLSDataPart) {

                            final String[] wifiNetworkData = wifi.split(Const.COMMA_SEPARATOR);
                            sb.append(valuesDivider);
                            sb.append("{\"macAddress\": \"");
                            sb.append(wifiNetworkData[0]);
                            sb.append("\", \"signalStrength\": \"");
                            sb.append(wifiNetworkData[1]);
                            sb.append("\"}");

                            valuesDivider = Const.COMMA_SEPARATOR;
                        }

                        sb.append("],");
                        sb.append("\"fallbacks\": {\"lacf\": true,\"ipf\": true}}");

                        googleMapReporter.registerNewMLSPoint(deviceId, getDeviceName(deviceId), sb.toString());
                    }  else {
                        log.warning("mainMLSDataParts LENGTH < 2! Skip `googleMapReporter.registerNewMLSPoint`");
                    }
                }

                alertSender.sendWifiInfo(deviceId, gpsData);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            log.warning("Empty GPS coordinates detected!");
        }

    }

    /**
     * Is device in safe zone?
     * Safe zone = zone within particular WiFi network range
     *
     * @param currentVisibleWifiNetworks
     *
     * @return
     */
    public String isInSafeZone(String deviceId, String currentVisibleWifiNetworks) {

        String result = StringUtils.EMPTY;

        final Configuration safeWifi =
            Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.SAFE_ZONE_WIFI);

        String safeWifis = safeWifi.getValue();
        String[] safeWifiPatternsWithNames = safeWifis.split(Const.WIFI_VALUES_SEPARATOR);
        for (String safeWifiPatternWithName : safeWifiPatternsWithNames) {

            String[] wifiValueAndAlias = safeWifiPatternWithName.split(Const.WIFI_NAME_SEPARATOR);
            if (currentVisibleWifiNetworks.matches(wifiValueAndAlias[0])) {
                result = wifiValueAndAlias[1];
                break;
            }

        }

        return result;

    }

    private void updateDeviceOutOfSafeZone(State stateOfDevice) {

        final String deviceId = stateOfDevice.getDeviceId();
        log.info("Updating " + getDeviceName(deviceId) + " LEAVE_SAFE_ZONE: " + deviceId);

        Integer outOfSafeZonePointsCount = stateOfDevice.getIntValue();
        final Integer newOutOfSafeZonePoints = outOfSafeZonePointsCount + 1;
        stateOfDevice.setIntValue(newOutOfSafeZonePoints);
        stateOfDevice.setDateValue(new Timestamp((new Date()).getTime()));
        stateDao.update(stateOfDevice); // Simply using already injected bean for common task

        final State inZone = isInSafeZoneAlready(deviceId);
        Integer inZoneTimes = 0;
        if (null != inZone) {
            inZoneTimes = inZone.getIntValue();
        }

        final Configuration leaveSafeConfiguration =
            Conf.getInstance().globals.get(ConfigurationKey.LEAVE_SAFE_ZONE_POINTS);
        final Configuration configuration = Conf.getInstance().globals.get(ConfigurationKey.ENTER_SAFE_ZONE_POINTS);

        if (outOfSafeZonePointsCount >= leaveSafeConfiguration.getIntValue()) {
            deleteDeviceState(deviceId, ConfigurationKey.IN_SAFE_ZONE);

//TODO: Enable map reporting...

            if (needReportMap(deviceId)) {
                googleMapReporter.reportGoogleMapGPSPath(deviceId);
                deleteDeviceState(deviceId, ConfigurationKey.MAP_REPORTED);
                saveMapReportedState(deviceId);
            }
        }

        if (inZoneTimes >= configuration.getIntValue() || outOfSafeZonePointsCount == 1) {

            deleteDeviceState(deviceId, ConfigurationKey.IN_SAFE_ZONE);

            final Map<ConfigurationKey, Configuration> deviceConfig =
                Conf.getInstance().deviceValues.get(deviceId);

            final Configuration telegramChatId = deviceConfig.get(ConfigurationKey.SEND_ALERT_ADDRESS_PARAM1);

            final Configuration leftSafeZoneMessage =
                Conf.getInstance().globals.get(ConfigurationKey.LEAVE_SAFE_ZONE_MESSAGE);

            String oldNetworkName = stateOfDevice.getValue();
            final String[] netName = StringUtils.split(oldNetworkName, Const.WIFI_NAME_SEPARATOR);
            if (netName.length > 1) {
                oldNetworkName = netName[1];
            }
            String messageText = String.format(leftSafeZoneMessage.getValue(), oldNetworkName);
            alertSender.sendZoneAdminAlert(stateOfDevice.getDeviceId(),
                                           AlertSender.LEFT_ZONE_TEXT_PREFIX + oldNetworkName);

            alertSender.sendZoneUserAlert(stateOfDevice.getDeviceId(), messageText);

        }

    }

    private void registerDeviceOutOfSafeZone(String deviceId) {
        State oldState = null;
        try {
            log.info(
                "lv.div.locator.servlet.SignalReceiverServlet.registerDeviceOutOfSafeZone - calling stateDao.findByDeviceAndKey");
            oldState = stateDao.findByDeviceAndKey(deviceId, ConfigurationKey.IN_SAFE_ZONE);
        } catch (Exception e) {
        }

        if (null != oldState) {
            final String oldWifiNetwork = oldState.getValue();
            saveOutOfSafeZoneState(deviceId, oldWifiNetwork);
        } else {
            saveOutOfSafeZoneState(deviceId, "NoName");
        }

        log.info("Registered " + getDeviceName(deviceId) + " OUT_OF_SAFE_ZONE: " + deviceId);
    }

    private void saveOutOfSafeZoneState(String deviceId, String oldWifiNetwork) {
        State outOfSafeZoneState = new State();
        outOfSafeZoneState.setDeviceId(deviceId);
        outOfSafeZoneState.setKey(ConfigurationKey.OUT_OF_SAFE_ZONE);
        outOfSafeZoneState.setIntValue(1);
        outOfSafeZoneState.setValue(oldWifiNetwork);
        stateDao.save(outOfSafeZoneState);
    }

    private void updateDeviceInSafeZone(State stateOfDevice, String safeNetwork) {

        log.info("Updating " + getDeviceName(stateOfDevice.getDeviceId()) + " ENTER_SAFE_ZONE: " +
                 stateOfDevice.getDeviceId());

        Integer safeZonePointsCount = stateOfDevice.getIntValue();
        final Integer newSafeZonePoints = safeZonePointsCount + 1;
        stateOfDevice.setIntValue(newSafeZonePoints);
        stateOfDevice.setDateValue(new Timestamp((new Date()).getTime()));
        stateDao.update(stateOfDevice);

        final State outOfZone = isOutOfSafeZoneAlready(stateOfDevice.getDeviceId());
        Integer outOfZoneTimes = 0;
        if (null != outOfZone) {
            outOfZoneTimes = outOfZone.getIntValue();
        }

        final Configuration enterSafeConfiguration =
            Conf.getInstance().globals.get(ConfigurationKey.ENTER_SAFE_ZONE_POINTS);
        final Configuration configuration = Conf.getInstance().globals.get(ConfigurationKey.LEAVE_SAFE_ZONE_POINTS);

        if (safeZonePointsCount >= enterSafeConfiguration.getIntValue()) {
            deleteDeviceState(stateOfDevice.getDeviceId(), ConfigurationKey.OUT_OF_SAFE_ZONE);
        }

        if (outOfZoneTimes >= configuration.getIntValue() ||
            safeZonePointsCount == 1) { // Long was out of zone! Need status update

            deleteDeviceState(stateOfDevice.getDeviceId(), ConfigurationKey.OUT_OF_SAFE_ZONE);

            final Map<ConfigurationKey, Configuration> deviceConfig =
                Conf.getInstance().deviceValues.get(stateOfDevice.getDeviceId());

            final Configuration telegramChatId = deviceConfig.get(ConfigurationKey.SEND_ALERT_ADDRESS_PARAM1);

            final Configuration safeZoneMessage =
                Conf.getInstance().globals.get(ConfigurationKey.ENTER_SAFE_ZONE_MESSAGE);

            String safeNetworkName = safeNetwork;
            final String[] netName = StringUtils.split(safeNetwork, Const.WIFI_NAME_SEPARATOR);
            if (netName.length > 1) {
                safeNetworkName = netName[1];
            }
            String messageText = //getDeviceName(stateOfDevice.getDeviceId()) + StringUtils.SPACE +
                String.format(safeZoneMessage.getValue(), safeNetworkName);
            alertSender
                .sendZoneAdminAlert(stateOfDevice.getDeviceId(),
                                    AlertSender.ENTERED_ZONE_TEXT_PREFIX + safeNetworkName);
            alertSender.sendZoneUserAlert(stateOfDevice.getDeviceId(), messageText);
            googleMapReporter.reportGoogleMap(stateOfDevice.getDeviceId());

        }

    }

    private void registerDeviceInSafeZone(String deviceId, String safeNetwork) {
        saveInSafeZoneState(deviceId, safeNetwork);
        log.info("Registered " + getDeviceName(deviceId) + " IN_SAFE_ZONE: " + deviceId);
    }

    private void saveInSafeZoneState(String deviceId, String safeNetwork) {
        // Cleanup old IN_SAFE_ZONE state, if any:
        stateDao.deleteByDeviceAndKey(deviceId, ConfigurationKey.IN_SAFE_ZONE);

        State safeZoneState = new State();
        safeZoneState.setDeviceId(deviceId);
        safeZoneState.setDeviceName(getDeviceName(deviceId));
        safeZoneState.setKey(ConfigurationKey.IN_SAFE_ZONE);
        safeZoneState.setIntValue(1);
        safeZoneState.setValue(safeNetwork);
        stateDao.save(safeZoneState);
    }

    private void saveMapReportedState(String deviceId) {
        State mapReportedState = new State();
        mapReportedState.setDeviceId(deviceId);
        final Date now = new Date();
        mapReportedState.setDateValue(new Timestamp(now.getTime()));
        mapReportedState.setDeviceName(getDeviceName(deviceId));
        mapReportedState.setKey(ConfigurationKey.MAP_REPORTED);
        stateDao.save(mapReportedState);
        log.info("ConfigurationKey.MAP_REPORTED state saved");
    }

    private void deleteDeviceState(String deviceId, final ConfigurationKey state) {
        stateDao.deleteByDeviceAndKey(deviceId, state);
        log.info("Device state deleted! " + state.toString());
    }

    private boolean needReportMap(String deviceId) {
        try {
            final State state = stateDao.findByDeviceAndKey(deviceId, ConfigurationKey.MAP_REPORTED);
            if (null != state) {
                final Date now = new Date();
                final Timestamp dateValue = state.getDateValue();
                final long diff = now.getTime() - dateValue.getTime();
                log.info("needReportMap TIME DIFF = " + diff);
                final boolean b = (diff > 60000);
                log.info("RETURNING " + b);
                return b;

            } else {
                return true;
            }
        } catch (NoResultException nre) {
            log.warning("MAP_REPORTED NoResultException");
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private State isInSafeZoneAlready(String deviceId) {
        //If "jumped" from 1 safe zone to another... Not handled!
        try {
            final State state = stateDao.findByDeviceAndKey(deviceId, ConfigurationKey.IN_SAFE_ZONE);
            return state;
        } catch (Exception e) {
            return null;
        }

    }

    private State isOutOfSafeZoneAlready(String deviceId) {
        //If "jumped" from 1 safe zone to another... Not handled!
        try {
            final State state = stateDao.findByDeviceAndKey(deviceId, ConfigurationKey.OUT_OF_SAFE_ZONE);
            return state;
        } catch (Exception e) {
            return null;
        }

    }

    private State getStateValue(String deviceId, String stateKey) {
        State result = null;
        try {
            result = stateDao.findByDeviceAndKey(deviceId, ConfigurationKey.valueOf(stateKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getDeviceName(String deviceId) {
        return Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.DEVICE_ALIAS)
            .getValue()
            .toUpperCase();
    }

}
