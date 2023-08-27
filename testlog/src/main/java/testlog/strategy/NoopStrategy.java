package testlog.strategy;

import testlog.LogItem;

public class NoopStrategy implements AssertionStrategy {
    @Override
    public String describeRemainingExpectations() {
        throw new IllegalStateException("This should not have been called");
    }

    @Override
    public int getRemainingCount() {
        return 0;
    }

    @Override
    public boolean hasRemainingExpectations() {
        return false;
    }

    @Override
    public boolean matchesNextExpectation(LogItem logItem) {
        return false;
    }

    @Override
    public void removeLaterExpectationForEfficiency(LogItem logItem) {

    }
}
