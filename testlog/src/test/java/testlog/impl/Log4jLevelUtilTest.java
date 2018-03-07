package testlog.impl;

import org.apache.log4j.Level;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Log4jLevelUtilTest {
    @Test
    public void testBecauseWeLoveCoverage() {
        new Log4jLevelUtil();
    }

    @Test
    public void testConvertLevelDebug() {
        assertEquals(org.slf4j.event.Level.DEBUG, Log4jLevelUtil.convertLevel(Level.DEBUG));
    }

    @Test
    public void testConvertLevelError() {
        assertEquals(org.slf4j.event.Level.ERROR, Log4jLevelUtil.convertLevel(Level.ERROR));
    }

    @Test
    public void testConvertLevelInfo() {
        assertEquals(org.slf4j.event.Level.INFO, Log4jLevelUtil.convertLevel(Level.INFO));
    }

    @Test
    public void testConvertLevelTrace() {
        assertEquals(org.slf4j.event.Level.TRACE, Log4jLevelUtil.convertLevel(Level.TRACE));
    }

    @Test
    public void testConvertLevelUnknown() {
        try {
            Log4jLevelUtil.convertLevel(Level.OFF);
            fail("expected an exception for not recognizing the unusual level given");
        } catch (RuntimeException exception) {
            assertEquals("level OFF is not supported", exception.getMessage());
        }
    }

    @Test
    public void testConvertLevelWarn() {
        assertEquals(org.slf4j.event.Level.WARN, Log4jLevelUtil.convertLevel(Level.WARN));
    }


}
