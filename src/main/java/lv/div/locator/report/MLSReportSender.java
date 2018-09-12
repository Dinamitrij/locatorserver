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
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Sending reports for MLS location points.
 * This mechanism is supposed to be called by CRON task.
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

    @Schedule(dayOfWeek = "*", hour = "*", minute = "*", second = "0", persistent = false)
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
                // Let's work with downloaded states on the App side and reduce database calls count:

                final String deviceAlias = Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.DEVICE_ALIAS).getValue();

                final State mlsReportedPoint =
                    allStatesById.get(deviceId).stream().filter(a -> ConfigurationKey.MLS_REPORTED.equals(a.getKey()))
                        .findFirst().orElse(null);

                if (null == mlsReportedPoint) { // MLS not reported yet. Send anyway
                    log.info("MLS: Reporting MLS location (initial) for device \"" + deviceAlias + "\"");
                    mapReporter.sendMLSReport(deviceId);
                    stateDao.deleteOldMlsReportedState(deviceId);
                    stateDao.saveNewMlsReportedState(deviceId);
                } else {

                    final Timestamp mlsWasReportedAtTS = mlsReportedPoint.getDateValue();
                    final DateTime mlsWasReportedAt = new DateTime(mlsWasReportedAtTS.getTime());
                    final Seconds seconds = Seconds.secondsBetween(now, mlsWasReportedAt);
                    final int secondsFromLastMLSReport = Math.abs(seconds.getSeconds());

                    // IN_SAFE_ZONE & OUT_OF_SAFE_ZONE flags are constantly updated, when APP is online.
                    // Long time after these flags update, means APP if switched off, so -
                    // not needed to report MLS.
                    final State deviceInSafeZone =
                        allStatesById.get(deviceId).stream().filter(a -> ConfigurationKey.IN_SAFE_ZONE.equals(a.getKey()))
                            .findFirst().orElse(null);
                    final State deviceOutOfSafeZone =
                        allStatesById.get(deviceId).stream().filter(a -> ConfigurationKey.OUT_OF_SAFE_ZONE.equals(a.getKey()))
                            .findFirst().orElse(null);

                    int secondsFromLastSignalInSafeZoneReceived = 0;
                    int secondsFromLastSignalOutOfSafeZoneReceived = 0;
                    if (null != deviceInSafeZone) {
                        log.info("MLS: Device in SafeZone - " + deviceAlias);
                        final DateTime lastSignalWhenInSafeZone = new DateTime(deviceInSafeZone.getDateValue());
                        final Seconds s = Seconds.secondsBetween(now, lastSignalWhenInSafeZone);
                        secondsFromLastSignalInSafeZoneReceived = Math.abs(s.getSeconds());
                    } else if (null != deviceOutOfSafeZone) {
                        log.info("MLS: Device NOT in SafeZone " + deviceAlias);
                        final DateTime lastSignalWhenOutOfSafeZone = new DateTime(deviceOutOfSafeZone.getDateValue());
                        final Seconds s = Seconds.secondsBetween(now, lastSignalWhenOutOfSafeZone);
                        secondsFromLastSignalOutOfSafeZoneReceived = Math.abs(s.getSeconds());
                    }

                    if (null != deviceInSafeZone) { // if device is in SAFE zone...
                        log.info("secondsFromLastMLSReport = " + secondsFromLastMLSReport);
                        log.info("mlsReportSafeZoneEachSec = " + mlsReportSafeZoneEachSec);
                        log.info("secondsFromLastSignalInSafeZoneReceived = " + secondsFromLastSignalInSafeZoneReceived);

                        // Do not report OLD states (mlsReportSafeZoneEachSec*2)
                        if (secondsFromLastMLSReport > mlsReportSafeZoneEachSec && secondsFromLastSignalInSafeZoneReceived < (mlsReportSafeZoneEachSec*2)) {
                            log.info("Reporting MLS location (in safe zone) for " + mlsReportedPoint.getDeviceName());
                            mapReporter.sendMLSReport(deviceId);
                            stateDao.deleteOldMlsReportedState(deviceId);
                            stateDao.saveNewMlsReportedState(deviceId);
                        }
                    } else {
                        log.info("secondsFromLastMLSReport = " + secondsFromLastMLSReport);
                        log.info("mlsReportOutOfSafeZoneEachSec = " + mlsReportOutOfSafeZoneEachSec);
                        log.info("secondsFromLastSignalOutOfSafeZoneReceived = " + secondsFromLastSignalOutOfSafeZoneReceived);

                        // Device NOT IN SAFE zone!
                        if (secondsFromLastMLSReport > mlsReportOutOfSafeZoneEachSec && secondsFromLastSignalOutOfSafeZoneReceived < (mlsReportOutOfSafeZoneEachSec*2)) {
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
