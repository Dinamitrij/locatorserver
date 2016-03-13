package lv.div.locator.dao;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.model.State;
import javax.ejb.Stateless;
import javax.persistence.Query;

@Stateless
public class StateDao extends GenericDao {

    public void cleanupAllStates() {
        final Query cleanupQ = entityManager.createNamedQuery("State.truncate");
        cleanupQ.executeUpdate();
    }

    public State findLastReportedByDevice(String deviceId) {
        return findByDeviceAndKey(deviceId, ConfigurationKey.LAST_REPORTED_GPS_POINT);
    }

    public State findByDeviceAndKey(String deviceId, ConfigurationKey configurationKey) {
        try {
            final Query cleanupQuery = entityManager.createNamedQuery("State.findByDeviceAndKey");
            cleanupQuery.setParameter("deviceId", deviceId);
            cleanupQuery.setParameter("ckey", configurationKey);
            return (State) cleanupQuery.getSingleResult();
        } catch (Exception e) {
            log.warning("Not found LAST_REPORTED_GPS_POINT for " + deviceId);
            return null;
        }
    }

    public void deleteByDeviceAndKey(String deviceId, ConfigurationKey configurationKey) {
        try {
            final Query cleanupQuery = entityManager.createNamedQuery("State.deleteByDeviceAndKey");
            cleanupQuery.setParameter("deviceId", deviceId);
            cleanupQuery.setParameter("ckey", configurationKey);
            cleanupQuery.executeUpdate();
        } catch (Exception e) {
            log.severe("Cannot delete state for " + deviceId);
        }
    }

    public void cleanupLastReportedGPSPoint(String deviceId) {
        final Query cleanupQuery = entityManager.createNamedQuery("State.cleanupLastReportedGPSPoint");
        cleanupQuery.setParameter(1, deviceId);
        cleanupQuery.executeUpdate();
    }

}
