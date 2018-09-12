package lv.div.locator.utils;

import java.util.Date;

public class Utils {

    /**
     * Find printable / human readable difference between 2 dates:
     *
     * @param sourceTimeInMilliseconds
     *
     * @return
     */
    public static String readableTimeDiff(long sourceTimeInMilliseconds) {

        final Date now = new Date();
        StringBuffer timeAgo = new StringBuffer();

        long diff = now.getTime() - sourceTimeInMilliseconds;

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;

        if (diffHours > 0) {
            timeAgo.append(diffHours);
            timeAgo.append("h ");
        }
        if (diffMinutes > 0) {
            timeAgo.append(diffMinutes);
            timeAgo.append("m ");
        }

        timeAgo.append(diffSeconds);
        timeAgo.append("s");

        return timeAgo.toString();

    }

}
