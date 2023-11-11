package testlog;

import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import testlog.impl.LogCallback;
import testlog.impl.Logging;
import testlog.impl.LoggingFactory;
import testlog.strategy.AssertionStrategy;
import testlog.strategy.CountStrategy;
import testlog.strategy.LevelsStrategy;
import testlog.strategy.MatcherStrategy;
import testlog.strategy.NoopStrategy;

import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Track the log and assert if a log occurs from a given level or higher. Meant to be used during unit tests
 */
public class LogAsserter implements LogCallback, Closeable {

    public static final NoopStrategy NOOP_STRATEGY = new NoopStrategy();

    private static final int MAXIMUM_MESSAGE_LENGTH = 80;

    private static final long MAXIMUM_TIME_OUT = 5_000;

    private static final Logger logger = LoggerFactory.getLogger(LogAsserter.class);

    private final Logging logging;

    private final Level minimumLevel;

    private AssertionError assertionError;

    private AssertionStrategy assertionStrategy;

    private List<LogItem> history = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param minimumLevel minimum log level to assert on
     */
    @SuppressWarnings("WeakerAccess")
    public LogAsserter(Level minimumLevel) {
        assertionStrategy = NOOP_STRATEGY;
        this.minimumLevel = minimumLevel;
        logging = LoggingFactory.getLogging();
        initialize();
    }

    /**
     * Assert the current expectations and reset the expectation. (So that going forward all logs will be asserted
     * again)
     */
    @SuppressWarnings("WeakerAccess")
    public void assertAndReset() {
        try {
            if (assertionStrategy.hasRemainingExpectations()) {
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
                    String remaining = assertionStrategy.describeRemainingExpectations();
                    throw new Error(format("waiting for expected log entries got interrupted: %s",
                            remaining), exception);
                }
            }
            throwPreparedAssertionError();
        } finally {
            assertionStrategy = NOOP_STRATEGY;
            history.clear();
        }
    }

    @Override
    public void close() {
        tearDown();
    }

    /**
     * Expect log events to be validated by the given matchers, in the given order.
     *
     * @param matchers matchers for log events to expect
     * @return a closeable object that can be used to trigger assertion and reset of the expectations upon leaving a
     * {@code try} block
     */
    @SafeVarargs
    public final ExpectedLogs expect(Matcher<LogItem>... matchers) {
        return expect(new MatcherStrategy(matchers));
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
        // do some gymnastics here for backward compatibility
        LevelsStrategy strategy = assertionStrategy instanceof LevelsStrategy
                                  ? ((LevelsStrategy) assertionStrategy).addExpectations(levels)
                                  : new LevelsStrategy(levels);

        return expect(strategy);
    }

    /**
     * Expect the given number of log events, only those that aren't muted, typically warnings and errors.
     *
     * @param count number of log events to expect
     * @return a closeable object that can be used to trigger assertion and reset of the expectations upon leaving a
     * {@code try} block
     */
    public ExpectedLogs expect(int count) {
        return expect(new CountStrategy(count));
    }

    public ExpectedLogs expect(AssertionStrategy strategy) {
        assertionStrategy = strategy;
        return this::assertAndReset;
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level.toInt() < minimumLevel.toInt()) {
            return;
        }

        LogItem logItem = new LogItem(level, message, throwable);
        history.add(logItem);
        if (assertionStrategy.matchesNextExpectation(logItem)) {
            logInfoIfBelowMinimumLevel("allowed log at level %s: %s", level, message);
            synchronized (this) {
                notify(); // see the wait in tearDown
            }
            return;
        }

        if (assertionError == null) {
            assertUnexpectedLogging(logItem);
        }
        assertionStrategy.removeLaterExpectationForEfficiency(logItem);
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

    protected void assertUnexpectedLogging(LogItem logItem) {
        String template = "Unexpected %s log during test execution with the following message: %s"
                + "\n"
                + "History:"
                + "%s"
                + "\n"
                + "(now follows once more the stacktrace for the log item that caused this)";
        String exceptionMessage = format(template, logItem.getLevel(), getMessageSummary(logItem), getHistory(logItem));
        assertionError = new AssertionError(exceptionMessage, logItem.getThrowable());
    }

    protected void initialize() {
        logging.registerCallback(this);
    }

    Logging getDelegate() {
        return logging;
    }

    private void assertExpectationsIsEmptyAfterWait() {
        if (assertionStrategy.hasRemainingExpectations()) {
            int count = assertionStrategy.getRemainingCount();
            String remaining = assertionStrategy.describeRemainingExpectations();
            String format = "%d expected log entries did not occur after waiting %dms: %s";
            throw new AssertionError(format(format, count, MAXIMUM_TIME_OUT, String.join(", ", remaining)));
        }
    }

    private String getHistory(LogItem logItem) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= history.size(); i++) {
            LogItem item = history.get(i - 1);
            stringBuilder.append(format("\n (%d) %s: %s", i, item.getLevel(), item.getMessage()));
            if (logItem == item) {
                stringBuilder.append(format("\n (%d) -- this is the one that caused the log asserter to fail --", i));
            }

            Throwable throwable = item.getThrowable();
            if (throwable != null) {
                stringBuilder.append(format("\n (%d)", i));
                for (String line : stackTrace(throwable).split("\n")) {
                    stringBuilder.append(format("\n (%d)   %s", i, line.replace("\t", "    ")));
                }
                stringBuilder.append(format("\n (%d)", i));
            }
        }
        return stringBuilder.toString();
    }

    private String getMessageSummary(LogItem logItem) {
        String message = logItem.getMessage();
        return message.length() > MAXIMUM_MESSAGE_LENGTH
               ? format("%s... (abbreviated, see full message below)", message.substring(0, MAXIMUM_MESSAGE_LENGTH - 5))
               : message;
    }

    private void logInfoIfBelowMinimumLevel(String format, Object... args) {
        if (Level.INFO.toInt() < minimumLevel.toInt()) {
            logger.info(format(format, args));
        }
    }

    private String stackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private void throwPreparedAssertionError() {
        if (assertionError != null) {
            throw assertionError;
        }
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
}
