package testlog.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.helpers.NOPAppender;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class LogbackLogging implements Logging {
    private final Map<LogCallback, Appender<ILoggingEvent>> logCallbacks = new HashMap<>();

    private final List<Appender<ILoggingEvent>> savedAppenders = new ArrayList<>();

    @Override
    public void deregisterCallback(LogCallback logCallback) {
        Appender<ILoggingEvent> appender = logCallbacks.get(logCallback);
        getRootLogger().detachAppender(appender);
    }

    @Override
    public void mute() {
        Logger rootLogger = getRootLogger();
        saveAppenders(rootLogger);
        rootLogger.addAppender(getNopAppender());
    }

    @Override
    public void registerCallback(LogCallback logCallback) {
        Appender<ILoggingEvent> appender = buildAppender(logCallback);
        logCallbacks.put(logCallback, appender);
        getRootLogger().addAppender(appender);
    }

    @Override
    public void unmute() {
        Logger rootLogger = getRootLogger();
        rootLogger.detachAndStopAllAppenders();
        restoreAppenders(rootLogger);
    }

    private Appender<ILoggingEvent> buildAppender(LogCallback logCallback) {
        return new CallbackAppender(logCallback);
    }

    private Appender<ILoggingEvent> getNopAppender() {
        NOPAppender<ILoggingEvent> appender = new NOPAppender<>();
        appender.setContext((Context) LoggerFactory.getILoggerFactory());
        return appender;
    }

    private Logger getRootLogger() {
        return (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    private void restoreAppenders(Logger rootLogger) {
        for (Appender<ILoggingEvent> savedAppender : savedAppenders) {
            rootLogger.addAppender(savedAppender);
        }
        savedAppenders.clear();
    }

    private void saveAppenders(Logger rootLogger) {
        Iterator<Appender<ILoggingEvent>> allAppenders = rootLogger.iteratorForAppenders();
        while (allAppenders.hasNext()) {
            Appender<ILoggingEvent> appender = allAppenders.next();
            savedAppenders.add(appender);
            rootLogger.detachAppender(appender);
        }
    }

    private class CallbackAppender extends AppenderBase<ILoggingEvent> {
        private final LogCallback logCallback;

        public CallbackAppender(LogCallback logCallback) {
            this.logCallback = logCallback;
            this.started = true;
            setContext((Context) LoggerFactory.getILoggerFactory());
        }

        @Override
        protected void append(ILoggingEvent event) {
            ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
            Throwable throwable = throwableProxy == null ? null : throwableProxy.getThrowable();
            logCallback.log(LogbackLevelUtil.convertLevel(event.getLevel()), event.getFormattedMessage(), throwable);
        }
    }
}
