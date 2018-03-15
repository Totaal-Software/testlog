package testlog;

import org.junit.Test;
import org.slf4j.event.Level;
import testlog.impl.Logging;

import static org.junit.Assert.assertEquals;

public class Log4jMutedLogAsserterAsserterTest extends Log4jLogAsserterTest {
    @Test
    @Override
    public void testDelegate() {
        try (LogAsserter subject = LogAsserter.setUpLogAsserter(Level.WARN)) {
            Logging actual = subject.getDelegate();
            assertEquals("Log4jLogging", actual.getClass().getSimpleName());
        }
    }

    @Override
    protected LogAsserter callLogAsserterConstructor(Level level) {
        return new MutedLogAsserter(level);
    }

    @Override
    protected LogAsserter callSubjectSetUpLogAsserter(Level level) {
        return MutedLogAsserter.setupMutedLogAsserter(level);
    }

    @Override
    protected boolean isMuted() {
        return true;
    }
}
