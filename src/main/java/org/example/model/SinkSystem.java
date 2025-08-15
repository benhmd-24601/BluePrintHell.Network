package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class SinkSystem extends NetworkSystem {
    private final List<Packet> receivedPackets = new ArrayList<>();
    private int sinkCapacity = 10;

    public SinkSystem(double x, double y, int capacity) {
        super(x, y, 1);
        this.sinkCapacity = capacity;
        getOutputPorts().clear(); // sink has no outputs
    }

    @Override
    public void addPacket(Packet packet) {
        if (receivedPackets.size() < sinkCapacity) {
            receivedPackets.add(packet);
        } // else overflow: ignore or count as loss if desired
    }

    @Override
    public void addInputWire(Wire wire) { /* could store if needed */ }

    public List<Packet> getReceivedPackets() { return receivedPackets; }
    public int getTotalReceived() { return receivedPackets.size(); }
    public int getSinkCapacity() { return sinkCapacity; }
}
