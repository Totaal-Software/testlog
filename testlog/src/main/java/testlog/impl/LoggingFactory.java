package testlog.impl;

public class LoggingFactory {
    private LoggingFactory() {
        ; // utility classes should not have a public or default constructor
    }

    public static Logging getLogging() {
        if (hasLog4j()) {
            return new Log4jLogging();
        }

        return new LogbackLogging();
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasLog4j() {
        // only logger and not appender happens for log4j-over-slf4j, in which case log4j is probably not what we want
        return hasClass("org.apache.log4j.Logger") && hasClass("org.apache.log4j.varia.NullAppender");
    }
}
