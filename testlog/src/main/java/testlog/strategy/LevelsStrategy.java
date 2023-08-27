package testlog.strategy;

import org.slf4j.event.Level;
import testlog.LogItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class LevelsStrategy implements AssertionStrategy {
    private final List<Level> expectations = new ArrayList<>();

    public LevelsStrategy(Level... levels) {
        addExpectations(levels);
    }

    public LevelsStrategy addExpectations(Level... levels) {
        expectations.addAll(asList(levels));
        return this;
    }

    @Override
    public String describeRemainingExpectations() {
        return expectations.stream()
                .map(Level::toString)
                .collect(joining(", "));
    }

    @Override
    public int getRemainingCount() {
        return expectations.size();
    }

    @Override
    public boolean hasRemainingExpectations() {
        return !expectations.isEmpty();
    }

    @Override
    public boolean matchesNextExpectation(LogItem logItem) {
        return !expectations.isEmpty() && matchesExpectation(logItem, expectations.remove(0));
    }

    @Override
    public void removeLaterExpectationForEfficiency(LogItem logItem) {
        Iterator<Level> iterator = expectations.iterator();
        while (iterator.hasNext()) {
            Level expected = iterator.next();
            if (matchesExpectation(logItem, expected)) {
                iterator.remove();
                return;
            }
        }
    }

    private boolean matchesExpectation(LogItem logItem, Level expected) {
        return expected.equals(logItem.getLevel());
    }
}
