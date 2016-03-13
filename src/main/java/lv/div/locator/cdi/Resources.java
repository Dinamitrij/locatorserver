package lv.div.locator.cdi;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Logger;

/**
 * CDI resources provider
 */
@Dependent
public class Resources {

    @Produces
    public Logger createLogger(InjectionPoint classInjector) {
        Logger log = Logger.getLogger(classInjector.getMember().getDeclaringClass().getName());
        return log;
    }
}
