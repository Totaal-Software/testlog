package testlog.strategy;

import testlog.LogItem;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class CountStrategy implements AssertionStrategy {
    private final AtomicInteger count;

    public CountStrategy(int count) {
        if (count < 1) {
            throw new IllegalArgumentException(format("Expected count should be at least 1, was %d", count));
        }
        this.count = new AtomicInteger(count);
    }

    @Override
    public String describeRemainingExpectations() {
        return format("any %d log items", count.get());
    }

    @Override
    public int getRemainingCount() {
        return count.get();
    }

    @Override
    public boolean hasRemainingExpectations() {
        return count.get() > 0;
    }

    @Override
    public boolean matchesNextExpectation(LogItem logItem) {
        boolean match = hasRemainingExpectations();
        if (match) {
            count.decrementAndGet();
        }
        return match;
    }

    @Override
    public void removeLaterExpectationForEfficiency(LogItem logItem) {
        count.decrementAndGet();
    }
}
