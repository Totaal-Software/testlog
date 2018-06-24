package testlog;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public abstract class AbstractMutedLogAsserterRuleIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMutedLogAsserterRuleIntegrationTest.class);

    @Rule
    public MutedLogAsserterRule subject = new MutedLogAsserterRule();

    @Test
    public void test() {
        try (ExpectedLogs ignored = subject.expect(Level.WARN)) {
            logger.warn("expected log");
        }
    }
}
