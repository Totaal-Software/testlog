package testlog.strategy;

import testlog.LogItem;

public interface AssertionStrategy {
    String describeRemainingExpectations();

    int getRemainingCount();

    boolean hasRemainingExpectations();

    boolean matchesNextExpectation(LogItem logItem);

    void removeLaterExpectationForEfficiency(LogItem logItem);
}
