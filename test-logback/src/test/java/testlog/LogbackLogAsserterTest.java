package testlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import testlog.impl.Logging;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LogbackLogAsserterTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFactoryTest.class);

    @Test
    public void testCloseWithUnexpectedLog() {
        try {
            try (LogAsserter ignored = LogAsserter.setUpLogAsserter(Level.WARN)) {
                logger.error("error statement");
            }
            fail("expected an exception for the unexpected log");
        } catch (AssertionError exception) {
            assertEquals("Unexpected ERROR log during test execution: error statement", exception.getMessage());
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
    public void testDelegate() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        Logging actual = subject.getDelegate();
        assertEquals("LogbackLogging", actual.getClass().getSimpleName());
    }

    @Test
    public void testExpectedError() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");
        subject.tearDown();
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
        subject.expect(Level.WARN);
        logger.warn("warn statement");
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
        CaptureInfoAppender appender = new CaptureInfoAppender();
        appender.register();

        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        subject.expect(Level.ERROR);
        logger.error("error statement");

        subject.tearDown();
        appender.assertMessages("allowed log at level ERROR: error statement");
        appender.unregister();
    }

    @Test
    public void testNoLogInfoForExpectedLog() {
        CaptureInfoAppender appender = new CaptureInfoAppender();
        appender.register();

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
    public void testUnexpectedError() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        logger.error("error statement");

        validateException(subject, "Unexpected ERROR log during test execution: error statement");
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
    public void testUnexpectedMultipleErrors() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        logger.error("error statement 1");
        logger.error("error statement 2");

        // make sure we're asserting the first unexpected error, not the second
        validateException(subject, "Unexpected ERROR log during test execution: error statement 1");
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

    private void validateException(LogAsserter subject, String expected) {
        try {
            subject.tearDown();
            fail("expected an exception for the unexpected log");
        } catch (AssertionError exception) {
            assertEquals(expected, exception.getMessage());
        }
    }

    private static class CaptureInfoAppender extends AppenderBase<ILoggingEvent> {
        private List<String> messages = new ArrayList<>();

        public CaptureInfoAppender() {
            started = true;
        }

        public void assertMessages(String... expected) {
            assertEquals(asList(expected), messages);
        }

        public void register() {
            getRootLogger().addAppender(this);
        }

        public void unregister() {
            getRootLogger().detachAppender(this);
        }

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (eventObject.getLevel().equals(ch.qos.logback.classic.Level.INFO)) {
                messages.add(eventObject.getMessage());
            }
        }

        private ch.qos.logback.classic.Logger getRootLogger() {
            return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        }
    }
}
