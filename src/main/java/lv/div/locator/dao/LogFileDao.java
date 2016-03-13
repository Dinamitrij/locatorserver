package lv.div.locator.dao;

import lv.div.locator.model.LogFile;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class LogFileDao extends GenericDao {

    public List<LogFile> reloadLogFilesForSelectedDevice(final String deviceId) {
        final Query mainDataQuery = entityManager.createNamedQuery("LogFile.listByDevice");
        mainDataQuery.setParameter("deviceId", deviceId);
        final List<LogFile> logList = (List<LogFile>) mainDataQuery.getResultList();
        return logList;
    }

}
