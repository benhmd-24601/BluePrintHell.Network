package org.example.model;

import java.util.LinkedList;
import java.util.Queue;

public class PacketQueue {
    private final Queue<Packet> queue = new LinkedList<>();
    private final NetworkSystem system;
    private final String portType;

    public PacketQueue(NetworkSystem system, String portType) {
        this.system = system;
        this.portType = portType;
    }

    public void addPacket(Packet packet) {
        if (packet.getType().equals(portType)) queue.add(packet);
    }

    public boolean removePacket(Packet packet) { return queue.remove(packet); }

    public int size() { return queue.size(); }

    public Packet getNextPacket() { return queue.poll(); }

    public boolean hasPackets() { return !queue.isEmpty(); }
}
