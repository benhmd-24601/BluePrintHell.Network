package org.example.model;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class Wire {
    private final NetworkSystem startSystem;
    private final NetworkSystem endSystem;
    private final String startPortType;
    private final String endPortType;
    private final double length;
    private Packet currentPacket;
    private final double Sx, Sy, Ex, Ey;
    private final Port startPort;
    private final Port endPort;
    private final List<Point> points;
    private final GameEnv env;

    public Wire(Port startPort, Port endPort,
                NetworkSystem startSystem, String startPortType,
                NetworkSystem endSystem, String endPortType,
                double length, double Sx, double Sy, double Ex, double Ey,
                List<Point> points, GameEnv env) {

        this.startPort = startPort;
        this.endPort = endPort;
        this.startSystem = startSystem;
        this.startPortType = startPortType;
        this.endSystem = endSystem;
        this.endPortType = endPortType;
        this.length = length;
        this.currentPacket = null;
        this.Sx = Sx ;
        this.Sy = Sy ;
        this.Ex = Ex;
        this.Ey = Ey ;
        this.points = points;
        this.env = env;
    }

    public void update() {
        if (currentPacket != null) {
            if (currentPacket.reachedDestination()) currentPacket = null;
        } else {
            if (!isBusy()) {
                if (startSystem != null) {
                    Packet packet = startSystem.getNextPacketForWire(startPortType, startSystem.getAllConnectedWires());
                    if (packet != null) {
                        currentPacket = packet;
                        packet.setWire(this);
                        env.getPackets().add(currentPacket);
                        if (!packet.getType().equals(startPortType)) {
                            packet.setSpeed(packet.getSpeed() * 0.7);
                        }
                    }
                }
            }
        }
    }

    public void setCurrentPacket(Packet p) {
        this.currentPacket = p;
        if (p != null) p.setWire(this);
    }

    public void deliverCurrentPacket() {
        if (currentPacket != null) {
            if (Objects.equals(this.getCurrentPacket().getType(), "square")) env.setCoins(env.getCoins() + 1);
            else if (Objects.equals(this.getCurrentPacket().getType(), "triangle")) env.setCoins(env.getCoins() + 2);

            endSystem.addPacket(currentPacket);
            startSystem.removePacket(currentPacket);
            currentPacket = null;
        }
    }

    public boolean canAcceptPacket(Packet packet) {
        return packet.getType().equals(this.getStartPortType());
    }

    public Port getStartPort() { return startPort; }
    public Port getEndPort() { return endPort; }
    public boolean isBusy() { return currentPacket != null; }
    public Packet getCurrentPacket() { return currentPacket; }

    public NetworkSystem getStartSystem() { return startSystem; }
    public NetworkSystem getEndSystem() { return endSystem; }

    public double getLength() { return length; }
    public double getEndX() { return Ex; }
    public double getEndY() { return Ey; }
    public double getStartx() { return Sx; }
    public double getStarty() { return Sy; }

    public String getStartPortType() { return startPortType; }
    public String getEndPortType() { return endPortType; }
    public List<Point>  getPoints() { return points; }
}
