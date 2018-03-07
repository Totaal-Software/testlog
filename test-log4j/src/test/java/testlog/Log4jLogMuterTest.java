package testlog;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.NullAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testlog.impl.Logging;

import static org.junit.Assert.assertEquals;

public class Log4jLogMuterTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFactoryTest.class);

    private int counter = 0;

    private Appender testAppender = new NullAppender() {
        @Override
        public void doAppend(LoggingEvent event) {
            counter++;
        }
    };

    @Before
    public void setUp() {
        org.apache.log4j.Logger.getRootLogger().addAppender(testAppender);
    }

    @After
    public void tearDown() {
        org.apache.log4j.Logger.getRootLogger().removeAppender(testAppender);
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
        assertEquals("Log4jLogging", actual.getClass().getSimpleName());
    }

    @Test
    public void testEnableOutput() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        assertLogIsMuted();

        subject.enableOutput();
        assertLogIsNotMuted();

        subject.tearDown();
    }

    @Test
    public void testRedundantMute() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        assertLogIsMuted();

        subject.disableOutput();
        assertLogIsMuted();

        subject.tearDown();
    }

    @Test
    public void testRedundantUnmute() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        subject.enableOutput();
        assertLogIsNotMuted();

        subject.enableOutput();
        assertLogIsNotMuted();

        subject.tearDown();
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
}
