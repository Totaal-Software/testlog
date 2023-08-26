package testlog;

import org.slf4j.event.Level;

public class LogItem {
    private final Level level;

    private final String message;

    private final Throwable throwable;

    public LogItem(Level level, String message, Throwable throwable) {
        this.level = level;
        this.message = message;
        this.throwable = throwable;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
