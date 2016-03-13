package lv.div.locator.model;

public class BSSIDnetwork {

    private String name;
    private String bssid;

    public BSSIDnetwork(String name, String bssid) {
        this.name = name;
        this.bssid = bssid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }
}
