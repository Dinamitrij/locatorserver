package lv.div.locator.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Devices' log files
 */
@Entity
@Table(name = "logfile")
public class LogFile {

    @Id
    @SequenceGenerator(name = "logfile_id_seq", sequenceName = "logfile_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logfile_id_seq")
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "deviceid", length = 36)
    private String deviceId;

    @Column(name = "devicename", length = 32)
    private String deviceName;

    @Column(name = "filename", length = 32)
    private String filename;

    @Lob
    @Column(name = "filedata")
    private String filedata;

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiledata() {
        return filedata;
    }

    public void setFiledata(String filedata) {
        this.filedata = filedata;
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

        LogFile logFile = (LogFile) o;

        if (deviceId != null ? !deviceId.equals(logFile.deviceId) : logFile.deviceId != null) {
            return false;
        }
        if (deviceName != null ? !deviceName.equals(logFile.deviceName) : logFile.deviceName != null) {
            return false;
        }
        if (filedata != null ? !filedata.equals(logFile.filedata) : logFile.filedata != null) {
            return false;
        }
        if (filename != null ? !filename.equals(logFile.filename) : logFile.filename != null) {
            return false;
        }
        if (id != null ? !id.equals(logFile.id) : logFile.id != null) {
            return false;
        }
        if (inserted != null ? !inserted.equals(logFile.inserted) : logFile.inserted != null) {
            return false;
        }
        if (tag != null ? !tag.equals(logFile.tag) : logFile.tag != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        result = 31 * result + (filedata != null ? filedata.hashCode() : 0);
        result = 31 * result + (inserted != null ? inserted.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}
