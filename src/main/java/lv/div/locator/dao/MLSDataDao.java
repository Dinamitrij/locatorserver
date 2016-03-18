package lv.div.locator.dao;

import lv.div.locator.model.MLSData;
import javax.ejb.Stateless;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Stateless
public class MLSDataDao extends GenericDao {

    public List<MLSData> listTwoLastMLSPoints(String deviceId) {
        try {
            final Query twoLastQuery = entityManager.createNamedQuery("MLSData.listLastMLSPointsByDeviceId");
            twoLastQuery.setParameter("deviceId", deviceId);
            twoLastQuery.setMaxResults(2);

            return (List<MLSData>) twoLastQuery.getResultList();

        } catch (Exception e) {
            log.warning("Not found for deviceId = " + deviceId);
            return Collections.EMPTY_LIST;
        }
    }

    public void cleanupAllData() {
        final Query cleanupQ = entityManager.createNamedQuery("MLSData.truncate");
        cleanupQ.executeUpdate();
    }

}
