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

    private static boolean hasLog4j() {
        try {
            Class.forName("org.apache.log4j.Logger");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
