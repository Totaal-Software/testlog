package testlog;

import testlog.impl.Logging;
import testlog.impl.LoggingFactory;

import java.io.Closeable;

/**
 * Mute the log. Meant to be used during unit tests
 */
public class LogMuter implements Closeable {

    private final Logging logging;

    private boolean mute = false;

    public LogMuter() {
        logging = LoggingFactory.getLogging();
    }

    /**
     * Set up a new log muter.
     *
     * @return new log muter
     */
    public static LogMuter setupLogMuter() {
        LogMuter logMuter = new LogMuter();
        logMuter.disableOutput();
        return logMuter;
    }

    @Override
    public void close() {
        tearDown();
    }

    /**
     * Disable log output (mute the log).
     */
    public void disableOutput() {
        if (!mute) {
            logging.mute();
            mute = true;
        }
    }

    /**
     * Enable log output (unmute the log).
     */
    public void enableOutput() {
        if (mute) {
            logging.unmute();
            mute = false;
        }
    }

    public Logging getDelegate() {
        return logging;
    }

    /**
     * Tear down the log muter. Don't forget to tear down, else subsequent tests that are executed will have their log
     * muted too, since the logging infrastructure may be static
     */
    public void tearDown() {
        enableOutput();
    }
}
