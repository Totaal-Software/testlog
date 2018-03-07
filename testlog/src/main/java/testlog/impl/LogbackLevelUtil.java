package testlog.impl;

import org.slf4j.event.Level;

import static java.lang.String.format;

public class LogbackLevelUtil {
    public static Level convertLevel(ch.qos.logback.classic.Level level) {
        if (ch.qos.logback.classic.Level.ERROR.equals(level)) {
            return Level.ERROR;
        } else if (ch.qos.logback.classic.Level.WARN.equals(level)) {
            return Level.WARN;
        } else if (ch.qos.logback.classic.Level.INFO.equals(level)) {
            return Level.INFO;
        } else if (ch.qos.logback.classic.Level.DEBUG.equals(level)) {
            return Level.DEBUG;
        } else if (ch.qos.logback.classic.Level.TRACE.equals(level)) {
            return Level.TRACE;
        }
        throw new RuntimeException(format("level %s is not supported", level));
    }
}
