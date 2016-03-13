package lv.div.locator.conf;

import lv.div.locator.commons.conf.ConfigurationKey;
import lv.div.locator.model.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration holder
 */
public class Conf {

    private static Conf ourInstance = new Conf();

    public static Conf getInstance() {
        return ourInstance;
    }

    private Conf() {
    }

    public static Map<ConfigurationKey, Configuration> globals = new HashMap();

    public static Map<String, Map<ConfigurationKey, Configuration>> deviceValues = new HashMap();

}
