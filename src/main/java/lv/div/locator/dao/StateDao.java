package lv.div.locator.dao;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.conf.Conf;
import lv.div.locator.model.State;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.Date;

@Stateless
public class StateDao extends GenericDao {

    public void cleanupAllStates() {
        final Query cleanupQ = entityManager.createNamedQuery("State.truncate");
        cleanupQ.executeUpdate();
    }


    public void registerLatestSignalFromDevice(String deviceId) {
        // Cleanup old IN_SAFE_ZONE state, if any:
        deleteByDeviceAndKey(deviceId, ConfigurationKey.DEVICE_SIGNAL);
        State safeZoneState = new State();
        safeZoneState.setDeviceId(deviceId);

        final String deviceName =
            Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.DEVICE_ALIAS).getValue().toUpperCase();

        safeZoneState.setDeviceName(deviceName);
        safeZoneState.setKey(ConfigurationKey.DEVICE_SIGNAL);
        save(safeZoneState);
    }


    public State findLastReportedByDevice(String deviceId) {
        return findByDeviceAndKey(deviceId, ConfigurationKey.LAST_REPORTED_GPS_POINT);
    }

    /**
     * Finds particular device' state.
     * <p>
     * It's veeeery strange, why I can't use simply query:
     * entityManager.createQuery("SELECT t FROM State t WHERE t.deviceId = :deviceId AND t.key = :key");
     * but...
     *
     * @param deviceId
     * @param configurationKey
     *
     * @return
     */
    public State findByDeviceAndKey(String deviceId, ConfigurationKey configurationKey) {
        try {

            final Query findQ = entityManager.createNamedQuery("State.findByDeviceAndKey");
            findQ.setParameter("deviceId", deviceId);
            findQ.setParameter("ckey", configurationKey);
            return (State) findQ.getSingleResult();

        } catch (Exception e) {
//            log.warning("No data for deviceId = " + deviceId + ", configurationKey = " + configurationKey);
            return null;
        }
    }

    public void deleteOldMlsReportedState(String deviceId) {
        final State state = findByDeviceAndKey(deviceId, ConfigurationKey.MLS_REPORTED);
        if (null != state) {
            delete(state);
            log.info("Old MLS_REPORTED state deleted. deviceId = " + deviceId);
        } else {
            log.warning("Cannot delete (not found) old MLS_REPORTED state. deviceId = " + deviceId);
        }
    }

    public void saveNewMlsReportedState(String deviceId) {
        final Date now = new Date();
        State mlsReportedState = new State();
        mlsReportedState.setKey(ConfigurationKey.MLS_REPORTED);
        mlsReportedState.setDeviceId(deviceId);
        mlsReportedState.setDateValue(new Timestamp(now.getTime()));
        save(mlsReportedState);
        log.info("New MLS_REPORTED state saved. deviceId = " + deviceId);

    }

    public void deleteByDeviceAndKey(String deviceId, ConfigurationKey configurationKey) {
        try {
            final Query cleanupQuery = entityManager.createNamedQuery("State.deleteByDeviceAndKey");
            cleanupQuery.setParameter("deviceId", deviceId);
            cleanupQuery.setParameter("ckey", configurationKey);
            cleanupQuery.executeUpdate();
            getEntityManager().flush();
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
