package testlog;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public abstract class AbstractMutedLogAsserterExtensionTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMutedLogAsserterExtensionTest.class);

    @Test
    public void testAssertAndResetNoExpectedLog() throws Throwable {
        MutedLogAsserterExtension subject = new MutedLogAsserterExtension();
        subject.beforeEach(null);
        subject.expect(Level.WARN);

        boolean fail = false;
        try {
            subject.assertAndReset();
            fail = true;
        } catch (AssertionError exception) {
            assertEquals("1 expected log entries did not occur after waiting 5000ms: [WARN]", exception.getMessage());
        }
        if (fail) {
            fail("expected an exception for the unexpected log");
        }

        subject.afterEach(null);
    }

    @Test
    public void testAssertAndResetExpectedLog() throws Throwable {
        MutedLogAsserterRule subject = new MutedLogAsserterRule();
        subject.before();
        subject.expect(Level.WARN);
        logger.warn("expected log");
        subject.assertAndReset();
        subject.after();
    }

    @Test
    public void testExpectedError() throws Throwable {
        MutedLogAsserterExtension subject = new MutedLogAsserterExtension(/* default constructor assumes WARN */);
        subject.beforeEach(null);

        try (ExpectedLogs ignored = subject.expect(Level.ERROR)) {
            logger.error("unexpected log");
        }

        subject.afterEach(null);
    }

    @Test
    public void testUnexpectedError() throws Throwable {
        MutedLogAsserterExtension subject = new MutedLogAsserterExtension(Level.INFO);
        assertNull(subject.getMutedLogAsserter());
        logger.info("no problem");

        subject.beforeEach(null);
        assertNotNull(subject.getMutedLogAsserter());
        logger.info("unexpected log");

        boolean fail = false;
        try {
            subject.afterEach(null);
            fail = true;
        } catch (AssertionError exception) {
            assertEquals("Unexpected INFO log during test execution: unexpected log", exception.getMessage());
        }
        if (fail) {
            fail("expected an exception for the expected log that did not occur");
        }
    }
}
