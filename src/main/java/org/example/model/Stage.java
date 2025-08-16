package org.example.model;

import org.example.model.Systems.NetworkSystem;

import java.util.List;

public class Stage {
    private final List<NetworkSystem> systems;
    private final double initialWireLength;
    private final int timeLimit;
    private final double lossThreshold;

    public Stage(List<NetworkSystem> systems, double initialWireLength, int timeLimit, double lossThreshold) {
        this.systems = systems;
        this.initialWireLength = initialWireLength;
        this.timeLimit = timeLimit;
        this.lossThreshold = lossThreshold;
    }

    public List<NetworkSystem> getSystems() { return systems; }
    public double getInitialWireLength() { return initialWireLength; }
    public int getTimeLimit() { return timeLimit; }
    public double getLossThreshold() { return lossThreshold; }
}
