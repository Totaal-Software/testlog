package testlog.impl;

import org.slf4j.event.Level;

import static java.lang.String.format;

public class Log4jLevelUtil {
    public static Level convertLevel(org.apache.log4j.Level level) {
        if (org.apache.log4j.Level.ERROR.equals(level)) {
            return Level.ERROR;
        } else if (org.apache.log4j.Level.WARN.equals(level)) {
            return Level.WARN;
        } else if (org.apache.log4j.Level.INFO.equals(level)) {
            return Level.INFO;
        } else if (org.apache.log4j.Level.DEBUG.equals(level)) {
            return Level.DEBUG;
        } else if (org.apache.log4j.Level.TRACE.equals(level)) {
            return Level.TRACE;
        }
        throw new RuntimeException(format("level %s is not supported", level));
    }
}
