package lv.div.locator.utils;

/**
 * Common service exception
 */
public class LocatorServiceException extends RuntimeException {

    public LocatorServiceException() {
    }

    public LocatorServiceException(String s) {
        super(s);
    }

    public LocatorServiceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public LocatorServiceException(Throwable throwable) {
        super(throwable);
    }

    public LocatorServiceException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
