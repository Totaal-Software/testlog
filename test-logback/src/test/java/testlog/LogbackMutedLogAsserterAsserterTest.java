package testlog;

import org.junit.Test;
import org.slf4j.event.Level;
import testlog.impl.Logging;

import static org.junit.Assert.assertEquals;

public class LogbackMutedLogAsserterAsserterTest extends LogbackLogAsserterTest {
    @Test
    @Override
    public void testDelegate() {
        try (LogAsserter subject = MutedLogAsserter.setUpLogAsserter(Level.WARN)) {
            Logging actual = subject.getDelegate();
            assertEquals("LogbackLogging", actual.getClass().getSimpleName());
        }
    }

    @Override
    protected LogAsserter callLogAsserterConstructor(Level level) {
        return new MutedLogAsserter(level);
    }

    @Override
    protected LogAsserter callSubjectSetUpLogAsserter(Level level) {
        return MutedLogAsserter.setUpLogAsserter(level);
    }
}
