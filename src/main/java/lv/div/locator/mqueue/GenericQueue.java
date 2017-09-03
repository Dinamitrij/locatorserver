package lv.div.locator.mqueue;

import java.util.HashMap;

/**
 * Common methods for all queues:
 */
public class GenericQueue {

    public HashMap<String, Object> getDefaultQueueArguments() {
        final HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 120000);
        return arguments;
    }
}
