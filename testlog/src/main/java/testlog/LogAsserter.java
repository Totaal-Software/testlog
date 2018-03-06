package testlog;


import org.slf4j.event.Level;
import testlog.impl.LogCallback;
import testlog.impl.Logging;
import testlog.impl.LoggingFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Track the log and assert if a log occurs from a given level or higher. Meant to be used during unit tests
 */
public class LogAsserter implements LogCallback, Closeable {
    private List<Level> expectations = new ArrayList<>();

    private final Logging logging;

    private final Level minimumLevel;

    /**
     * Constructor.
     *
     * @param minimumLevel minimum log level to assert on
     */
    public LogAsserter(Level minimumLevel) {
        this.minimumLevel = minimumLevel;
        logging = LoggingFactory.getLogging();
        logging.registerCallback(this);
    }

    /**
     * Set up a new log asserter.
     *
     * @param minimumLevel minimum log level to assert on
     * @return new log asserter
     */
    public static LogAsserter setUpLogAsserter(Level minimumLevel) {
        return new LogAsserter(minimumLevel);
    }

    @Override
    public void close() throws IOException {
        tearDown();
    }

    /**
     * Expect a log event of the given level. (Can be called multiple times to set up subsequent expectations)
     *
     * @param level log event level to expect
     */
    public void expect(Level level) {
        expectations.add(level);
    }

    public Logging getDelegate() {
        return logging;
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (expectations.size() >= 1 && expectations.remove(0).equals(level)) {
            return;
        }
        if (level.toInt() >= minimumLevel.toInt()) {
            String error = format("Unexpected %s. Log: %s", level, "");
            throw new AssertionError(error, throwable);
        }
    }

    /**
     * Tear down the log asserter. Don't forget to tear down, else subsequent tests that are executed will assert too,
     * since the logging infrastructure may be static
     */
    public void tearDown() {
        logging.deregisterCallback(this);
    }
}
