package lv.div.locator.servlet;

import lv.div.locator.commons.conf.Const;
import lv.div.locator.dao.GPSDataDao;
import lv.div.locator.model.GPSData;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Statistics...
 */
@ManagedBean(name = "stat")
@RequestScoped
public class Statistics implements Serializable {

    private static final long serialVersionUID = 4035503804519764111L;
    /**
     * Max accuracy for GPS data
     */
    public static final int GPS_ACCURACY_THRESHOLD = 20;

    public static final int GPS_POINTS_COUNT_FOR_REPORT = 5;

    @EJB
    private GPSDataDao gpsDataDao;

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    private List<GPSData> points = new ArrayList<GPSData>();
    private GPSData currentPoint;
    private List<GPSData> lastLines = new ArrayList<GPSData>();

    @PostConstruct
    private void init() {
    }

    public void requestData(String deviceid, Integer limit, Integer all) {
        points = buildPoints(deviceid, limit, all);
    }

    public List<GPSData> buildPoints(String device, Integer limit, Integer all) {

        List<GPSData> dbLinesItems = gpsDataDao.listLastRecordsByDevice(device);

        if (null != dbLinesItems && dbLinesItems.size() > 0) {
            currentPoint = dbLinesItems.get(0);
            lastLines.clear();
            lastLines.addAll(dbLinesItems);
        } else {
            currentPoint = buildEmptyPoint();
        }
        if (null != device) {
            @SuppressWarnings("unchecked")
            List<GPSData> items = new ArrayList<GPSData>();
            final List resultList = gpsDataDao.listRawStatisticsDataArray(device, limit);

            for (Object line : resultList) { // We need this conversion here, due to NATIVE query:
                final Object[] singleLine = (Object[]) line;
                items.add(new GPSData((Integer) singleLine[0],
                                      (String) singleLine[7],
                                      (String) singleLine[8],
                                      (String) singleLine[10],
                                      (String) singleLine[12],
                                      (Long) singleLine[1],
                                      (String) singleLine[8],
                                      (Long) singleLine[2],
                                      (String) singleLine[3],
                                      (String) singleLine[4],
                                      (Timestamp) singleLine[6],
                                      (Long) singleLine[13],
                                      null
                ));
            }

            if (null == items || items.isEmpty()) {
                items.add(buildEmptyPoint());
            }

            return items;

        } else {
            return Collections.EMPTY_LIST;
        }

    }

    private GPSData buildEmptyPoint() {
        final GPSData emptyPoint = new GPSData();
        emptyPoint.setBattery(0L);
        emptyPoint.setSpeed(Const.ZERO_VALUE);
        emptyPoint.setAccuracy(0L);
        emptyPoint.setLatitude(Const.ZERO_VALUE);
        emptyPoint.setLongitude(Const.ZERO_VALUE);
        emptyPoint.setDeviceId(Const.ZERO_VALUE);
        emptyPoint.setId(0);
        emptyPoint.setInserted(new Timestamp(0));
        emptyPoint.setWifi("No data");
        emptyPoint.setSafeNetwork("default");
        return emptyPoint;
    }

    public String batteryColor() {

        final Long battery = currentPoint.getBattery();

        if (battery >= 75) {
            return "#008000";
        } else if (battery >= 50) {
            return "#68A921";
        } else if (battery >= 25) {
            return "#FF8000";
        } else {
            return "#FF0000";
        }
    }

    public String accuracyColor() {

        final GPSData gpsData = (GPSData) points.get(0);
        final Long accuracy = gpsData.getAccuracy();

        if (accuracy >= 75) {
            return "#FF0000";
        } else if (accuracy > GPS_ACCURACY_THRESHOLD) {
            return "#FF8000";
        } else {
            return "#008000";
        }

    }

    public String accPointColor(GPSData p) {
        if (p.getAccuracy() <= GPS_ACCURACY_THRESHOLD) {
            return "00CC00";
        } else {
            return "CCCCCC";
        }
    }

    public String ftime(GPSData p) {
        return simpleDateFormat.format(new Date(p.getInserted().getTime()));
    }

    public List<GPSData> getPoints() {
        return points;
    }

    public void setPoints(List<GPSData> points) {
        this.points = points;
    }

    public GPSData getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(GPSData currentPoint) {
        this.currentPoint = currentPoint;
    }

    public List<GPSData> getLastLines() {
        return lastLines;
    }

    public void setLastLines(List<GPSData> lastLines) {
        this.lastLines = lastLines;
    }
}