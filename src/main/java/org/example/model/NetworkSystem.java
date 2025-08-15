package org.example.model;

import java.util.*;

public class NetworkSystem {
    private final List<Port> inputPorts = new ArrayList<>();
    private final List<Port> outputPorts = new ArrayList<>();
    private final ArrayList<Packet> packetStorage = new ArrayList<>();
    private final Map<String, PacketQueue> inputQueues = new HashMap<>();
    private final Map<String, List<Wire>> outputWires = new HashMap<>();

    private boolean indicatorOn;
    private int storageCapacity = 5;

    private double x, y;
    private boolean isSourceSystem = false;
    private int type;
    private double packetsDropped;

    private GameEnv env;

    public NetworkSystem(double x, double y, int type) {
        this.type = type;
        double spacing = 120 / 3.0;

        if (type == 1) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + spacing, true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 2) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + spacing - 20, true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing - 20, true));
            inputPorts.add(new Port("square", -1, this, x - 15, y + 3 * spacing - 20, true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 4 * spacing - 20, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 3) {
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing - 20, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 4) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + spacing, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 5) {
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 6) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + 2 * spacing, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
        }

        this.x = x - 6;
        this.y = y - 30;

        this.indicatorOn = false;

        outputWires.put("square", new ArrayList<>());
        outputWires.put("triangle", new ArrayList<>());
    }

    public void setEnv(GameEnv env) { this.env = env; }
    public GameEnv getEnv() { return env; }

    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        if (packetStorage.isEmpty()) return null;

        for (Packet packet : packetStorage) {
            if (packet.getType().equals(startPortType)) {
                packetStorage.remove(packet);
                return packet;
            }
        }
        List<Wire> freeWires = new ArrayList<>();
        for (Wire wire : allWires) if (!wire.isBusy()) freeWires.add(wire);

        if (!freeWires.isEmpty()) {
            for (Packet packet : packetStorage) {
                Wire randomFree = freeWires.get(new Random().nextInt(freeWires.size()));
                if (randomFree.getStartPortType().equals(startPortType)) {
                    packetStorage.remove(packet);
                    return packet;
                }
            }
        }
        return null;
    }

    public List<Wire> getAllConnectedWires() {
        List<Wire> all = new ArrayList<>();
        for (List<Wire> ws : outputWires.values()) all.addAll(ws);
        return all;
    }

    public void addOutputWire(Wire wire) {
        outputWires.get(wire.getStartPortType()).add(wire);
    }

    public void addInputWire(Wire wire) {
        // no-op, override in SinkSystem if needed
    }

    public void removeOutputWire(Wire wire) {
        List<Wire> list = outputWires.get(wire.getStartPortType());
        if (list != null) list.remove(wire);
        checkIndicator();
    }

    public void removeInputWire(Wire wire) { checkIndicator(); }

    public boolean canStorePacket() { return packetStorage.size() < storageCapacity; }

    public void addPacket(Packet packet) {
        if (canStorePacket()) {
            packetStorage.add(packet);
            PacketQueue queue = inputQueues.get(packet.getType());
            if (queue != null) queue.addPacket(packet);
        } else {
            packetsDropped++;
        }
    }

    public double getPacketsDropped() { return packetsDropped; }

    public void removePacket(Packet packet) { packetStorage.remove(packet); }

    public void checkIndicator() {
        boolean allConnected = true;
        for (Port port : inputPorts) {
            if (port.getIsEmpty()) { allConnected = false; break; }
        }
        this.indicatorOn = allConnected;
    }

    public List<Port> getInputPorts() { return inputPorts; }
    public List<Port> getOutputPorts() { return outputPorts; }

    public boolean isIndicatorOn() { return indicatorOn; }
    public void setIndicatorOn(boolean indicatorOn) { this.indicatorOn = indicatorOn; }

    public int getPacketCount() { return packetStorage.size(); }

    public void update() { }

    public boolean isSourceSystem() { return isSourceSystem; }

    public double getX() { return x; }
    public double getY() { return y; }

    public ArrayList<Packet> getPacketStorage() { return new ArrayList<>(packetStorage); }
}
