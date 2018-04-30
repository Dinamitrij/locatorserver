package lv.div.locator.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Main GPS data table
 */
@Entity
@Table(name = "bssiddata")
public class BSSIDdata {

    public static final int LAT_LON_DATA_LEN = 16;

    @Id
    @SequenceGenerator(name = "bssiddata_id_seq", sequenceName = "bssiddata_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bssiddata_id_seq")
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "deviceid", length = 36)
    private String deviceId;

    @Column(name = "devicename", length = 32)
    private String deviceName;

    @Column(name = "latitude", length = LAT_LON_DATA_LEN)
    private String latitude;

    @Column(name = "longitude", length = LAT_LON_DATA_LEN)
    private String longitude;

    @Column(name = "accuracy")
    private Long accuracy;

    @Column(name = "wifiname", length = 50)
    private String wifiname;

    @Column(name = "bssid", length = 50)
    private String bssid;

    @Column(name = "inserted")
    private Timestamp inserted = new Timestamp((new Date()).getTime());

    @Column(name = "tag")
    private Integer tag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Long getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Long accuracy) {
        this.accuracy = accuracy;
    }

    public String getWifiname() {
        return wifiname;
    }

    public void setWifiname(String wifiname) {
        this.wifiname = wifiname;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public Timestamp getInserted() {
        return inserted;
    }

    public void setInserted(Timestamp inserted) {
        this.inserted = inserted;
    }

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BSSIDdata bssiDdata = (BSSIDdata) o;

        if (accuracy != null ? !accuracy.equals(bssiDdata.accuracy) : bssiDdata.accuracy != null) {
            return false;
        }
        if (bssid != null ? !bssid.equals(bssiDdata.bssid) : bssiDdata.bssid != null) {
            return false;
        }
        if (deviceId != null ? !deviceId.equals(bssiDdata.deviceId) : bssiDdata.deviceId != null) {
            return false;
        }
        if (deviceName != null ? !deviceName.equals(bssiDdata.deviceName) : bssiDdata.deviceName != null) {
            return false;
        }
        if (id != null ? !id.equals(bssiDdata.id) : bssiDdata.id != null) {
            return false;
        }
        if (inserted != null ? !inserted.equals(bssiDdata.inserted) : bssiDdata.inserted != null) {
            return false;
        }
        if (latitude != null ? !latitude.equals(bssiDdata.latitude) : bssiDdata.latitude != null) {
            return false;
        }
        if (longitude != null ? !longitude.equals(bssiDdata.longitude) : bssiDdata.longitude != null) {
            return false;
        }
        if (tag != null ? !tag.equals(bssiDdata.tag) : bssiDdata.tag != null) {
            return false;
        }
        if (wifiname != null ? !wifiname.equals(bssiDdata.wifiname) : bssiDdata.wifiname != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (accuracy != null ? accuracy.hashCode() : 0);
        result = 31 * result + (wifiname != null ? wifiname.hashCode() : 0);
        result = 31 * result + (bssid != null ? bssid.hashCode() : 0);
        result = 31 * result + (inserted != null ? inserted.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
