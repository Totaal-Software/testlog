package testlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testlog.impl.Logging;

import static org.junit.Assert.assertEquals;

public class LogbackLogMuterTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFactoryTest.class);

    private int counter = 0;

    private Appender<ILoggingEvent> testAppender = new AppenderForTest();

    @Before
    public void setUp() {
        getRootLogger().addAppender(testAppender);
    }

    @After
    public void tearDown() {
        getRootLogger().detachAppender(testAppender);
    }

    @Test
    public void testClose() {
        assertLogIsNotMuted();

        try (LogMuter ignored = LogMuter.setupLogMuter()) {
            assertLogIsMuted();
        }

        assertLogIsNotMuted();
    }

    @Test
    public void testDelegate() {
        LogMuter subject = LogMuter.setupLogMuter();
        Logging actual = subject.getDelegate();
        assertEquals("LogbackLogging", actual.getClass().getSimpleName());
    }

    @Test
    public void testSetupLogMuter() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();

        assertLogIsMuted();
        subject.tearDown();
    }

    @Test
    public void testTearDown() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        subject.tearDown();

        assertLogIsNotMuted();
    }

    @Test
    public void testUseConstructor() {
        assertLogIsNotMuted();

        LogMuter subject = new LogMuter();
        assertLogIsNotMuted();

        subject.disableOutput();
        assertLogIsMuted();

        subject.tearDown();
    }

    private void assertLogIsMuted() {
        int currentCount = counter;
        logger.info("log statement");
        assertEquals(currentCount, counter);
    }

    private void assertLogIsNotMuted() {
        int currentCount = counter;
        logger.info("log statement");
        assertEquals(currentCount + 1, counter);
    }

    private ch.qos.logback.classic.Logger getRootLogger() {
        return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    }

    private class AppenderForTest extends AppenderBase<ILoggingEvent> {
        AppenderForTest() {
            started = true;
        }

        @Override
        protected void append(ILoggingEvent eventObject) {
            counter++;
        }
    }
}
