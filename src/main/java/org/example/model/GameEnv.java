package org.example.model;

import javax.swing.*;
import java.util.*;

public class GameEnv {

    private final int levelNumber;

    private final List<NetworkSystem> systems = new ArrayList<>();
    private final List<Wire> wires = new ArrayList<>();
    private final List<Packet> packets = new ArrayList<>();

    private int coins;
    private double packetLoss; // count (not percent)
    private double temporalProgress; // 0..100
    private double remainingWireLength;
    private double initialWireLength;
    private int totalPacketsGenerated;

    // Store
    private boolean hasAtar = false;
    private boolean hasAiryaman = false;
    private boolean hasAnahita = false;
    private boolean activeAtar, activeAiryaman, activeAnahita;
    private double atarTimer = 0;
    private double airyTimer = 0;

    private Runnable onGameOver;
    private boolean gameOverFired = false;

    public GameEnv(int levelNumber) {
        this.levelNumber = levelNumber;
        coins = 100;
        packetLoss = 0;
        temporalProgress = 0;
    }

    // region Abilities
    public boolean hasAtar() { return hasAtar; }
    public boolean hasAiryaman() { return hasAiryaman; }
    public boolean hasAnahita() { return hasAnahita; }
    public boolean isActiveAtar() { return activeAtar; }
    public boolean isActiveAiryaman() { return activeAiryaman; }
    public boolean isActiveAnahita() { return activeAnahita; }

    public boolean canBuyAtar() { return !hasAtar && coins >= 3; }
    public boolean canBuyAiryaman() { return !hasAiryaman && coins >= 4; }
    public boolean canBuyAnahita() { return !hasAnahita && coins >= 5; }

    public void buyAtar() { if (canBuyAtar()) { hasAtar = true; coins -= 3; } }
    public void buyAiryaman() { if (canBuyAiryaman()) { hasAiryaman = true; coins -= 4; } }
    public void buyAnahita() { if (canBuyAnahita()) { hasAnahita = true; coins -= 5; } }

    public void setActiveAtar(boolean a) { activeAtar = a; }
    public void setActiveAiryaman(boolean a) { activeAiryaman = a; }
    public void setActiveAnahita(boolean a) { activeAnahita = a; }

    public void activateAtar() { if (hasAtar) { activeAtar = true; atarTimer = 10.0; } }
    public void activateAiryaman() { if (hasAiryaman) { activeAiryaman = true; airyTimer = 5.0; } }
    // endregion

    // region Accessors
    public List<NetworkSystem> getSystems() { return systems; }
    public List<Wire> getWires() { return wires; }
    public List<Packet> getPackets() { return packets; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public void addCoins(int amount) { coins += amount; }

    public double getPacketLoss() { return packetLoss; } // count
    public void increasePacketLoss(double value) { packetLoss += value; }

    public double getTemporalProgress() { return temporalProgress; }
    public void setTemporalProgress(double progress) { this.temporalProgress = progress; }

    public double getRemainingWireLength() { return remainingWireLength; }
    public void setRemainingWireLength(double length) { this.remainingWireLength = length; }

    public double getInitialWireLength() { return initialWireLength; }

    public int getTotalPacketsGenerated() { return totalPacketsGenerated; }
    public void incrementTotalPacketCount() { totalPacketsGenerated++; }
    // endregion

    // region Build
    public void applyStage(Stage stage) {
        systems.clear();
        wires.clear();
        packets.clear();

        remainingWireLength = stage.getInitialWireLength();
        temporalProgress = 0;
        packetLoss = 0;
//        coins = 0;

        for (NetworkSystem sys : stage.getSystems()) {
            sys.setEnv(this);          // تزریق env به سیستم‌ها
            systems.add(sys);
            if (sys instanceof SourceSystem src) {
                src.setEnv(this);
            }
        }
        initialWireLength = stage.getInitialWireLength();
        updateTotalPacketsFromSources();
    }
    // endregion

    // region Game loop
    private long lastPowerUpdateTime;

    public void update(double delta) {
        long currentTime = System.currentTimeMillis();
        if (lastPowerUpdateTime == 0) lastPowerUpdateTime = currentTime;
        long elapsed = currentTime - lastPowerUpdateTime;

        if (activeAtar) {
            atarTimer -= elapsed / 1000.0;
            if (atarTimer <= 0) { activeAtar = false; atarTimer = 0; }
        }
        if (activeAiryaman) {
            airyTimer -= elapsed / 1000.0;
            if (airyTimer <= 0) { activeAiryaman = false; airyTimer = 0; }
        }
        lastPowerUpdateTime = currentTime;

        generate(delta);
        processWires(delta);
        cleanup();

        if (!gameOverFired && isGameOver()) {
            gameOverFired = true;
            if (onGameOver != null) SwingUtilities.invokeLater(onGameOver);
            return;
        }
    }
    // endregion

    public void setOnGameOver(Runnable onGameOver) { this.onGameOver = onGameOver; }

    public boolean isGameOver() {
        int allpackets = 0;
        double droppedPackets = 0;
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem source) {
                allpackets = source.getAllpackets();
            }
            droppedPackets += system.getPacketsDropped();
        }
        if (allpackets <= 0) return false;
        double lossPercent = (packetLoss + droppedPackets) / allpackets * 100.0;
        return lossPercent >= 50.0;
    }

    public double getTotalPacketLossPercent() {
        int allpackets = 0;
        double droppedPackets = 0;
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem source) {
                allpackets = source.getAllpackets();
            }
            droppedPackets += system.getPacketsDropped();
        }
        if (allpackets <= 0) return 0;
        return (packetLoss + droppedPackets) / allpackets * 100.0;
    }

    private boolean gameisWon() {
        double allPackets = 0;
        double reached = 0;
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem) allPackets = ((SourceSystem) system).getAllpackets();
            if (system instanceof SinkSystem) reached = ((SinkSystem) system).getTotalReceived();
        }
        return allPackets > 0 && (reached / allPackets >= 0.5);
    }

    // region Packets
    private int nextPacketId = 0;

    public void generate(double delta) {
        List<Packet> newPackets = new ArrayList<>();
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem src && src.isGenerating()) {
                newPackets.addAll(src.updateAndGeneratePackets());
            }
        }
        for (Packet p : newPackets) p.setId(nextPacketId++);
        packets.addAll(newPackets);
    }

    public void processWires(double delta) {
        for (Wire wire : wires) {
            wire.update();
            if (wire.getCurrentPacket() != null) {
                wire.getCurrentPacket().updatePosition(delta);
            }
        }
    }

    public void cleanup() {
        // remove delivered
        packets.removeIf(packet -> {
            if (packet.reachedDestination()) {
                if (packet.getWire() != null && packet.getWire().getEndSystem() instanceof SinkSystem) {
                    // reached sink
                }
                return true;
            }
            return false;
        });

        // collisions
        for (int i = 0; i < packets.size(); i++) {
            Packet p1 = packets.get(i);
            for (int j = i + 1; j < packets.size(); j++) {
                Packet p2 = packets.get(j);
                if (p1.getWire() != p2.getWire() && areColliding(p1, p2)) {
                    if (!activeAiryaman) {
                        p1.onCollision(p2);
                    }
                    if (!activeAtar) {
                        triggerImpact(p1.getX(), p1.getY(), p1);
                    }
                }
            }
        }
    }

    private boolean areColliding(Packet p1, Packet p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.hypot(dx, dy) < 10;
    }

    public void triggerImpact(double cx, double cy, Packet source) {
        double maxRadius = 60;
        double baseStrength = 5;
        List<Packet> toRemove = new ArrayList<>();

        for (Packet p : new ArrayList<>(packets)) {
            double dx = p.getX() - cx;
            double dy = p.getY() - cy;
            double dist = Math.hypot(dx, dy);

            if (p.getNoiseLevel() > p.getSize()) {
                toRemove.add(p);
                increasePacketLoss(1);
            }
            if (dist < maxRadius && dist > 0) {
                if (!source.getCurrentImpact().contains(p)) {
                    source.getCurrentImpact().add(p);
                    double factor = 1 - (dist / maxRadius);
                    double impact = baseStrength * factor;
                    double ux = dx / dist, uy = dy / dist;
                    p.applyImpact(ux * impact, uy * impact);
                }
                if (p.isCompletelyOffWire()) toRemove.add(p);
            }
        }
        for (Packet p : toRemove) markAsLost(p);
    }

    public void markAsLost(Packet p) {
        if (p == null) return;
        Wire w = p.getWire();
        if (w != null && w.getCurrentPacket() == p) w.setCurrentPacket(null);
        packets.remove(p);
        packetLoss++;
        if (!gameOverFired && isGameOver()) {
            gameOverFired = true;
            if (onGameOver != null) SwingUtilities.invokeLater(onGameOver);
        }
    }
    // endregion

    public void addWire(Wire wire) { wires.add(wire); }
    public void removeWire(Wire wire) {
        if (wire == null) return;
        wires.remove(wire);
        if (wire.getStartSystem() != null) wire.getStartSystem().removeOutputWire(wire);
        if (wire.getEndSystem() != null) wire.getEndSystem().removeInputWire(wire);
    }

    public Wire findWireByStartPort(Port p) {
        for (Wire w : wires) if (w.getStartPort() == p) return w;
        return null;
    }

    public void updateTotalPacketsFromSources() {
        int total = 0;
        for (NetworkSystem system : systems)
            if (system instanceof SourceSystem src) total = src.getPacketCount();
        totalPacketsGenerated = total;
    }

    public void resetGameState() {
        packets.clear();
        for (Wire w : wires) w.setCurrentPacket(null);
        for (NetworkSystem sys : systems) {
            if (sys instanceof SourceSystem src) src.reset();
            else sys.getPacketStorage().clear();
        }
    }

    public void simulateFastForward(double targetPercent) {
        double targetProgress = targetPercent * 100;
        if (targetProgress == temporalProgress) return;
        if (targetProgress < temporalProgress) {
            resetGameState();
            temporalProgress = 0;
        }
        int frames = (int)((targetProgress - temporalProgress) / 0.5);
        for (int i = 0; i < frames; i++) update(0.016);
        temporalProgress = targetProgress;
    }

    public void applyEffect(String effect) {
        switch (effect) {
            case "Anahita" -> {
                for (NetworkSystem system : systems) {
                    if (system instanceof SourceSystem src) {
                        for (Packet p : src.getAllPackets()) p.setNoiseLevel(0);
                    }
                }
            }
            case "Airyaman" -> setActiveAiryaman(true);
            case "Atar" -> setActiveAtar(true);
        }
    }
}
