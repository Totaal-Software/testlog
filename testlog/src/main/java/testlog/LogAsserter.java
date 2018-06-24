package testlog;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import testlog.impl.LogCallback;
import testlog.impl.Logging;
import testlog.impl.LoggingFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Track the log and assert if a log occurs from a given level or higher. Meant to be used during unit tests
 */
public class LogAsserter implements LogCallback, Closeable {
    private static final long MAXIMUM_TIME_OUT = 5_000;

    private static final Logger logger = LoggerFactory.getLogger(LogAsserter.class);

    private AssertionError assertionError;

    private List<Level> expectations = new ArrayList<>();

    private final Logging logging;

    private final Level minimumLevel;

    /**
     * Constructor.
     *
     * @param minimumLevel minimum log level to assert on
     */
    @SuppressWarnings("WeakerAccess")
    public LogAsserter(Level minimumLevel) {
        this.minimumLevel = minimumLevel;
        logging = LoggingFactory.getLogging();
        initialize();
    }

    /**
     * Set up a new log asserter.
     *
     * @param minimumLevel minimum log level to assert on
     * @return new log asserter
     */
    @SuppressWarnings("WeakerAccess")
    public static LogAsserter setUpLogAsserter(Level minimumLevel) {
        return new LogAsserter(minimumLevel);
    }

    /**
     * Assert the current expectations and reset the expectation. (So that going forward all logs will be asserted
     * again)
     */
    @SuppressWarnings("WeakerAccess")
    public void assertAndReset() {
        try {
            if (!expectations.isEmpty()) {
                try {
                    // wait for expectations, else they may bleed into the next test
                    // this is probably only true with something asynchronous in the chain
                    synchronized (this) {
                        wait(MAXIMUM_TIME_OUT);
                    }
                    if (assertionError == null) {
                        assertExpectationsIsEmptyAfterWait();
                    }
                } catch (InterruptedException exception) {
                    throw new Error(format("waiting for expected log entries got interrupted: %s", expectations), exception);
                }
            }
            throwPreparedAssertionError();
        } finally {
            expectations.clear();
        }
    }

    @Override
    public void close() {
        tearDown();
    }

    /**
     * Expect log events of the given levels, in the given order.
     *
     * @param levels log event levels to expect
     * @return a closeable object that can be used to trigger assertion and reset of the expectations upon leaving a
     * {@code try} block
     */
    @SuppressWarnings("WeakerAccess")
    public ExpectedLogs expect(Level... levels) {
        expectations.addAll(asList(levels));
        return this::assertAndReset;
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level.toInt() < minimumLevel.toInt()) {
            return;
        }

        if (matchesNextExpectation(level, message, throwable)) {
            logInfoIfBelowMinimumLevel("allowed log at level %s: %s", level, message);
            synchronized (this) {
                notify(); // see the wait in tearDown
            }
            return;
        }

        if (assertionError == null) {
            assertUnexpectedLogging(level, message, throwable);
        }
        removeLaterExpectationForEfficiency(level, message, throwable);
    }

    /**
     * Tear down the log asserter. Don't forget to tear down, else subsequent tests that are executed will assert too,
     * since the logging infrastructure may be static
     */
    public void tearDown() {
        try {
            assertAndReset();
        } finally {
            logging.deregisterCallback(this);
        }
    }

    private void assertExpectationsIsEmptyAfterWait() {
        if (!expectations.isEmpty()) {
            String format = "%d expected log entries did not occur after waiting %dms: %s";
            throw new AssertionError(format(format, expectations.size(), MAXIMUM_TIME_OUT, expectations));
        }
    }

    private void assertUnexpectedLogging(Level level, String message, Throwable throwable) {
        String exceptionMessage = format("Unexpected %s log during test execution: %s", level, message);
        if (throwable != null) {
            exceptionMessage += format("; throwable: %s", throwable);
        }
        assertionError = new AssertionError(exceptionMessage, throwable);
    }

    Logging getDelegate() {
        return logging;
    }

    protected void initialize() {
        logging.registerCallback(this);
    }

    private void logInfoIfBelowMinimumLevel(String format, Object... args) {
        if (Level.INFO.toInt() < minimumLevel.toInt()) {
            logger.info(format(format, args));
        }
    }

    private boolean matchesExpectation(Level expected, Level actual) {
        return actual.equals(expected);
    }

    private boolean matchesNextExpectation(Level expected) {
        Level actual = expectations.remove(0);
        return matchesExpectation(expected, actual);
    }

    private boolean matchesNextExpectation(Level level, String message, Throwable throwable) {
        return !expectations.isEmpty() && matchesNextExpectation(level);
    }

    private void removeLaterExpectationForEfficiency(Level expected, String message, Throwable throwable) {
        Iterator<Level> iterator = expectations.iterator();
        while (iterator.hasNext()) {
            Level actual = iterator.next();
            if (matchesExpectation(expected, actual)) {
                iterator.remove();
                return;
            }
        }
    }

    private void throwPreparedAssertionError() {
        if (assertionError != null) {
            throw assertionError;
        }
    }
}
