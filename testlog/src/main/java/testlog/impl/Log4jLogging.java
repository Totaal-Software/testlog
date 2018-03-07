package testlog.impl;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.log4j.varia.NullAppender;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

class Log4jLogging implements Logging {
    private final Map<LogCallback, Appender> logCallbacks = new HashMap<>();

    private final List<Appender> savedAppenders = new ArrayList<>();

    @Override
    public void deregisterCallback(LogCallback logCallback) {
        Appender appender = logCallbacks.get(logCallback);
        Logger.getRootLogger().removeAppender(appender);
    }

    @Override
    public void mute() {
        Logger rootLogger = Logger.getRootLogger();
        saveAppenders(rootLogger);
        rootLogger.addAppender(new NullAppender());
    }

    @Override
    public void registerCallback(LogCallback logCallback) {
        Appender appender = buildAppender(logCallback);
        logCallbacks.put(logCallback, appender);
        Logger.getRootLogger().addAppender(appender);
    }

    @Override
    public void unmute() {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAllAppenders();
        restoreAppenders(rootLogger);
    }

    private Appender buildAppender(LogCallback logCallback) {
        return new NullAppender() {
            @Override
            public void doAppend(LoggingEvent event) {
                ThrowableInformation throwableInformation = event.getThrowableInformation();
                Throwable throwable = throwableInformation == null ? null : throwableInformation.getThrowable();
                logCallback.log(Log4jLevelUtil.convertLevel(event.getLevel()), event.getRenderedMessage(), throwable);
            }
        };
    }

    private void restoreAppenders(Logger rootLogger) {
        for (Appender savedAppender : savedAppenders) {
            rootLogger.addAppender(savedAppender);
        }
        savedAppenders.clear();
    }

    private void saveAppenders(Logger rootLogger) {
        @SuppressWarnings("unchecked")
        Enumeration<Appender> allAppenders = rootLogger.getAllAppenders();
        while (allAppenders.hasMoreElements()) {
            Appender appender = allAppenders.nextElement();
            savedAppenders.add(appender);
            rootLogger.removeAppender(appender);
        }
    }
}
