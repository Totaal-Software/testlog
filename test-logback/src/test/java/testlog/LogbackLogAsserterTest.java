package testlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import testlog.impl.Logging;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class LogbackLogAsserterTest extends AbstractLogAsserterTest {
    @Test
    public void testDelegate() {
        try (LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN)) {
            Logging actual = subject.getDelegate();
            assertEquals("LogbackLogging", actual.getClass().getSimpleName());
        }
    }

    private static ch.qos.logback.classic.Logger getRootLogger() {
        return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    }

    @Override
    protected LogAsserter callLogAsserterConstructor(Level level) {
        return new LogAsserter(level);
    }

    @Override
    protected LogAsserter callSubjectSetUpLogAsserter(Level level) {
        return LogAsserter.setUpLogAsserter(level);
    }

    @Override
    protected CaptureInfoAppender createAppender() {
        return new LogbackCaptureInfoAppender();
    }

    @Override
    protected void enableTraceLogging() {
        getRootLogger().setLevel(ch.qos.logback.classic.Level.TRACE);
    }

    @Override
    protected boolean isMuted() {
        return false;
    }

    private static class LogbackCaptureInfoAppender extends AppenderBase<ILoggingEvent> implements CaptureInfoAppender {
        private List<String> messages = new ArrayList<>();

        LogbackCaptureInfoAppender() {
            started = true;
        }

        @Override
        public void assertMessages(String... expected) {
            assertEquals(asList(expected), messages);
        }

        @Override
        public void register() {
            getRootLogger().addAppender(this);
        }

        @Override
        public void unregister() {
            getRootLogger().detachAppender(this);
        }

        @Override
        protected void append(ILoggingEvent eventObject) {
            if (eventObject.getLevel().equals(ch.qos.logback.classic.Level.INFO)) {
                messages.add(eventObject.getMessage());
            }
        }
    }
}
