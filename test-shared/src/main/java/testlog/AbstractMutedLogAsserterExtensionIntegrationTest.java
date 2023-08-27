package testlog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

public abstract class AbstractMutedLogAsserterExtensionIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(
            AbstractMutedLogAsserterExtensionIntegrationTest.class);

    @RegisterExtension
    public MutedLogAsserterExtension subject = new MutedLogAsserterExtension();

    @Test
    public void testExpectByCount() {
        try (ExpectedLogs ignored = subject.expect(2)) {
            logger.error("expected log");
            logger.warn("expected log");
        }
    }

    @Test
    public void testExpectByLevel() {
        try (ExpectedLogs ignored = subject.expect(Level.WARN)) {
            logger.warn("expected log");
        }
    }

    @Test
    public void testExpectByMatcher() {
        try (ExpectedLogs ignored = subject.expect(hasProperty("message", is(equalTo("expected log"))))) {
            logger.warn("expected log");
        }
    }
}
