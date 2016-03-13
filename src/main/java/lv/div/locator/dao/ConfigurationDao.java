package lv.div.locator.dao;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.model.Configuration;
import javax.ejb.Stateless;
import javax.faces.event.ActionEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class ConfigurationDao extends GenericDao{

    public List<Configuration> loadDevices() {
        final Query mainDataQuery = entityManager.createNamedQuery("Configuration.listDevices");
        mainDataQuery.setParameter("ckey", ConfigurationKey.DEVICE_ALIAS);
        List<Configuration> devices = (List<Configuration>) mainDataQuery.getResultList();
        return devices;
    }

    public void updateConfigurationRecord(Configuration dbRecord) {
        update(dbRecord);
    }

    public List<Configuration> listConfigByDeviceId(String deviceId) {
        final Query mainDataQuery = entityManager.createNamedQuery("Configuration.listConfigByDeviceId");
        mainDataQuery.setParameter("deviceId", deviceId);
        List<Configuration> devices = (List<Configuration>) mainDataQuery.getResultList();
        return devices;
    }

    public Configuration getSafeWifiConfig(final String deviceId) {
        final Query query = entityManager.createNamedQuery("Configuration.getSafeWifiConfig");
        query.setParameter("ckey", ConfigurationKey.SAFE_ZONE_WIFI);
        query.setParameter("deviceId", deviceId);
        final Configuration configRow = (Configuration) query.getSingleResult();
        return configRow;
    }





}
