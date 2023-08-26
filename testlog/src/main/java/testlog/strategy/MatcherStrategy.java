package testlog.strategy;

import org.hamcrest.Matcher;
import testlog.LogItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class MatcherStrategy implements AssertionStrategy {
    private final List<Matcher<LogItem>> matchers;

    @SafeVarargs
    public MatcherStrategy(Matcher<LogItem>... matchers) {
        this.matchers = new ArrayList<>(asList(matchers));
    }

    @Override
    public String describeRemainingExpectations() {
        return matchers.stream()
                .map(Objects::toString)
                .collect(joining(", "));
    }

    @Override
    public int getRemainingCount() {
        return matchers.size();
    }

    @Override
    public boolean hasRemainingExpectations() {
        return !matchers.isEmpty();
    }

    @Override
    public boolean matchesNextExpectation(LogItem logItem) {
        return !matchers.isEmpty() && matchesExpectation(logItem, matchers.remove(0));
    }

    @Override
    public void removeLaterExpectationForEfficiency(LogItem logItem) {
        Iterator<Matcher<LogItem>> iterator = matchers.iterator();
        while (iterator.hasNext()) {
            Matcher<LogItem> expected = iterator.next();
            if (matchesExpectation(logItem, expected)) {
                iterator.remove();
                return;
            }
        }
    }

    private boolean matchesExpectation(LogItem logItem, Matcher<LogItem> expected) {
        return expected.matches(logItem);
    }
}
