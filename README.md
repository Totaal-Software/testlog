# testlog

[![Build Status](https://travis-ci.org/Totaal-Software/testlog.svg?branch=master)](https://travis-ci.org/Totaal-Software/testlog) [![Coverage Status](https://coveralls.io/repos/github/Totaal-Software/testlog/badge.svg?branch=master)](https://coveralls.io/github/Totaal-Software/testlog?branch=master)

Library to interact with logging in JUnit tests. Some main concepts are:

- Muting: make log entirely silent
- Assertions: assert that the log only contains specified messages of a given level

## Requirements

Projects using `testlog` must use:

- Java 8 or higher
- JUnit
- SLF4j logging facade
- One of the supported logging implementations: Log4j, Logback

## Installation

Add the `testlog` JAR file to your test classpath. You can do this as follows, using Maven:

    <dependency>
        <groupId>com.totaalsoftware.testlog</groupId>
        <artifactId>testlog</artifactId>
        <version>...version...</version>
        <scope>test</scope>
    </dependency>

## Usage

Mute logs in all tests:

    import testlog.LogMuter;

    public class MyTest {

        private LogMuter logMuter = LogMuter.setupLogMuter();
    
        @After
        public void tearDown() {
            logMuter.tearDown();
        }

Mute logs in one test:

    public class MyTest {
    
        @Test
        public void testSomething() {
            LogMuter logMuter = LogMuter.setupLogMuter();
        
            ...test something...
        
            logMuter.tearDown();
        }

Or use the `Closeable` implementation:

    public class MyTest {
    
        @Test
        public void testSomething() {
            try(LogMuter logMuter = LogMuter.setupLogMuter()) {

                ...test something...

            }
        }

Assert unexpected `WARN` and `ERROR` logs in all tests:

    import testlog.LogAsserter;

    public class MyTest {

        private LogAsserter logAsserter = LogAsserter.setUpLogAsserter(Level.WARN);    

        @After
        public void tearDown() {
            logAsserter.tearDown();
        }

Allow exceptions in a test, e.g. expecting two `ERROR` logs, then one `WARN` log (in that order):

    public class MyTest {
    
        private LogAsserter logAsserter = LogAsserter.setUpLogAsserter(Level.WARN);    

        @Test
        public void testSomething() {
            subject.expect(Level.ERROR, Level.ERROR, Level.WARN);
        
            ...test something...
        }

## FAQ

- **Why not mute logs through configuration of the log framework (e.g. in `log4j.properties`,
`logback.xml`)?**

    It is often useful to suppress noisy logs while testing (`DEBUG`, `INFO`) but still see `WARN` and `ERRORS`. That as
    a configuration does not help for the occasional ("negative") test, that intends to exercise these exceptional
    code paths on purpose. This library can help with that.
    
- **Why not just catch an exception, rather than assert it in the log?**

    The exception may not be available to catch, for example if it is caught by the production code, or if it is logged
    by a remote or asynchronous operation (like a REST API).   
