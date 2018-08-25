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
@Table(name = "gpsdata")
public class GPSData {

    public static final int DB_MAX_STRING_LEN = 255;

    public static final int LAT_LON_DATA_LEN = 16;

    @Id
    @SequenceGenerator(name = "gpsdata_id_seq", sequenceName = "gpsdata_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gpsdata_id_seq")
    @Column(name = "id", updatable = false)
    private Integer id;

    @Column(name = "latitude", length = LAT_LON_DATA_LEN)
    private String latitude;

    @Column(name = "longitude", length = LAT_LON_DATA_LEN)
    private String longitude;

    @Column(name = "speed", length = LAT_LON_DATA_LEN)
    private String speed;

    @Column(name = "wifi", length = DB_MAX_STRING_LEN)
    private String wifi;

    @Column(name = "accuracy")
    private Long accuracy;

    @Column(name = "accelerometer")
    private Long accelerometer;

    @Column(name = "safenetwork", length = 32)
    private String safeNetwork;

    @Column(name = "battery")
    private Long battery;

    @Column(name = "deviceid", length = 36)
    private String deviceId;

    @Column(name = "devicename", length = 32)
    private String deviceName;

    @Column(name = "devicetime")
    private Timestamp deviceTime;

    @Column(name = "inserted")
    private Timestamp inserted = new Timestamp((new Date()).getTime());

    @Column(name = "tag")
    private Integer tag;

    public GPSData() {
    }

    public GPSData(Integer id, String latitude, String longitude, String speed, String wifi, Long accuracy,
                   String safeNetwork, Long battery, String deviceId, String deviceName, Timestamp inserted,
                   Long accelerometer, Integer tag) {

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.wifi = wifi;
        this.accuracy = accuracy;
        this.safeNetwork = safeNetwork;
        this.battery = battery;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.inserted = inserted;
        this.accelerometer = accelerometer;
        this.tag = tag;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getBattery() {
        return battery;
    }

    public void setBattery(Long battery) {
        this.battery = battery;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
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

    public String getWifi() {
        return wifi;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }

    public Long getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Long accuracy) {
        this.accuracy = accuracy;
    }

    public Long getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(Long accelerometer) {
        this.accelerometer = accelerometer;
    }

    public String getSafeNetwork() {
        return safeNetwork;
    }

    public void setSafeNetwork(String safeNetwork) {
        this.safeNetwork = safeNetwork;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Timestamp getInserted() {
        return inserted;
    }

    public void setInserted(Timestamp inserted) {
        this.inserted = inserted;
    }

    public Timestamp getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(Timestamp devicetime) {
        this.deviceTime = devicetime;
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

        GPSData gpsData = (GPSData) o;

        if (accelerometer != null ? !accelerometer.equals(gpsData.accelerometer) : gpsData.accelerometer != null) {
            return false;
        }
        if (accuracy != null ? !accuracy.equals(gpsData.accuracy) : gpsData.accuracy != null) {
            return false;
        }
        if (battery != null ? !battery.equals(gpsData.battery) : gpsData.battery != null) {
            return false;
        }
        if (deviceId != null ? !deviceId.equals(gpsData.deviceId) : gpsData.deviceId != null) {
            return false;
        }
        if (deviceName != null ? !deviceName.equals(gpsData.deviceName) : gpsData.deviceName != null) {
            return false;
        }
        if (deviceTime != null ? !deviceTime.equals(gpsData.deviceTime) : gpsData.deviceTime != null) {
            return false;
        }
        if (id != null ? !id.equals(gpsData.id) : gpsData.id != null) {
            return false;
        }
        if (inserted != null ? !inserted.equals(gpsData.inserted) : gpsData.inserted != null) {
            return false;
        }
        if (latitude != null ? !latitude.equals(gpsData.latitude) : gpsData.latitude != null) {
            return false;
        }
        if (longitude != null ? !longitude.equals(gpsData.longitude) : gpsData.longitude != null) {
            return false;
        }
        if (safeNetwork != null ? !safeNetwork.equals(gpsData.safeNetwork) : gpsData.safeNetwork != null) {
            return false;
        }
        if (speed != null ? !speed.equals(gpsData.speed) : gpsData.speed != null) {
            return false;
        }
        if (tag != null ? !tag.equals(gpsData.tag) : gpsData.tag != null) {
            return false;
        }
        if (wifi != null ? !wifi.equals(gpsData.wifi) : gpsData.wifi != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (speed != null ? speed.hashCode() : 0);
        result = 31 * result + (wifi != null ? wifi.hashCode() : 0);
        result = 31 * result + (accuracy != null ? accuracy.hashCode() : 0);
        result = 31 * result + (accelerometer != null ? accelerometer.hashCode() : 0);
        result = 31 * result + (safeNetwork != null ? safeNetwork.hashCode() : 0);
        result = 31 * result + (battery != null ? battery.hashCode() : 0);
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (deviceTime != null ? deviceTime.hashCode() : 0);
        result = 31 * result + (inserted != null ? inserted.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
