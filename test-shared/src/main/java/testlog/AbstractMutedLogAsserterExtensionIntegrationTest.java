package testlog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public abstract class AbstractMutedLogAsserterExtensionIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMutedLogAsserterExtensionIntegrationTest.class);

    @RegisterExtension
    public MutedLogAsserterExtension subject = new MutedLogAsserterExtension();

    @Test
    public void test() {
        try (ExpectedLogs ignored = subject.expect(Level.WARN)) {
            logger.warn("expected log");
        }
    }
}
