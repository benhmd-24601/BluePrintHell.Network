package org.example.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Packet {

    private double wireX, wireY;
    private double offsetX = 0, offsetY = 0;
    private static final double OFF_WIRE_THRESHOLD = 7.0;

    private String type;
    private double speed;
    private double noiseLevel;
    private Wire wire;
    private double progress = 0;

    private final List<Packet> currentCollisions = new ArrayList<>();
    private final List<Packet> currentImpact = new ArrayList<>();

    private int id;

    public Packet(String type, double x, double y) {
        this.type = type;
        this.wireX = x;
        this.wireY = y;
        this.speed = 1.5;
        this.noiseLevel = 0;
    }

    public void setId(int id) { this.id = id; }
    public int getId() { return id; }

    public void setWire(Wire wire) { this.wire = wire; this.progress = 0; }
    public Wire getWire() { return wire; }

    public boolean isCompletelyOffWire() {
        return Math.hypot(offsetX, offsetY) > OFF_WIRE_THRESHOLD;
    }

    public void setSpeed(double newSpeed) { this.speed = Math.max(0.2, newSpeed); }
    public double getSpeed() { return speed; }

    public double getSize() {
        if (Objects.equals(this.getType(), "square")) return 2;
        else if (Objects.equals(this.getType(), "triangle")) return 3;
        return 0;
    }

    public void applyImpact(double dx, double dy) {
        this.offsetX += dx;
        this.offsetY += dy;
    }

    public List<Packet> getCurrentImpact() { return currentImpact; }

    public void updatePosition(double delta) {
        if (wire == null) return;

        double totalLen = wire.getLength();
        progress = Math.min(progress + speed * delta, totalLen);

        java.util.List<Point> pts = wire.getPoints();
        if (pts.size() >= 3) {
            int curves = (pts.size() - 1) / 2;
            double segLen = totalLen / curves;
            int idx = Math.min((int) (progress / segLen), curves - 1);
            double t = (progress - idx * segLen) / segLen;
            Point p0 = pts.get(idx * 2);
            Point p1 = pts.get(idx * 2 + 1);
            Point p2 = pts.get(idx * 2 + 2);
            double u = 1 - t;

            double baseX = u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x;
            double baseY = u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y;

            this.wireX = baseX - 6;
            this.wireY = baseY - 30;
        }

        if (progress >= totalLen) {
            wire.deliverCurrentPacket();
        }
    }

    public boolean reachedDestination() { return wire != null && progress >= wire.getLength(); }

    public void onCollision(Packet other) {
        if (!currentCollisions.contains(other)) {
            this.noiseLevel += 1;
            other.noiseLevel += 1;
            this.speed *= 0.7;
            currentCollisions.add(other);
        }
    }

    public String getType() { return type; }
    public double getX() { return wireX + offsetX; }
    public double getY() { return wireY + offsetY; }
    public double getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(double v) { noiseLevel = v; }

    public double getProgress() { return progress; }
}
