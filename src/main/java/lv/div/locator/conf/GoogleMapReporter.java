package lv.div.locator.conf;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.commons.conf.Const;
import lv.div.locator.dao.GPSDataDao;
import lv.div.locator.dao.StateDao;
import lv.div.locator.model.Configuration;
import lv.div.locator.model.GPSData;
import lv.div.locator.model.MLSData;
import lv.div.locator.model.State;
import lv.div.locator.servlet.Statistics;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Stateless
public class GoogleMapReporter {

    @EJB
    private ConfigurationManager configurationManager;

    private Logger log = Logger.getLogger(GoogleMapReporter.class.getName());

    @EJB
    private GPSDataDao gpsDataDao;

    @EJB
    private StateDao stateDao;

    public void reportGoogleMapGPSPath(final String deviceId) {

        final List<GPSData> resultList = listLastNonSafeAfterReported(deviceId);

        if (null != resultList && resultList.size() >=
                                  Statistics.GPS_POINTS_COUNT_FOR_REPORT) { // Only report, if we've got "N" values ahead...

            saveLastReportedGPSPoint(resultList.get(0), deviceId);

            final StringBuffer sb = new StringBuffer(
                "http://maps.google.com/maps/api/staticmap?");
            try {

                final GPSData lastPoint = resultList.get(0);

                sb.append("center=");
                sb.append(lastPoint.getLatitude());
                sb.append(",");
                sb.append(lastPoint.getLongitude());
                sb.append("&zoom=15&size=300x300&maptype=terrain&sensor=false");

                for (int i = resultList.size() - 1; i > 0; i--) {
                    final GPSData gpsData = resultList.get(i);
                    sb.append("&markers=color:blue|label:");
                    sb.append(i);
                    sb.append("|");
                    sb.append(gpsData.getLatitude());
                    sb.append(",");
                    sb.append(gpsData.getLongitude());
                }

                sb.append("&markers=color:red|label:x");
                sb.append("|");
                sb.append(lastPoint.getLatitude());
                sb.append(",");
                sb.append(lastPoint.getLongitude());

            } catch (Exception e) {
                e.printStackTrace();
            }

            HttpClient c = new DefaultHttpClient();
            String line = null;
            String staticMapShortenedUrl = null;
            String staticMapUrl = StringUtils.EMPTY;
            try {
                //http://is.gd/create.php?format=simple&url=%s

                staticMapUrl = URLEncoder.encode(sb.toString(), "UTF-8");
                HttpGet suHttpGet = new HttpGet("http://is.gd/create.php?format=simple&url=" + staticMapUrl);

                HttpResponse r = c.execute(suHttpGet);

                BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));

                while ((line = rd.readLine()) != null) {
                    staticMapShortenedUrl = line;
                }

                if (!StringUtils.isBlank(staticMapShortenedUrl)) {
                    sendGoogleMapReport(deviceId, staticMapShortenedUrl, "GPS");
                }

            } catch (Exception e) {
                log.severe("Cannot get short URL for Google Map reporting. Using long one.");
                if (!StringUtils.isBlank(staticMapUrl)) {
                    sendGoogleMapReport(deviceId, staticMapUrl, "GPS");
                }
            }

        }
    }

    public void reportGoogleMap(final String deviceId) {

        final List<GPSData> resultList = findLastNonSafe(deviceId);
        if (null != resultList && resultList.size() > 0) {

            for (GPSData gpsData : resultList) {
                if (!Const.ZERO_COORDINATE.equals(gpsData.getLatitude())) {
                    sendReportForOnePoint(deviceId, gpsData);
                    break; // Only 1 point is needed
                }
            }

        } else {
            log.warning("Last 1 Non-Safe point(s) without GPS coordinates / No results for GPS query");
        }
    }

    private void sendReportForOnePoint(String deviceId, GPSData gpsData) {
        HttpClient c = new DefaultHttpClient();
        String line = null;
        String staticMapShortenedUrl = null;
        final String lat = gpsData.getLatitude();
        final String lon = gpsData.getLongitude();

        String staticMapUrl = StringUtils.EMPTY;

        if (!Const.ZERO_COORDINATE.equals(lat)) {
            try {
                //http://is.gd/create.php?format=simple&url=%s

                staticMapUrl = URLEncoder.encode(
                    "http://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon +
                    "&zoom=15&size=300x300&maptype=terrain&sensor=false&markers=color:red|label:x|" + lat + "," +
                    lon,
                    "UTF-8");
                HttpGet suHttpGet = new HttpGet("http://is.gd/create.php?format=simple&url=" + staticMapUrl);

                HttpResponse r = c.execute(suHttpGet);

                BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));

                while ((line = rd.readLine()) != null) {
                    staticMapShortenedUrl = line;
                }

                if (!StringUtils.isBlank(staticMapShortenedUrl)) {
                    sendGoogleMapReport(deviceId, staticMapShortenedUrl, "GPS");
                }

            } catch (Exception e) {
                log.severe("Cannot get short URL for Google Map reporting. Using long one.");
                if (!StringUtils.isBlank(staticMapUrl)) {
                    sendGoogleMapReport(deviceId, staticMapUrl, "GPS");
                }
            }
        }
    }

    //TODO: refactor to periodical (each minute report, getting data from database)!!!
    public MLSData sendMLSReport(String deviceId, String mlsJson) {

        HttpClient c = new DefaultHttpClient();
        String line = null;
        String staticMapShortenedUrl = null;

        String lat = Const.EMPTY;
        String lon = Const.EMPTY;
        String acc = Const.EMPTY;

        MLSData mlsData = new MLSData();

        try {
            HttpPost httpPost = new HttpPost("https://location.services.mozilla.com/v1/geolocate?key=test");
            StringEntity input = new StringEntity(mlsJson);
            input.setContentType("application/json");
            httpPost.setEntity(input);

            final HttpResponse executeResp = c.execute(httpPost);

            BufferedReader br = new BufferedReader(
                new InputStreamReader((executeResp.getEntity().getContent())));

            String output;
            StringBuffer dataFromMLSserver = new StringBuffer();
            while ((output = br.readLine()) != null) {
                dataFromMLSserver.append(output);
            }

            final String mlsResponse = dataFromMLSserver.toString();
            log.info("MLS server response: " + mlsResponse);

            JSONObject json = (JSONObject) new JSONParser().parse(mlsResponse);
            final JSONObject location = (JSONObject) json.get("location");
            Double dLat = (Double) location.get("lat");
            Double dLon = (Double) location.get("lng");
            Double accuracy = (Double) json.get("accuracy");

            lat = String.valueOf(dLat);
            lon = String.valueOf(dLon);
            acc = String.valueOf(accuracy);

            log.info("lat, lon = " + lat + ", " + lon);
            log.info("accuracy = " + acc);

            mlsData.setLatitude(lat);
            mlsData.setLongitude(lon);
            mlsData.setAccuracy(accuracy);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        final Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int minutes = calendar.get(Calendar.MINUTE);

        String staticMapUrl = StringUtils.EMPTY;
        if (!Const.EMPTY.equals(lat) && !Const.ZERO_COORDINATE.equals(lat) &&
            (minutes % 3 == 0)
            ) {
            try {
                //http://is.gd/create.php?format=simple&url=%s

                staticMapUrl = URLEncoder.encode(
                    "http://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon +
                    "&zoom=15&size=300x300&maptype=terrain&sensor=false&markers=color:green|label:x|" + lat + "," +
                    lon,
                    "UTF-8");
                HttpGet suHttpGet = new HttpGet("http://is.gd/create.php?format=simple&url=" + staticMapUrl);

                HttpResponse r = c.execute(suHttpGet);

                BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));

                while ((line = rd.readLine()) != null) {
                    staticMapShortenedUrl = line;
                }

                if (!StringUtils.isBlank(staticMapShortenedUrl)) {
                    sendGoogleMapReport(deviceId, staticMapShortenedUrl, "MLS");
                }

                return mlsData;

            } catch (Exception e) {
                e.printStackTrace();
                log.severe("Cannot get short URL for Google Map reporting. Using long one.");
                if (!StringUtils.isBlank(staticMapUrl)) {
                    sendGoogleMapReport(deviceId, staticMapUrl, "MLS");
                    return mlsData;
                }
            }
        } else {
            return mlsData;
        }

        return null;
    }

    public void sendGoogleMapReport(final String deviceId, final String staticMapShortenedUrl, String title) {

        configurationManager.loadDeviceSpecificConfigIfNeeded(deviceId);

        final Map<ConfigurationKey, Configuration> deviceConfig =
            Conf.getInstance().deviceValues.get(deviceId);

        final Configuration telegramChatId = deviceConfig.get(ConfigurationKey.SEND_ALERT_ADDRESS_PARAM1);



        final String messageText =
            deviceConfig.get(ConfigurationKey.DEVICE_ALIAS).getValue() +
            ":Last%20*" + title + "*%20points[.](" + staticMapShortenedUrl + ")";
        String url = String.format(Conf.getInstance().globals.get(ConfigurationKey.SEND_ALERT_ADDRESS).getValue(),
                                   telegramChatId.getValue(),
                                   messageText);

        HttpClient c = new DefaultHttpClient();
        final HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse r = c.execute(httpGet);
            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
            }
        } catch (Exception e) {
        }

    }

    public List<GPSData> listLastNonSafeAfterReported(final String deviceId) {

        try {

            final State state = stateDao.findLastReportedByDevice(deviceId);
            Integer lastIdToFindAfter = 0;

            if (null != state) {
                lastIdToFindAfter = state.getIntValue();
            }

            final Map<ConfigurationKey, Configuration> deviceConfig =
                Conf.getInstance().deviceValues.get(deviceId);
            final Configuration gpsAccuracyThreshold =
                deviceConfig.get(ConfigurationKey.DEVICE_MAP_REPORT_GPS_ACCURACY_THRESHOLD);
            return gpsDataDao
                .findLastNonSafeAfterReported(deviceId, gpsAccuracyThreshold.getIntValue(), lastIdToFindAfter);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }

    }

    public void saveLastReportedGPSPoint(GPSData point, String deviceId) {
        try {
            stateDao.cleanupLastReportedGPSPoint(deviceId);

            State lastGpsState = new State();
            lastGpsState.setDeviceId(deviceId);

            final String deviceName = Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.DEVICE_ALIAS)
                .getValue()
                .toUpperCase();

            lastGpsState.setDeviceName(deviceName);
            lastGpsState.setKey(ConfigurationKey.LAST_REPORTED_GPS_POINT);
            lastGpsState.setIntValue(point.getId().intValue());

            stateDao.save(lastGpsState);

            log.info("ConfigurationKey.LAST_REPORTED_GPS_POINT - saved!!!");

        } catch (Exception e) {
            log.severe("Cannot save ConfigurationKey.LAST_REPORTED_GPS_POINT!");
            e.printStackTrace();
        }
    }

    public List<GPSData> findLastNonSafe(final String deviceId) {

        final Map<ConfigurationKey, Configuration> deviceConfig =
            Conf.getInstance().deviceValues.get(deviceId);
        final Configuration gpsAccuracyThreshold =
            deviceConfig.get(ConfigurationKey.DEVICE_MAP_REPORT_GPS_ACCURACY_THRESHOLD);

        try {
            return gpsDataDao.findLastNonSafe(deviceId, (long) gpsAccuracyThreshold.getIntValue());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }

    }

}
