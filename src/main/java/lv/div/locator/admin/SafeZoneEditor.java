package lv.div.locator.admin;

import lv.div.locator.dao.ConfigurationDao;
import lv.div.locator.model.Configuration;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Edit safe zones for devices...
 */
@ManagedBean(name = "safe")
@ViewScoped
public class SafeZoneEditor implements Serializable {

    private static final long serialVersionUID = -4307171672613912978L;

    @EJB
    private ConfigurationDao configuration;

    private List<Configuration> devices = new ArrayList<Configuration>();
    private Configuration selectedDevice;
    private Configuration selectedSafeZone = new Configuration();
    private String selectedDeviceId;

    @PostConstruct
    private void init() {
        loadDevices();
    }

    public void loadDevices() {
        devices = configuration.loadDevices();
        setSelectedDevice(devices.get(0));
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

    public void buttonSaveAction(ActionEvent actionEvent) {
        final Configuration selectedSafeZone = getSelectedSafeZone();
        configuration.updateConfigurationRecord(selectedSafeZone);
    }

    public void buttonShowAction(ActionEvent actionEvent) {
        final Configuration configRow = configuration.getSafeWifiConfig(getSelectedDevice().getDeviceId());
        setSelectedSafeZone(configRow);
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

    public Configuration getSelectedSafeZone() {
        return selectedSafeZone;
    }

    public void setSelectedSafeZone(Configuration selectedSafeZone) {
        this.selectedSafeZone = selectedSafeZone;
    }
}