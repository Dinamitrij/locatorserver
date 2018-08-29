package lv.div.locator.dao;

import lv.div.locator.model.BSSIDdata;
import javax.ejb.Stateless;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class BSSIDDao extends GenericDao {

//    public List<BSSIDdata> listNetworks(final String deviceId) {
//        final Query mainDataQuery = entityManager.createNamedQuery("BSSIDdata.getNetworks");
//        mainDataQuery.setParameter(1, deviceId);
//        List<BSSIDdata> networks = (List<BSSIDdata>) mainDataQuery.getResultList();
//        return networks;
//    }

    public List listNetworks(final String deviceId) {
        final Query mainDataQuery = entityManager.createNamedQuery("BSSIDdata.getNetworks");
        mainDataQuery.setParameter(1, deviceId);
        return mainDataQuery.getResultList();
    }

    public List<BSSIDdata> getDataByBssidAndDevice(String bssid, final String selectedDeviceId) {
        final Query mainDataQuery = entityManager.createNamedQuery("BSSIDdata.getDataByBssidAndDevice");
        mainDataQuery.setParameter("bssid", bssid);
        mainDataQuery.setParameter("deviceId", selectedDeviceId);

        return (List<BSSIDdata>) mainDataQuery.getResultList();
    }

}
