package testlog.impl;

import org.slf4j.event.Level;

public interface LogCallback {
    void log(Level level, String message, Throwable throwable);
}
