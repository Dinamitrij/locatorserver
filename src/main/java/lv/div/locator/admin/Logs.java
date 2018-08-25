package lv.div.locator.admin;

import lv.div.locator.commons.conf.Const;
import lv.div.locator.dao.BSSIDDao;
import lv.div.locator.dao.ConfigurationDao;
import lv.div.locator.dao.LogFileDao;
import lv.div.locator.model.BSSIDdata;
import lv.div.locator.model.BSSIDnetwork;
import lv.div.locator.model.Configuration;
import lv.div.locator.model.GPSData;
import lv.div.locator.model.LogFile;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Statistics...
 */
@ManagedBean(name = "logs")
@ViewScoped
public class Logs implements Serializable {

    private static final long serialVersionUID = -8027827268848674639L;

    @EJB
    ConfigurationDao configuration;

    @EJB
    BSSIDDao bssidDao;

    @EJB
    LogFileDao logFileDao;

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    private List<LogFile> logFiles = new ArrayList<LogFile>();
    private Map<String, List> logFilesCache = new HashMap<String, List>();
    private GPSData currentPoint;
    private List<BSSIDnetwork> bssids = new ArrayList<BSSIDnetwork>();
    private List<Configuration> devices = new ArrayList<Configuration>();
    private Configuration selectedDevice;
    private LogFile selectedLogFile;
    private String selectedDeviceId;

    private List<BSSIDnetwork> filteredBssids = new ArrayList<BSSIDnetwork>();
    private BSSIDnetwork selectedBssid;
    private String selectedBssidValue = StringUtils.EMPTY;
    private String mapCenter = "0,0";
    private MapModel circleModel;

    @PostConstruct
    private void init() {
        loadDevices();
    }

    public void handleChange(AjaxBehaviorEvent event) {
        UIComponent component = event.getComponent();
        String bssidValue = (String) ((HtmlSelectOneListbox) component).getSubmittedValue();
        final List<BSSIDdata> dataByBssid =
            bssidDao.getDataByBssidAndDevice(bssidValue, getSelectedDevice().getDeviceId());

        circleModel = new DefaultMapModel();

        final BSSIDdata bssiDdata = dataByBssid.get(0);
        mapCenter = String.valueOf(bssiDdata.getLatitude()) + "," + String.valueOf(bssiDdata.getLongitude());

        StringBuffer sb = new StringBuffer();
        for (BSSIDdata bssid : dataByBssid) {
            sb.append(bssid.getLatitude());
            sb.append(", ");
            sb.append(bssid.getLongitude());
            sb.append("\n");

            circleModel.addOverlay(
                new Marker(new LatLng(Double.valueOf(bssid.getLatitude()), Double.valueOf(bssid.getLongitude())),
                           bssid.getWifiname()));

        }

        selectedBssidValue = sb.toString();
    }

    public void reloadLogFilesForSelectedDevice() {
        if (logFilesCache.containsKey(getSelectedDevice().getDeviceId())) {
            setLogFiles(logFilesCache.get(getSelectedDevice().getDeviceId()));
        } else {
            final List<LogFile> logsList =
                logFileDao.reloadLogFilesForSelectedDevice(getSelectedDevice().getDeviceId());
            logFilesCache.put(getSelectedDevice().getDeviceId(), logsList);
            setLogFiles(logsList);
        }

    }

    public void loadDevices() {
        devices = configuration.loadDevices();
        setSelectedDevice(devices.get(0));
    }

    private GPSData buildEmptyPoint() {
        final GPSData emptyPoint = new GPSData();
        emptyPoint.setBattery(Const.ZERO_LONG_VALUE);
        emptyPoint.setSpeed(Const.ZERO_VALUE);
        emptyPoint.setAccuracy(Const.ZERO_LONG_VALUE);
        emptyPoint.setLatitude(Const.ZERO_VALUE);
        emptyPoint.setLongitude(Const.ZERO_VALUE);
        emptyPoint.setDeviceId(Const.ZERO_VALUE);
        emptyPoint.setId(0);
        emptyPoint.setInserted(new Timestamp(Const.ZERO_LONG_VALUE));
        emptyPoint.setWifi("No data");
        emptyPoint.setSafeNetwork("default");
        return emptyPoint;
    }

    public void deviceChange(AjaxBehaviorEvent event) {
        UIComponent component = event.getComponent();
        final String submittedDeviceId = (String) ((HtmlSelectOneListbox) component).getSubmittedValue();
        for (Configuration device : devices) {
            if (device.getDeviceId().equals(submittedDeviceId)) {
                setSelectedDevice(device);
                break;
            }

        }
    }

    public void onRowSelect(SelectEvent event) {
        final LogFile selectedLogFile = getSelectedLogFile();
    }

    public void buttonDownloadAction(ActionEvent actionEvent) {
        final LogFile selectedLogFile = getSelectedLogFile();

        InputStream is = new BufferedInputStream(new ByteArrayInputStream(selectedLogFile.getFiledata().getBytes()));
        Base64InputStream b64is = new Base64InputStream(is);

        FacesContext fc = FacesContext.getCurrentInstance();

        try {
            ExternalContext ec = fc.getExternalContext();

            ec.responseReset();
            ec.setResponseContentType("application/zip");
            //ec.setResponseContentLength(contentLength);
            ec.setResponseHeader("Content-Disposition",
                                 "attachment; filename=\"" + selectedLogFile.getFilename() + "\"");

            OutputStream output = ec.getResponseOutputStream();
            try {
                byte[] buff = new byte[2048];
                int count;
                while ((count = b64is.read(buff)) > 0) {
                    output.write(buff, 0, count);
                }
            } finally {
                b64is.close();
                output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            fc.responseComplete();
        }

    }

    public String ftime(GPSData p) {
        return simpleDateFormat.format(new Date(p.getInserted().getTime()));
    }

    public List<LogFile> getLogFiles() {
        reloadLogFilesForSelectedDevice();
        return logFiles;
    }

    public void setLogFiles(List<LogFile> logFiles) {
        this.logFiles = logFiles;
    }

    public GPSData getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(GPSData currentPoint) {
        this.currentPoint = currentPoint;
    }

    public List<BSSIDnetwork> getBssids() {
        return bssids;
    }

    public void setBssids(List<BSSIDnetwork> bssids) {
        this.bssids = bssids;
    }

    public BSSIDnetwork getSelectedBssid() {
        return selectedBssid;
    }

    public void setSelectedBssid(BSSIDnetwork selectedBssid) {
        this.selectedBssid = selectedBssid;
    }

    public String getSelectedBssidValue() {
        return selectedBssidValue;
    }

    public void setSelectedBssidValue(String selectedBssidValue) {
        this.selectedBssidValue = selectedBssidValue;
    }

    public MapModel getCircleModel() {
        return circleModel;
    }

    public String getMapCenter() {
        return mapCenter;
    }

    public void setMapCenter(String mapCenter) {
        this.mapCenter = mapCenter;
    }

    public List<BSSIDnetwork> getFilteredBssids() {
        return filteredBssids;
    }

    public void setFilteredBssids(List<BSSIDnetwork> filteredBssids) {
        this.filteredBssids = filteredBssids;
    }

    public List<Configuration> getDevices() {
        return devices;
    }

    public void setDevices(List<Configuration> devices) {
        this.devices = devices;
    }

    public Configuration getSelectedDevice() {
        return selectedDevice;
    }

    public void setSelectedDevice(Configuration selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public LogFile getSelectedLogFile() {
        return selectedLogFile;
    }

    public void setSelectedLogFile(LogFile selectedLogFile) {
        this.selectedLogFile = selectedLogFile;
    }

    public String getSelectedDeviceId() {
        return selectedDeviceId;
    }

    public void setSelectedDeviceId(String selectedDeviceId) {
        this.selectedDeviceId = selectedDeviceId;
    }
}