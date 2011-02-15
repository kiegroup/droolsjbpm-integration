package org.drools.grid;

public interface DaemonService
    extends
    Service {
    void start();

    void stop();

    boolean isRunning();
}
