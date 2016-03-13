package lv.div.locator.healthcheck;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.conf.Conf;
import lv.div.locator.model.Configuration;
import lv.div.locator.model.GPSData;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import javax.ejb.Stateless;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map;

@Stateless
public class AlertSender {

    public static final String ENTERED_ZONE_TEXT_PREFIX = "Entered ";
    public static final String LEFT_ZONE_TEXT_PREFIX = "Left ";

    public void sendAlert(String urlMask, final String text) {
        try {
            String finalUrl = String.format(urlMask, URLEncoder.encode(text, "UTF-8"));

            HttpClient c = new DefaultHttpClient();
            final HttpGet httpGet = new HttpGet(finalUrl);

            HttpResponse r = c.execute(httpGet);

            BufferedReader rd = new BufferedReader(new InputStreamReader(r.getEntity().getContent()));
            String line;

            while ((line = rd.readLine()) != null) {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendWifiInfo(String deviceId, GPSData gpsData) {
        try {

            // Reporting the same to Telegram for faster reports (without web page reload):
            final Map<ConfigurationKey, Configuration> globals = Conf.getInstance().globals;
            StringBuffer sb = buildMessageBufferWithDeviceAlias(deviceId);

            sb.append(": \uD83D\uDD0B ");
            sb.append(gpsData.getBattery());

            final Long accValue = gpsData.getAccelerometer();
            if (accValue == 0) {
                sb.append(",  \uD83D\uDCA4 "); // Sleep sign
            } else if (accValue > 200) {
                sb.append(", \uD83C\uDFC3 "); // Running man
                sb.append(accValue);
            } else if (accValue > 100) {
                sb.append(", \uD83D\uDEB6 "); // Walking man
                sb.append(accValue);
            } else {
                sb.append(", \uD83D\uDC4B "); // Shaking hand sign
                sb.append(accValue);
            }

            if (!StringUtils.isBlank(gpsData.getSafeNetwork())) {
                sb.append(", \uD83D\uDCCD");
                sb.append(gpsData.getSafeNetwork());
            }
            sb.append(", *Wifi*=");

            String truncatedWifiText = StringUtils.substring(gpsData.getWifi(), 0, 100);
            truncatedWifiText = StringUtils.replace(truncatedWifiText, "* ", "# ");
            sb.append(StringUtils.replace(truncatedWifiText, "_", "--")); // Removing MarkDown's "_" special symbol

            sendAlert(globals.get(ConfigurationKey.ADMIN_ALERT_ADDRESS).getValue(), sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendZoneAlert(String deviceId, String message) {
        try {
            final Map<ConfigurationKey, Configuration> globals = Conf.getInstance().globals;
            StringBuffer sb = buildMessageBufferWithDeviceAlias(deviceId);

            if (message.indexOf(ENTERED_ZONE_TEXT_PREFIX) > -1) {
//                sb.append(" \uD83C\uDFC1 "); // Kletch flag sign
                sb.append(" \uD83C\uDFE0 "); // Domik
            } else {
                sb.append(" \uD83D\uDEA7 "); // Yellow construction zone
            }

            sb.append(message);

            sendAlert(globals.get(ConfigurationKey.ADMIN_ALERT_ADDRESS).getValue(), sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendLowMLSAccuracyAlert(String deviceId, String message) {
        try {
            final Map<ConfigurationKey, Configuration> globals = Conf.getInstance().globals;

            StringBuffer sb = buildMessageBufferWithDeviceAlias(deviceId);
            sb.append(" \uD83C\uDFAF "); // Mishen - target

            sb.append(message);

            sendAlert(globals.get(ConfigurationKey.ADMIN_ALERT_ADDRESS).getValue(), sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringBuffer buildMessageBufferWithDeviceAlias(String deviceId) {

        final String alias =
            Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.ADMIN_ALERT_ADDRESS.DEVICE_ALIAS)
                .getValue();

        StringBuffer sb = new StringBuffer();
        sb.append("_");
        sb.append(alias);
        sb.append("_:");
        return sb;
    }

}
