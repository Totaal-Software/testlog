package testlog;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.NullAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testlog.impl.Logging;

import static org.junit.Assert.assertEquals;

public class Log4jLogMuterTest extends AbstractLogMuterTest {
    private Appender testAppender = new AppenderForTest();

    @Before
    public void setUp() {
        org.apache.log4j.Logger.getRootLogger().addAppender(testAppender);
    }

    @After
    public void tearDown() {
        org.apache.log4j.Logger.getRootLogger().removeAppender(testAppender);
    }

    @Test
    public void testDelegate() {
        try (LogMuter subject = LogMuter.setupLogMuter()) {
            Logging actual = subject.getDelegate();
            assertEquals("Log4jLogging", actual.getClass().getSimpleName());
        }
    }

    private class AppenderForTest extends NullAppender {
        @Override
        public void doAppend(LoggingEvent event) {
            incrementLogCounter();
        }
    }
}
