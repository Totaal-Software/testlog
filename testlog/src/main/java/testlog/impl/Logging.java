package testlog.impl;

public interface Logging {
    void deregisterCallback(LogCallback logCallback);

    void mute();

    void registerCallback(LogCallback logCallback);

    void unmute();
}
