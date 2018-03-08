package testlog;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public abstract class AbstractLogAsserterTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLogAsserterTest.class);

    @Before
    public void setUp() {
        enableTraceLogging();
    }

    @Test
    public void testCloseWithUnexpectedLog() {
        boolean fail = false;
        try {
            try (LogAsserter ignored = LogAsserter.setUpLogAsserter(Level.WARN)) {
                logger.error("error statement");
            }
            fail = true;
        } catch (AssertionError exception) {
            assertEquals("Unexpected ERROR log during test execution: error statement", exception.getMessage());
        }
        if (fail) {
            fail("expected an exception for the unexpected log");
        }
    }

    @Test
    public void testCloseWithoutUnexpectedLog() {
        try (LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN)) {
            subject.expect(Level.ERROR);
            logger.error("error statement");
        }
    }

    @Test
    public void testExpectedError() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");
        subject.tearDown();
    }

    @Test
    public void testExpectedErrorComingLate() throws InterruptedException {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);

        final Throwable[] caught = new Throwable[1];
        Thread testThread = new Thread(() -> {
            try {
                subject.tearDown();
            } catch (Throwable throwable) {
                caught[0] = throwable;
            }
        });
        testThread.start();

        sleep(750);
        Thread logThread = new Thread(() -> logger.error("error statement"));
        logThread.start();

        testThread.join();
        logThread.join();

        assertNull(caught[0]);
    }

    @Test
    public void testExpectedErrorNotOccurred() {
        boolean fail = false;
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        try {
            subject.tearDown();
            fail = true;
        } catch (AssertionError exception) {
            assertEquals(
                    "1 expected log entries did not occur after waiting 5000ms: [ERROR]", exception.getMessage());
        }
        if (fail) {
            fail("expected an exception for the unexpected log");
        }
    }

    @Test
    public void testExpectedErrorsNotOccurred() {
        boolean fail = false;
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        subject.expect(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");
        try {
            subject.tearDown();
            fail = true;
        } catch (AssertionError exception) {
            assertEquals(
                    "2 expected log entries did not occur after waiting 5000ms: [WARN, ERROR]", exception.getMessage());
        }
        if (fail) {
            fail("expected an exception for the expected log that did not occur");
        }
    }

    @Test
    public void testExpectedTwoLogs() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR, Level.WARN);
        logger.error("error statement");
        logger.warn("warn statement");
        subject.tearDown();
    }

    @Test
    public void testExpectedTwoLogsWrongOrder() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        subject.expect(Level.ERROR);
        subject.expect(Level.WARN);
        logger.warn("warn statement");
        logger.error("error statement");
        logger.error("error statement");

        validateException(subject, "Unexpected WARN log during test execution: warn statement");
    }

    @Test
    public void testExpectedTwoSeparateLogs() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        subject.expect(Level.WARN);
        logger.error("error statement");
        logger.warn("warn statement");
        subject.tearDown();
    }

    @Test
    public void testLogInfoForExpectedLog() {
        CaptureInfoAppender appender = registerAppender();

        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");

        subject.tearDown();
        appender.assertMessages("allowed log at level ERROR: error statement");
        appender.unregister();
    }

    @Test
    public void testNoLogInfoForExpectedLog() {
        CaptureInfoAppender appender = registerAppender();

        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.INFO);
        subject.expect(Level.ERROR);
        logger.error("error statement");

        subject.tearDown();
        appender.assertMessages(/* empty */);
        appender.unregister();
    }

    @Test
    public void testNoUnexpectedErrors() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.ERROR);
        logger.warn("warn statement");

        subject.tearDown();
    }

    @Test
    public void testNoUnexpectedWarnings() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        logger.info("info statement");

        subject.tearDown();
    }

    @Test
    public void testTearDownInterrupted() throws InterruptedException {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);

        final Throwable[] caught = new Throwable[1];
        Thread testThread = new Thread(() -> {
            try {
                subject.tearDown();
            } catch (Throwable throwable) {
                caught[0] = throwable;
            }
        });
        testThread.start();

        sleep(750);
        testThread.interrupt();

        testThread.join();

        assertNotNull(caught[0]);
        assertEquals(Error.class, caught[0].getClass());
        assertEquals("waiting for expected log entries got interrupted: [ERROR]", caught[0].getMessage());
    }

    @Test
    public void testUnexpectedDebug() throws InterruptedException {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.DEBUG);
        logger.debug("debug statement");

        validateException(subject, "Unexpected DEBUG log during test execution: debug statement");
    }

    @Test
    public void testUnexpectedError() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        logger.error("error statement");

        validateException(subject, "Unexpected ERROR log during test execution: error statement");
    }

    @Test
    public void testUnexpectedErrorComingLate() throws InterruptedException {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        subject.expect(Level.ERROR);

        final Throwable[] caught = new Throwable[1];
        Thread testThread = new Thread(() -> {
            try {
                subject.tearDown();
            } catch (Throwable throwable) {
                caught[0] = throwable;
            }
        });
        testThread.start();

        sleep(750);
        Thread logThread = new Thread(() -> {
            logger.warn("warn statement");
            logger.error("error statement");
        });
        logThread.start();

        testThread.join();
        logThread.join();

        assertNotNull(caught[0]);
        assertEquals(AssertionError.class, caught[0].getClass());
        assertEquals("Unexpected WARN log during test execution: warn statement", caught[0].getMessage());
    }

    @Test
    public void testUnexpectedErrorNotFirst() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement 1");
        logger.error("error statement 2");

        validateException(subject, "Unexpected ERROR log during test execution: error statement 2");
    }

    @Test
    public void testUnexpectedErrorWithException() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        logger.error("error statement", new IOException("something"));

        validateException(subject, "Unexpected ERROR log during test execution: error statement; " +
                "throwable: java.io.IOException: something");
    }

    @Test
    public void testUnexpectedInfo() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.INFO);
        logger.info("info statement");

        validateException(subject, "Unexpected INFO log during test execution: info statement");
    }

    @Test
    public void testUnexpectedMultipleErrors() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        logger.error("error statement 1");
        logger.error("error statement 2");

        // make sure we're asserting the first unexpected error, not the second
        validateException(subject, "Unexpected ERROR log during test execution: error statement 1");
    }

    @Test
    public void testUnexpectedTrace() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.TRACE);
        logger.trace("trace statement");

        validateException(subject, "Unexpected TRACE log during test execution: trace statement");
    }

    @Test
    public void testUnexpectedWarning() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        logger.warn("warn statement");

        validateException(subject, "Unexpected WARN log during test execution: warn statement");
    }

    @Test
    public void testUseConstructor() {
        LogAsserter subject = new LogAsserter(Level.WARN);
        logger.error("error statement");

        validateException(subject, "Unexpected ERROR log during test execution: error statement");
    }

    @Test
    public void testWithAlsoLogBelowMinimumLevel() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR, Level.WARN);
        logger.error("error statement");
        logger.info("info statement");
        logger.warn("warn statement");
        subject.tearDown();
    }

    protected abstract CaptureInfoAppender createAppender();

    protected abstract void enableTraceLogging();

    private CaptureInfoAppender registerAppender() {
        CaptureInfoAppender appender = createAppender();
        appender.register();
        return appender;
    }

    private void validateException(LogAsserter subject, String expected) {
        boolean fail = false;
        try {
            subject.tearDown();
            fail = true;
        } catch (AssertionError exception) {
            assertEquals(expected, exception.getMessage());
        }
        if (fail) {
            fail("expected an exception for the unexpected log");
        }
    }

    protected interface CaptureInfoAppender {
        void assertMessages(String... expected);

        void register();

        void unregister();
    }
}
