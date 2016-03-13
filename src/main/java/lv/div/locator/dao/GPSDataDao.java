package lv.div.locator.dao;

import lv.div.locator.commons.conf.Const;
import lv.div.locator.model.GPSData;
import lv.div.locator.servlet.Statistics;
import org.apache.commons.lang3.StringUtils;
import javax.ejb.Stateless;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class GPSDataDao extends GenericDao {

    public List<GPSData> findLastNonSafe(String deviceId, Integer accuracyValue) {

        final Query lastNonSafeQuery = entityManager.createNamedQuery("GPSData.findLastNonSafe");
        lastNonSafeQuery.setParameter(Const.DEVICE_ID_PARAMETER_NAME, deviceId);
        lastNonSafeQuery.setParameter("safenetwork", StringUtils.EMPTY);
        lastNonSafeQuery.setParameter("latfilter", Const.ZERO_COORDINATE);  // not 0.0
        lastNonSafeQuery.setParameter("accuracy", accuracyValue);  // accurate
        lastNonSafeQuery.setMaxResults(1); // Only 1 last point needed

        return (List<GPSData>) lastNonSafeQuery.getResultList();

    }

    public List<GPSData> findLastNonSafeAfterReported(String deviceId, Integer accuracyValue,
                                                      Integer lastIdToFindAfter) {

        final Query findLastNonSafeAfterReported =
            entityManager.createNamedQuery("GPSData.findLastNonSafeAfterReported");

        findLastNonSafeAfterReported.setParameter(Const.DEVICE_ID_PARAMETER_NAME, deviceId);
        findLastNonSafeAfterReported.setParameter("safenetwork", StringUtils.EMPTY);
        findLastNonSafeAfterReported.setParameter("latfilter", Const.ZERO_COORDINATE);  // not 0.0
        findLastNonSafeAfterReported.setParameter("accuracy", accuracyValue);  // accurate
        findLastNonSafeAfterReported.setParameter("lastId", lastIdToFindAfter);
        findLastNonSafeAfterReported.setMaxResults(Statistics.GPS_POINTS_COUNT_FOR_REPORT);
        return (List<GPSData>) findLastNonSafeAfterReported.getResultList();

    }

    public List<GPSData> listLastRecordsByDevice(String deviceId) {
        final Query lastByDevice = entityManager.createNamedQuery("GPSData.listLastRecordsByDevice");
        lastByDevice.setParameter(Const.DEVICE_ID_PARAMETER_NAME, deviceId);
        lastByDevice.setMaxResults(20); // 20 last results hardcoded
        return (List<GPSData>) lastByDevice.getResultList();
    }

    public List listRawStatisticsDataArray(String deviceId, int limitRecords) {
        final Query query = entityManager.createNamedQuery("GPSData.listStatistics");
        query.setParameter(1, deviceId);
        query.setParameter(2, limitRecords);
        final List rawDataResultList = query.getResultList();
        return rawDataResultList;

    }







}
