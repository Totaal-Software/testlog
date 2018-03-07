package testlog.impl;

import ch.qos.logback.classic.Level;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LogbackLevelUtilTest {
    @Test
    public void testBecauseWeLoveCoverage() {
        new LogbackLevelUtil();
    }

    @Test
    public void testConvertLevelDebug() {
        assertEquals(org.slf4j.event.Level.DEBUG, LogbackLevelUtil.convertLevel(Level.DEBUG));
    }

    @Test
    public void testConvertLevelError() {
        assertEquals(org.slf4j.event.Level.ERROR, LogbackLevelUtil.convertLevel(Level.ERROR));
    }

    @Test
    public void testConvertLevelInfo() {
        assertEquals(org.slf4j.event.Level.INFO, LogbackLevelUtil.convertLevel(Level.INFO));
    }

    @Test
    public void testConvertLevelTrace() {
        assertEquals(org.slf4j.event.Level.TRACE, LogbackLevelUtil.convertLevel(Level.TRACE));
    }

    @Test
    public void testConvertLevelUnknown() {
        try {
            LogbackLevelUtil.convertLevel(Level.OFF);
            fail("expected an exception for not recognizing the unusual level given");
        } catch (RuntimeException exception) {
            assertEquals("level OFF is not supported", exception.getMessage());
        }
    }

    @Test
    public void testConvertLevelWarn() {
        assertEquals(org.slf4j.event.Level.WARN, LogbackLevelUtil.convertLevel(Level.WARN));
    }
}
