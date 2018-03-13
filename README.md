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

            } // tears down the muter here
        }

Assert unexpected `WARN` and `ERROR` logs in all tests:

    import testlog.LogAsserter;

    public class MyTest {

        private LogAsserter logAsserter = LogAsserter.setUpLogAsserter(Level.WARN);    

        @After
        public void tearDown() {
            logAsserter.tearDown();
        }

Mute logs and assert unexpected `WARN` and `ERROR` logs 

    import testlog.MutedLogAsserter;

    public class MyTest {
    
        private MutedLogAsserter mutedLogAsserter = MutedLogAsserter.setupMutedLogAsserter(Level.WARN);    

        @Test
        public void testSomething() {
            mutedLogAsserter.expect(Level.ERROR, Level.ERROR, Level.WARN);
        
            ...test something... expectations are observed...
            
            mutedLogAsserter.assertAndReset();
            
            ...test some more... no more expectations...
        }
    
        @After
        public void tearDown() {
            mutedLogAsserter.tearDown();
        }

Allow exceptions in a test, e.g. expecting two `ERROR` logs, then one `WARN` log (in that order), this works for both
`LogAsserter` and `MutedLogAsserter`:

    public class MyTest {
    
        private LogAsserter logAsserter = LogAsserter.setUpLogAsserter(Level.WARN);    

        @Test
        public void testSomething() {
            logAsserter.expect(Level.ERROR, Level.ERROR, Level.WARN);
        
            ...test something... expectations are observed...
            
            logAsserter.assertAndReset();
            
            ...test some more... no more expectations...
        }
    
        @After
        public void tearDown() {
            logAsserter.tearDown();
        }
        
Or use the `Closeable` implementation, this works for both `LogAsserter` and `MutedLogAsserter`:

    public class MyTest {
    
        private LogAsserter logAsserter = LogAsserter.setUpLogAsserter(Level.WARN);    

        @Test
        public void testSomething() {
            try (ExpectedLogs ignored = logAsserter.expect(Level.ERROR, Level.ERROR, Level.WARN)) {
        
                ...test something... expectations are observed...

            } // asserts and resets here
            
            ...test some more... no more expectations...
        }
    
        @After
        public void tearDown() {
            logAsserter.tearDown();
        }



## FAQ

- **Why not mute logs through configuration of the log framework (e.g. in `log4j.properties`,
`logback.xml`)?**

    It is often useful to suppress noisy logs while testing (`DEBUG`, `INFO`) but still see `WARN` and `ERRORS`. That as
    a configuration does not help for the occasional ("negative") test, that intends to exercise these exceptional
    code paths on purpose. This library can help with that. So that warnings and errors are asserted, or muted when
    specific occurrences of warnings and errors are expected specifically.
    
- **Why not just catch an exception, rather than assert it in the log?**

    The exception may not be available to catch, for example if it is caught by the production code, or if it is logged
    by a remote or asynchronous operation (like a REST API).

- **What is your preferred use of this library?**

    Add the `MutedLogAsserter` on the class level (in a base class that's commonly reused for integration tests, if you
    have such base class). It will assert all warnings and errors. Then surgically wrap just the lines (not even whole
    tests) that are expected to cause specific warnings or errors with a `try(ExpectedLogs ignored =
    mutedLogAsserter.expect(...)) { ...those lines ...}`. That way new warnings or errors of the production code prompt
    thinking about whether these are desired. In this setup these will be asserted by default.

- **Why is there `LogAsserter` and `LogMuter`, while there is also the combined `MutedLogAsserter`?**

    The `MutedLogAsserter` is a progression of the former two. The former two are still the primitives for the
    `MutedLogAsserter`, and they are still sometimes useful by themselves. For instance, the `LogMuter` has been useful
    by itself in cases where a disastrous error scenario is tested, and the exact order and occurrence of expected
    exceptions is not predictable and also not interesting. 

- **What prompted this library?**

    Having used different iterations of the asserter and the muter at subsequent jobs, and specifically seeing these
    classes being copied into different projects during the same job, inspired releasing it as a library, Java's vehicle
    for reuse.
    
- **So is this code copyrighted by your previous employer?**

    No, the first version of these classes were written as part of a self-owned, personal project, before getting
    sprinkled "around town".
