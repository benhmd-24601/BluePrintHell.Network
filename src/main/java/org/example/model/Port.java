package org.example.model;

import java.awt.*;
import java.util.List;

public class Port {
    private final int clickRadius;
    private final String type; // "square" | "triangle"
    private final int side;    // -1 input, 1 output
    private final NetworkSystem parentSystem;
    private double x, y;
    private boolean isEmpty;

    public Port(String type, int side, NetworkSystem parentSystem, double x, double y, boolean isEmpty) {
        this.type = type;
        this.side = side;
        this.parentSystem = parentSystem;
        this.x = x - 6;
        this.y = y ;
        this.isEmpty = isEmpty;
        this.clickRadius = 15;
    }

    public String getType() { return type; }
    public int getSide() { return side; }
    public NetworkSystem getSystem() { return parentSystem; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean getIsEmpty() { return isEmpty; }
    public void setEmpty(boolean empty) { isEmpty = empty; }

    public boolean contains(Point p) {
        Rectangle bounds = new Rectangle((int) (x), (int) (y), clickRadius, clickRadius);
        return bounds.contains(p);
    }

    public double distanceTo(Port other, GameEnv env) {
        // اگر سیم فعالی به این پورت وصل نیست، فاصله خط مستقیم
        Wire wire = env.findWireByStartPort(this);
        if (wire == null || wire.getPoints().size() < 3) {
            double dx = this.x - other.getX();
            double dy = this.y - other.getY();
            return Math.hypot(dx, dy);
        }

        // طول تقریبی منحنی‌های موجود
        List<Point> points = wire.getPoints();
        double total = 0.0;
        for (int i = 0; i < points.size() - 2; i += 2) {
            Point p0 = points.get(i);
            Point p1 = points.get(i + 1);
            Point p2 = points.get(i + 2);
            total += approximateQuadraticCurveLength(p0, p1, p2, 20);
        }
        return total;
    }

    private double approximateQuadraticCurveLength(Point p0, Point p1, Point p2, int samples) {
        double length = 0.0;
        double prevX = p0.getX(), prevY = p0.getY();

        for (int i = 1; i <= samples; i++) {
            double t = i / (double) samples;
            double x = (1 - t) * (1 - t) * p0.getX() + 2 * (1 - t) * t * p1.getX() + t * t * p2.getX();
            double y = (1 - t) * (1 - t) * p0.getY() + 2 * (1 - t) * t * p1.getY() + t * t * p2.getY();
            length += Math.hypot(x - prevX, y - prevY);
            prevX = x; prevY = y;
        }
        return length;
    }
}
