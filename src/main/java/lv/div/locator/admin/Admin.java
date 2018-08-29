package lv.div.locator.admin;

import lv.div.locator.commons.conf.Const;
import lv.div.locator.dao.BSSIDDao;
import lv.div.locator.dao.ConfigurationDao;
import lv.div.locator.model.BSSIDdata;
import lv.div.locator.model.BSSIDnetwork;
import lv.div.locator.model.Configuration;
import lv.div.locator.model.GPSData;
import lv.div.locator.utils.GeoUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.Circle;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Statistics...
 */
@ManagedBean(name = "adm")
@ViewScoped
public class Admin implements Serializable {

    private static final long serialVersionUID = 4035503804519664111L;

    @EJB
    private ConfigurationDao configuration;

    @EJB
    private BSSIDDao bssidDao;

    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    private List<GPSData> points = new ArrayList<GPSData>();
    private GPSData currentPoint;
    private List<BSSIDnetwork> bssids = new ArrayList<BSSIDnetwork>();
    private List<Configuration> devices = new ArrayList<Configuration>();
    private Configuration selectedDevice;
    private String selectedDeviceId;

    private List<BSSIDnetwork> filteredBssids = new ArrayList<BSSIDnetwork>();
    private BSSIDnetwork selectedBssid;
    private String selectedBssidValue = StringUtils.EMPTY;
    private String mapCenter = "0,0";
    private MapModel circleModel;

    @PostConstruct
    private void init() {
        loadDevices();
        buildData();
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

    public void buildData() {
        final List<BSSIDdata> networks = bssidDao.listNetworks(getSelectedDevice().getDeviceId());
        Iterator it = networks.iterator();
        bssids.clear();
        while (it.hasNext()) {
            Object[] result = (Object[]) it.next(); // Iterating through array object
            bssids.add(new BSSIDnetwork((String) result[0], (String) result[1]));
        }

    }

//    public void buildData() {
//        final List<BSSIDdata> networks = bssidDao.listNetworks(getSelectedDevice().getDeviceId());
//        Iterator it = networks.iterator();
//        bssids.clear();
//        while (it.hasNext()) {
//            Object[] result = (Object[]) it.next(); // Iterating through array object
//            bssids.add(new BSSIDnetwork((String) result[0], (String) result[1]));
//        }
//        int a = 1 + 1;
//    }
//
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

    public void deviceChange(ValueChangeEvent event) {
        final String submittedDeviceId = (String) event.getNewValue();
        for (Configuration device : devices) {
            if (device.getDeviceId().equals(submittedDeviceId)) {
                setSelectedDevice(device);
                break;
            }

        }
        buildData();
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
        buildData();
    }

    public void onRowSelect(SelectEvent event) {
        final BSSIDnetwork selectedNetwork = getSelectedBssid();
        final List<BSSIDdata> dataByBssid = bssidDao.getDataByBssidAndDevice(selectedNetwork.getBssid(),
                                                                             getSelectedDevice().getDeviceId());

        final BSSIDdata bssiDdata1 = dataByBssid.get(0);
        mapCenter = String.valueOf(bssiDdata1.getLatitude()) + "," + String.valueOf(bssiDdata1.getLongitude());

        circleModel = new DefaultMapModel();

        for (BSSIDdata bssiDdata : dataByBssid) {
            circleModel.addOverlay(new Marker(
                new LatLng(Double.valueOf(bssiDdata.getLatitude()), Double.valueOf(bssiDdata.getLongitude())),
                bssiDdata.getWifiname()));
        }

        SortedMap<Float, List> distances = new TreeMap<Float, List>();

        Set<BSSIDdata> outerThead = new HashSet<BSSIDdata>();
        for (BSSIDdata outerBssiDdata : dataByBssid) {

            for (BSSIDdata bssiDdata : dataByBssid) {

                if (!bssiDdata.equals(outerBssiDdata) && !outerThead.contains(bssiDdata)) {
                    final float dist = GeoUtils.distBetween(Double.valueOf(outerBssiDdata.getLatitude()),
                                                            Double.valueOf(outerBssiDdata.getLongitude()),
                                                            Double.valueOf(bssiDdata.getLatitude()),
                                                            Double.valueOf(bssiDdata.getLongitude())
                    );

                    List points = new ArrayList();
                    points.add(outerBssiDdata);
                    points.add(bssiDdata);
                    distances.put(dist, points);
                }

            }
            outerThead.add(outerBssiDdata);

        }

        double circleRadius = 50;
        if (distances.size() > 0) {
            final Float aFloat = distances.lastKey();
            circleRadius = aFloat.doubleValue() / 2;
        }

        double lat = 0;
        double lng = 0;

        for (BSSIDdata bssiDdata : dataByBssid) {
            lat = lat + Double.valueOf(bssiDdata.getLatitude());
            lng = lng + Double.valueOf(bssiDdata.getLongitude());
        }

        lat = lat / dataByBssid.size();
        lng = lng / dataByBssid.size();

        //Shared coordinates
        LatLng coord1 = new LatLng(lat, lng);

        //Circle
        Circle circle1 = new Circle(coord1, circleRadius);
        circle1.setStrokeColor("#0080C0");
        circle1.setFillColor("#0080C0");
        circle1.setFillOpacity(0.2);

        circleModel.addOverlay(circle1);

        int a = 1 + 2;

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

    public void onCircleSelect(OverlaySelectEvent event) {
        FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Circle Selected!!!", null));
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

    public String getSelectedDeviceId() {
        return selectedDeviceId;
    }

    public void setSelectedDeviceId(String selectedDeviceId) {
        this.selectedDeviceId = selectedDeviceId;
    }
}