package testlog;

import java.io.Closeable;

public interface ExpectedLogs extends Closeable {
    @Override
    void close();
}
