# testlog

[![Build Status](https://travis-ci.org/Totaal-Software/testlog.svg?branch=master)](https://travis-ci.org/Totaal-Software/testlog)

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
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

## Usage

To do.
