package lv.div.locator.report;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.conf.Conf;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.dao.StateDao;
import lv.div.locator.healthcheck.AlertSender;
import lv.div.locator.model.State;
import lv.div.locator.utils.Utils;
import org.joda.time.DateTime;
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
public class LatestSignalReportSender {

    @EJB
    private StateDao stateDao;

    @EJB
    private AlertSender alertSender;

    @EJB
    private ConfigurationManager configurationManager;

    @Inject
    private Logger log;

    @Schedule(dayOfWeek = "*", hour = "*", minute = "59", second = "0", persistent = false)
    public void sendMLSReportIfNeeded() {
        try {

            configurationManager.fetchGlobalConfiguration();

            final DateTime now = DateTime.now();

            final List<State> allStates = stateDao.findAll(State.class);
            if (allStates.isEmpty()) {
                log.warning("States are empty for all devices! Skip Latest Signal reporting");
                return;
            }

            final Map<String, List<State>> allStatesById =
                allStates.stream().collect(Collectors.groupingBy(a -> a.getDeviceId()));

            for (String deviceId : allStatesById.keySet()) {
                // Let's work with downloaded states on the App side and reduce database calls count:

                final String deviceAlias =
                    Conf.getInstance().deviceValues.get(deviceId).get(ConfigurationKey.DEVICE_ALIAS).getValue();

                final State latestSignalFromDevice =
                    allStatesById.get(deviceId).stream().filter(a -> ConfigurationKey.DEVICE_SIGNAL.equals(a.getKey()))
                        .findFirst().orElse(null);

                if (null == latestSignalFromDevice) { // MLS not reported yet. Send anyway
                    log.info("Latest signal registration not found for: " + deviceAlias);
                } else {

                    final Timestamp latestSignalTS = latestSignalFromDevice.getDateValue();
                    alertSender.sendZoneAdminAlert(deviceId, "Last signal was " +
                                                             Utils.readableTimeDiff(latestSignalTS.getTime()) + " ago");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
