package testlog;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.event.Level;

public class MutedLogAsserterExtension implements AfterEachCallback, BeforeEachCallback {
    private Level minimumLevel;

    private MutedLogAsserter mutedLogAsserter;

    /**
     * Constructor. Initialize at default minimum level ({@link Level#WARN})
     */
    @SuppressWarnings("WeakerdAccess")
    public MutedLogAsserterExtension() {
        minimumLevel = Level.WARN;
    }

    /**
     * Constructor.
     *
     * @param minimumLevel minimum log level to assert on
     */
    public MutedLogAsserterExtension(Level minimumLevel) {
        this.minimumLevel = minimumLevel;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        mutedLogAsserter.tearDown();
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

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        mutedLogAsserter = MutedLogAsserter.setupMutedLogAsserter(minimumLevel);
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

    MutedLogAsserter getMutedLogAsserter() {
        return mutedLogAsserter;
    }
}
