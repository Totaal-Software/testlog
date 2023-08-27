package testlog;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
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
        try {
            try (LogAsserter ignored = callSubjectSetUpLogAsserter(Level.WARN)) {
                logger.error("error statement");
            }
            fail("expected an exception for the unexpected log");
        } catch (AssertionError exception) {
            String actual = exception.getMessage();
            assertEquals("Unexpected ERROR log during test execution with the following message: error statement\n"
                    + "History:\n"
                    + " (1) ERROR: error statement\n"
                    + " (1) -- this is the one that caused the log asserter to fail --\n"
                    + "(now follows once more the stacktrace for the log item that caused this)", actual);
        }
    }

    @Test
    public void testCloseWithUnexpectedLogAndException() {
        Exception expectedCause = new Exception("something wrong");
        try {
            try (LogAsserter ignored = callSubjectSetUpLogAsserter(Level.WARN)) {
                logger.error("error statement", expectedCause);
            }
            fail("expected an exception for the unexpected log");
        } catch (AssertionError exception) {
            String actual = exception.getMessage();
            assertThat(actual, both(startsWith("Unexpected ERROR log during test execution with the following message: "
                    + "error statement\n"
                    + "History:\n"
                    + " (1) ERROR: error statement\n"
                    + " (1) -- this is the one that caused the log asserter to fail --\n"
                    + " (1)\n"
                    + " (1)   java.lang.Exception: something wrong\n"
                    + " (1)       at testlog.AbstractLogAsserterTest.testCloseWithUnexpectedLogAndException("
                    + "AbstractLogAsserterTest.java:"))
                    .and(endsWith(" (1)\n"
                            + "(now follows once more the stacktrace for the log item that caused this)")));

            Throwable actualCause = exception.getCause();
            assertEquals(expectedCause, actualCause);
        }
    }

    @Test
    public void testCloseWithoutUnexpectedLog() {
        try (LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN)) {
            subject.expect(Level.ERROR);
            logger.error("error statement");
        }
    }

    @Test
    public void testExceptionMessageLong() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        logger.error("this old message is already 80 characters long at the upcoming very next period.",
                new IOException("something"));

        validateException(subject, startsWith("Unexpected ERROR log during test execution with the following message: "
                + "this old message is already 80 characters long at the upcoming very next period.\n"
                + "History:\n"
                + " (1) ERROR: this old message is already 80 characters long at the upcoming very next period.\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + " (1)\n"
                + " (1)   java.io.IOException: something\n"
                + " (1)       at testlog.AbstractLogAsserterTest.testExceptionMessageLong"));
    }

    @Test
    public void testExceptionMessageTooLong() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        logger.error("this old message is already 80 characters long at the upcoming very next period._",
                new IOException("something"));

        validateException(subject, startsWith("Unexpected ERROR log during test execution with the following message: "
                + "this old message is already 80 characters long at the upcoming very next pe... (abbreviated, see "
                + "full message below)\n"
                + "History:\n"
                + " (1) ERROR: this old message is already 80 characters long at the upcoming very next period._\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + " (1)\n"
                + " (1)   java.io.IOException: something\n"
                + " (1)       at testlog.AbstractLogAsserterTest.testExceptionMessageTooLong"));
    }

    @Test
    public void testExpectedError() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");
        subject.tearDown();
    }

    @Test
    public void testExpectedErrorComingLate() throws InterruptedException {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
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
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        try {
            subject.tearDown();
            fail("expected an exception for the unexpected log");
        } catch (AssertionError exception) {
            assertEquals(
                    "1 expected log entries did not occur after waiting 5000ms: ERROR", exception.getMessage());
        }
    }

    @Test
    public void testExpectedErrorsNotOccurred() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        subject.expect(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");
        try {
            subject.tearDown();
            fail("expected an exception for the expected log that did not occur");
        } catch (AssertionError exception) {
            assertEquals(
                    "2 expected log entries did not occur after waiting 5000ms: WARN, ERROR", exception.getMessage());
        }
    }

    @Test
    public void testExpectedTwoLogs() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR, Level.WARN);
        logger.error("error statement");
        logger.warn("warn statement");
        subject.tearDown();
    }

    @Test
    public void testExpectedTwoLogsWrongOrder() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        subject.expect(Level.ERROR);
        subject.expect(Level.WARN);
        logger.warn("warn statement");
        logger.error("error statement");
        logger.error("error statement");

        validateException(subject, "Unexpected WARN log during test execution with the following message: "
                + "warn statement\n"
                + "History:\n"
                + " (1) WARN: warn statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testExpectedTwoSeparateLogs() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        subject.expect(Level.WARN);
        logger.error("error statement");
        logger.warn("warn statement");
        subject.tearDown();
    }

    @Test
    public void testLogInfoForExpectedLog() {
        CaptureInfoAppender appender = registerAppender();

        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");

        subject.tearDown();
        if (isMuted()) {
            appender.assertMessages(/* empty */);
        } else {
            appender.assertMessages("allowed log at level ERROR: error statement");
        }
        appender.unregister();
    }

    @Test
    public void testNoLogInfoForExpectedLog() {
        CaptureInfoAppender appender = registerAppender();

        LogAsserter subject = callSubjectSetUpLogAsserter(Level.INFO);
        subject.expect(Level.ERROR);
        logger.error("error statement");

        subject.tearDown();
        appender.assertMessages(/* empty */);
        appender.unregister();
    }

    @Test
    public void testNoUnexpectedErrors() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.ERROR);
        logger.warn("warn statement");

        subject.tearDown();
    }

    @Test
    public void testNoUnexpectedWarnings() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        logger.info("info statement");

        subject.tearDown();
    }

    @Test
    public void testTearDownInterrupted() throws InterruptedException {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
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
        assertEquals("waiting for expected log entries got interrupted: ERROR", caught[0].getMessage());
    }

    @Test
    public void testUnexpectedDebug() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.DEBUG);
        logger.debug("debug statement");

        validateException(subject, "Unexpected DEBUG log during test execution with the following message: "
                + "debug statement\n"
                + "History:\n"
                + " (1) DEBUG: debug statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testUnexpectedError() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        logger.error("error statement");

        validateException(subject, "Unexpected ERROR log during test execution with the following message: "
                + "error statement\n"
                + "History:\n"
                + " (1) ERROR: error statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testUnexpectedErrorComingLate() throws InterruptedException {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
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
        assertEquals("Unexpected WARN log during test execution with the following message: warn statement\n"
                + "History:\n"
                + " (1) WARN: warn statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)", caught[0].getMessage());
    }

    @Test
    public void testUnexpectedErrorNotFirst() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement 1");
        logger.error("error statement 2");

        validateException(subject, "Unexpected ERROR log during test execution with the following message: "
                + "error statement 2\n"
                + "History:\n"
                + " (1) ERROR: error statement 1\n"
                + " (2) ERROR: error statement 2\n"
                + " (2) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testUnexpectedErrorWithException() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        logger.error("error statement", new IOException("something"));

        validateException(subject, startsWith("Unexpected ERROR log during test execution with the following message: "
                + "error statement\n"
                + "History:\n"
                + " (1) ERROR: error statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + " (1)\n"
                + " (1)   java.io.IOException: something\n"
                + " (1)       at testlog.AbstractLogAsserterTest.testUnexpectedErrorWithException"));
    }

    @Test
    public void testUnexpectedInfo() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.INFO);
        logger.info("info statement");

        validateException(subject, "Unexpected INFO log during test execution with the following message: "
                + "info statement\n"
                + "History:\n"
                + " (1) INFO: info statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testUnexpectedMultipleErrors() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        logger.error("error statement 1");
        logger.error("error statement 2");

        // make sure we're asserting the first unexpected error, not the second
        validateException(subject, "Unexpected ERROR log during test execution with the following message: "
                + "error statement 1\n"
                + "History:\n"
                + " (1) ERROR: error statement 1\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testUnexpectedTrace() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.TRACE);
        logger.trace("trace statement");

        validateException(subject, "Unexpected TRACE log during test execution with the following message: "
                + "trace statement\n"
                + "History:\n"
                + " (1) TRACE: trace statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testUnexpectedWarning() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        logger.warn("warn statement");

        validateException(subject, "Unexpected WARN log during test execution with the following message: "
                + "warn statement\n"
                + "History:\n"
                + " (1) WARN: warn statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testUseConstructor() {
        LogAsserter subject = callLogAsserterConstructor(Level.WARN);
        logger.error("error statement");

        validateException(subject, "Unexpected ERROR log during test execution with the following message: "
                + "error statement\n"
                + "History:\n"
                + " (1) ERROR: error statement\n"
                + " (1) -- this is the one that caused the log asserter to fail --\n"
                + "(now follows once more the stacktrace for the log item that caused this)");
    }

    @Test
    public void testWithAlsoLogBelowMinimumLevel() {
        LogAsserter subject = callSubjectSetUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR, Level.WARN);
        logger.error("error statement");
        logger.info("info statement");
        logger.warn("warn statement");
        subject.tearDown();
    }

    protected abstract LogAsserter callLogAsserterConstructor(Level level);

    protected abstract LogAsserter callSubjectSetUpLogAsserter(Level level);

    protected abstract CaptureInfoAppender createAppender();

    protected abstract void enableTraceLogging();

    protected abstract boolean isMuted();

    private CaptureInfoAppender registerAppender() {
        CaptureInfoAppender appender = createAppender();
        appender.register();
        return appender;
    }

    private void validateException(LogAsserter subject, String expected) {
        validateException(subject, equalTo(expected));
    }

    private void validateException(LogAsserter subject, Matcher<String> asExpected) {
        try {
            subject.tearDown();
            fail("expected an exception for the unexpected log");
        } catch (AssertionError exception) {
            assertThat(exception.getMessage(), asExpected);
        }
    }

    protected interface CaptureInfoAppender {
        void assertMessages(String... expected);

        void register();

        void unregister();
    }
}
