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

        try {
            subject.assertAndReset();
            fail("expected an exception for the unexpected log");
        } catch (AssertionError exception) {
            assertEquals("1 expected log entries did not occur after waiting 5000ms: WARN", exception.getMessage());
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

        try {
            subject.afterEach(null);
            fail("expected an exception for the expected log that did not occur");
        } catch (AssertionError exception) {
            String actual = exception.getMessage();
            assertEquals("Unexpected INFO log during test execution with the following message: unexpected log\n"
                    + "History:\n"
                    + " (1) INFO: unexpected log\n"
                    + " (1) -- this is the one that caused the log asserter to fail --\n"
                    + "(now follows once more the stacktrace for the log item that caused this)", actual);
        }
    }
}
