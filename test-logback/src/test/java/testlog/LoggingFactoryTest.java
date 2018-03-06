package testlog;

import org.junit.Test;
import testlog.impl.Logging;
import testlog.impl.LoggingFactory;

import static org.junit.Assert.assertEquals;

public class LoggingFactoryTest {
    @Test
    public void testGetLogging() {
        Logging actual = LoggingFactory.getLogging();
        assertEquals("LogbackLogging", actual.getClass().getSimpleName());
    }
}
