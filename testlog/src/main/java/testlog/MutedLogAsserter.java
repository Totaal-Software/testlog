package testlog;

import org.slf4j.event.Level;

public class MutedLogAsserter extends LogAsserter {
    private LogMuter logMuter;

    @SuppressWarnings("WeakerAccess")
    public MutedLogAsserter(Level minimumLevel) {
        super(minimumLevel);
    }

    public static MutedLogAsserter setupMutedLogAsserter(Level minimumLevel) {
        return new MutedLogAsserter(minimumLevel);
    }

    @Override
    public void close() {
        tearDown();
    }

    /**
     * Tear down the log muter and asserter. Don't forget to tear down, else subsequent tests that are executed will
     * have their log muted too and will assert too, since the logging infrastructure may be static
     *
     * @see LogAsserter#tearDown()
     * @see LogMuter#tearDown()
     */
    @Override
    public void tearDown() {
        try {
            super.tearDown();
        } finally {
            logMuter.tearDown();
        }
    }

    @Override
    protected void initialize() {
        logMuter = LogMuter.setupLogMuter();
        super.initialize();
    }
}
