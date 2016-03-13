package lv.div.locator.utils;

import java.sql.Timestamp;
import java.util.Date;

public class LocUtl {

    /**
     * Makes DB-compatible Date value
     *
     * @param date
     *
     * @return
     */
    public static Timestamp dbDate(Date date) {
        return new Timestamp(date.getTime());
    }

}
