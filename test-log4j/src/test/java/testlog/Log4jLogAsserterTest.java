package testlog;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.NullAppender;
import org.junit.Test;
import org.slf4j.event.Level;
import testlog.impl.Logging;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class Log4jLogAsserterTest extends AbstractLogAsserterTest {
    @Test
    public void testDelegate() {
        LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN);
        Logging actual = subject.getDelegate();
        assertEquals("Log4jLogging", actual.getClass().getSimpleName());
    }

    private static org.apache.log4j.Logger getRootLogger() {
        return org.apache.log4j.Logger.getRootLogger();
    }

    @Override
    protected CaptureInfoAppender createAppender() {
        return new Log4jCaptureInfoAppender();
    }

    @Override
    protected void enableTraceLogging() {
        getRootLogger().setLevel(org.apache.log4j.Level.TRACE);
    }

    private static class Log4jCaptureInfoAppender extends NullAppender implements CaptureInfoAppender {
        private List<String> messages = new ArrayList<>();

        @Override
        public void assertMessages(String... expected) {
            assertEquals(asList(expected), messages);
        }

        @Override
        public void doAppend(LoggingEvent eventObject) {
            if (eventObject.getLevel().equals(org.apache.log4j.Level.INFO)) {
                messages.add(eventObject.getRenderedMessage());
            }
        }

        @Override
        public void register() {
            getRootLogger().addAppender(this);
        }

        @Override
        public void unregister() {
            getRootLogger().removeAppender(this);
        }
    }
}
