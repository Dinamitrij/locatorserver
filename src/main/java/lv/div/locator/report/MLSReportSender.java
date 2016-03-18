package lv.div.locator.report;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.conf.Conf;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.conf.GoogleMapReporter;
import lv.div.locator.dao.StateDao;
import lv.div.locator.model.State;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Sending reports for MLS location points
 */
@Stateless
public class MLSReportSender {

    @EJB
    private StateDao stateDao;

    @EJB
    private GoogleMapReporter mapReporter;

    @EJB
    private ConfigurationManager configurationManager;

    @Inject
    private Logger log;

    public void sendMLSReportIfNeeded() {
        try {

            configurationManager.fetchGlobalConfiguration();

            final DateTime now = DateTime.now();

            final Integer mlsReportSafeZoneEachSec =
                Conf.getInstance().globals.get(ConfigurationKey.MLS_REPORT_SAFE_ZONE_EACH_SEC).getIntValue();

            final Integer mlsReportOutOfSafeZoneEachSec =
                Conf.getInstance().globals.get(ConfigurationKey.MLS_REPORT_OUT_OF_SAFE_ZONE_EACH_SEC).getIntValue();

            final List<State> allStates = stateDao.findAll(State.class);
            if (allStates.isEmpty()) {
                log.warning("States are empty for all devices! Skip MLS reporting");
                return;
            }


            final Map<String, List<State>> allStatesById =
                allStates.stream().collect(Collectors.groupingBy(a -> a.getDeviceId()));

            for (String deviceId : allStatesById.keySet()) {
                // Work with downloaded states on the App side. Reduce database calls:

                final List<State> states = allStatesById.get(deviceId);
//                for (State state : states) {
//                    log.info("State = " + state.toString());
//                }

//                log.info("Working with deviceId = " + deviceId + ", total devices count = " + allStatesById.keySet().size());

//                final State mlsReportedPoint = stateDao.findByDeviceAndKey(deviceId, ConfigurationKey.MLS_REPORTED);

                final State mlsReportedPoint =
                    allStatesById.get(deviceId).stream().filter(a -> ConfigurationKey.MLS_REPORTED.equals(a.getKey()))
                        .findFirst().orElse(null);

                if (null == mlsReportedPoint) { // MLS not reported yet. Send anyway
                    log.info("MLS: Reporting MLS location (initial) for " + deviceId);
                    mapReporter.sendMLSReport(deviceId);
                    stateDao.deleteOldMlsReportedState(deviceId);
                    stateDao.saveNewMlsReportedState(deviceId);
                } else {

                    log.info("MLS: Trying to report MLS location for " + deviceId);

                    final Timestamp mlsWasReportedAtTS = mlsReportedPoint.getDateValue();
                    final DateTime mlsWasReportedAt = new DateTime(mlsWasReportedAtTS.getTime());
//                    log.info("mlsWasReportedAt = " + mlsWasReportedAt);

                    final Seconds seconds = Seconds.secondsBetween(now, mlsWasReportedAt);
                    final int secondsFromLastReport = Math.abs(seconds.getSeconds());

                    // IN_SAFE_ZONE & OUT_OF_SAFE_ZONE flags are constantly updated, when APP is online.
                    // Long time after these flags update, means APP if switched off, so -
                    // not needed to report MLS.
                    final State deviceInSafeZone =
                        allStatesById.get(deviceId).stream().filter(a -> ConfigurationKey.IN_SAFE_ZONE.equals(a.getKey()))
                            .findFirst().orElse(null);
                    final State deviceOutOfSafeZone =
                        allStatesById.get(deviceId).stream().filter(a -> ConfigurationKey.OUT_OF_SAFE_ZONE.equals(a.getKey()))
                            .findFirst().orElse(null);

                    int secondsFromLastSignalReceived = 0;
                    if (null != deviceInSafeZone) {
                        log.info("MLS: Device in SafeZone " + deviceId);
                        final DateTime lastSignalWhenInSafeZone = new DateTime(deviceInSafeZone.getDateValue());
                        final Seconds s = Seconds.secondsBetween(now, lastSignalWhenInSafeZone);
                        secondsFromLastSignalReceived = Math.abs(s.getSeconds());
                    } else if (null != deviceOutOfSafeZone) {
                        log.info("MLS: Device NOT in SafeZone " + deviceId);
                        final DateTime lastSignalWhenOutOfSafeZone = new DateTime(deviceOutOfSafeZone.getDateValue());
                        final Seconds s = Seconds.secondsBetween(now, lastSignalWhenOutOfSafeZone);
                        secondsFromLastSignalReceived = Math.abs(s.getSeconds());
                    }

                    if (null != deviceInSafeZone) { // if device is in SAFE zone...

//                        log.info("Device in SAFE ZONE is found! for " + mlsReportedPoint.getDeviceName());
                        log.info("secondsFromLastReport = " + secondsFromLastReport);
                        log.info("mlsReportSafeZoneEachSec = " + mlsReportSafeZoneEachSec);
                        log.info("secondsFromLastSignalReceived = " + secondsFromLastSignalReceived);

                        // Do not report OLD states (mlsReportSafeZoneEachSec*2)
                        if (secondsFromLastReport > mlsReportSafeZoneEachSec && secondsFromLastSignalReceived < (mlsReportSafeZoneEachSec*2)) {
                            log.info("Reporting MLS location (in safe zone) for " + mlsReportedPoint.getDeviceName());
                            mapReporter.sendMLSReport(deviceId);
                            stateDao.deleteOldMlsReportedState(deviceId);
                            stateDao.saveNewMlsReportedState(deviceId);
                        }
                    } else {

//                        log.info("Device NOT IN SAFE zone for " + mlsReportedPoint.getDeviceName());
                        log.info("secondsFromLastReport = " + secondsFromLastReport);
                        log.info("mlsReportOutOfSafeZoneEachSec = " + mlsReportOutOfSafeZoneEachSec);
                        log.info("secondsFromLastSignalReceived = " + secondsFromLastSignalReceived);

                        // Device NOT IN SAFE zone!
                        if (secondsFromLastReport > mlsReportOutOfSafeZoneEachSec && secondsFromLastSignalReceived < (mlsReportOutOfSafeZoneEachSec*2)) {
                            log.info(
                                "Reporting MLS location (out of safe zone) for " + mlsReportedPoint.getDeviceName());
                            mapReporter.sendMLSReport(deviceId);
                            stateDao.deleteOldMlsReportedState(deviceId);
                            stateDao.saveNewMlsReportedState(deviceId);
                        }
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
