package testlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import testlog.impl.Logging;

import static org.junit.Assert.assertEquals;

public class LogbackLogMuterTest extends AbstractLogMuterTest {
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
    public void testDelegate() {
        try (LogMuter subject = LogMuter.setupLogMuter()) {
            Logging actual = subject.getDelegate();
            assertEquals("LogbackLogging", actual.getClass().getSimpleName());
        }
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
            incrementLogCounter();
        }
    }
}
