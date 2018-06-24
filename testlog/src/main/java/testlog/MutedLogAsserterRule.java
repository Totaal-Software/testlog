package testlog;

import org.junit.rules.ExternalResource;
import org.slf4j.event.Level;

public class MutedLogAsserterRule extends ExternalResource {
    private Level minimumLevel;

    private MutedLogAsserter mutedLogAsserter;

    /**
     * Constructor. Initialize at default minimum level ({@link Level#WARN})
     */
    @SuppressWarnings("WeakerAccess")
    public MutedLogAsserterRule() {
        minimumLevel = Level.WARN;
    }

    /**
     * Constructor.
     *
     * @param minimumLevel minimum log level to assert on
     */
    public MutedLogAsserterRule(Level minimumLevel) {
        this.minimumLevel = minimumLevel;
    }

    /**
     * Assert the current expectations and reset the expectation. (So that going forward all logs will be asserted
     * again)
     *
     * @see MutedLogAsserter#assertAndReset()
     */
    @SuppressWarnings("WeakerAccess")
    public void assertAndReset() {
        mutedLogAsserter.assertAndReset();
    }

    /**
     * Expect log events of the given levels, in the given order.
     *
     * @param levels log event levels to expect
     * @return a closeable object that can be used to trigger assertion and reset of the expectations upon leaving a
     * {@code try} block
     * @see MutedLogAsserter#expect(Level...)
     */
    @SuppressWarnings("WeakerAccess")
    public ExpectedLogs expect(Level... levels) {
        return mutedLogAsserter.expect(levels);
    }

    @Override
    protected void after() {
        mutedLogAsserter.tearDown();
    }

    @Override
    protected void before() throws Throwable {
        mutedLogAsserter = MutedLogAsserter.setupMutedLogAsserter(minimumLevel);
    }

    MutedLogAsserter getMutedLogAsserter() {
        return mutedLogAsserter;
    }
}
