package testlog;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public abstract class AbstractLogMuterTest {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLogMuterTest.class);

    private int counter = 0;

    @Test
    public void testClose() {
        assertLogIsNotMuted();

        try (LogMuter ignored = LogMuter.setupLogMuter()) {
            assertLogIsMuted();
        }

        assertLogIsNotMuted();
    }

    @Test
    public void testEnableOutput() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        assertLogIsMuted();

        subject.enableOutput();
        assertLogIsNotMuted();

        subject.tearDown();
    }

    @Test
    public void testRedundantMute() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        assertLogIsMuted();

        subject.disableOutput();
        assertLogIsMuted();

        subject.tearDown();
    }

    @Test
    public void testRedundantUnmute() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        subject.enableOutput();
        assertLogIsNotMuted();

        subject.enableOutput();
        assertLogIsNotMuted();

        subject.tearDown();
    }

    @Test
    public void testSetupLogMuter() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();

        assertLogIsMuted();
        subject.tearDown();
    }

    @Test
    public void testTearDown() {
        assertLogIsNotMuted();

        LogMuter subject = LogMuter.setupLogMuter();
        subject.tearDown();

        assertLogIsNotMuted();
    }

    @Test
    public void testUseConstructor() {
        assertLogIsNotMuted();

        LogMuter subject = new LogMuter();
        assertLogIsNotMuted();

        subject.disableOutput();
        assertLogIsMuted();

        subject.tearDown();
    }

    private void assertLogIsMuted() {
        int currentCount = counter;
        logger.info("log statement");
        assertEquals(currentCount, counter);
    }

    private void assertLogIsNotMuted() {
        int currentCount = counter;
        logger.info("log statement");
        assertEquals(currentCount + 1, counter);
    }

    protected void incrementLogCounter() {
        counter++;
    }
}
