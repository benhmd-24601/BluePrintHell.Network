package org.example.controller;

import org.example.model.*;
import org.example.model.Systems.NetworkSystem;
import org.example.util.Debug;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class MouseHandler extends MouseAdapter {
    private final GameEnv env;
    private final Runnable repaintCallback;

    private Port startPort;
    private Point currentMouse;

    // برای گزارش hover فقط وقتی تغییر می‌کند
    private Port lastHoverPort = null;

    private NetworkSystem draggingSystem = null;
    private int dragOffsetX = 0, dragOffsetY = 0;

    // === خم‌کردن سیم (Multi-Anchor)
    private Wire bendingWire = null;          // سیمی که در حال درگِ نودش هستیم
    private int  bendingAnchorIdx = -1;       // اندیس نودی که درگ می‌شود
    private static final int HANDLE_HIT_R = 10; // شعاع هیت‌تست برای نود
    private static final int PATH_HIT_DIST = 6; // تلورانس نزدیکی کلیک به مسیر سیم

    private NetworkSystem findSystemAt(Point p) {
        for (NetworkSystem s : env.getSystems()) {
            int x = (int) s.getX(), y = (int) s.getY();
            if (new Rectangle(x, y, 120, 160).contains(p)) return s;
        }
        return null;
    }

    public MouseHandler(GameEnv env, Runnable repaintCallback) {
        this.env = env;
        this.repaintCallback = repaintCallback;
    }

    private String portDesc(Port p) {
        String side = p.getSide() == 1 ? "OUT" : "IN";
        return side + " " + p.getType() + " port@(" + (int) p.getX() + "," + (int) p.getY() +
                ") of sys@(" + (int) p.getSystem().getX() + "," + (int) p.getSystem().getY() + ")";
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        currentMouse = e.getPoint();

        if (Debug.throttle("mouseMove", 30)) {
            Debug.log("[MOUSE]", "move (" + e.getX() + "," + e.getY() + ")");
        }

        Port hover = findPortAt(e.getPoint());
        if (hover != lastHoverPort) {
            if (hover != null) Debug.log("[HOVER]", "over " + portDesc(hover));
            else Debug.log("[HOVER]", "left port");
            lastHoverPort = hover;
        }

        // --- PREVIEW: وقتی در حال کشیدن سیم هستیم، هر موو پریویو را آپدیت کن
        if (startPort != null) {
            Port candidateEnd = tryFindCompatibleEmptyEndPort(e.getPoint(), startPort);
            env.setWirePreview(startPort, e.getPoint(), candidateEnd);
        } else {
            env.clearWirePreview();
        }

        repaintCallback.run();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentMouse = e.getPoint();

        // === اگر در حال درگ یک نود هستیم: فقط همان نود را جابجا کن (نود جدید نساز!)
        if (bendingWire != null && bendingAnchorIdx >= 0) {
            bendingWire.setAnchor(bendingAnchorIdx, e.getX(), e.getY());
            env.recalcWireBudget(); // اگر طول تغییر کند
            repaintCallback.run();
            return;
        }

        // درگ سیستم؟
        if (draggingSystem != null) {
            int nx = e.getX() - dragOffsetX;
            int ny = e.getY() - dragOffsetY;
            env.moveSystem(draggingSystem, nx, ny);
            repaintCallback.run();
            return;
        }

        // در حال کشیدن سیم → پریویو
        if (startPort != null) {
            Port candidateEnd = tryFindCompatibleEmptyEndPort(e.getPoint(), startPort);
            env.setWirePreview(startPort, e.getPoint(), candidateEnd);
        }

        if (Debug.throttle("mouseDrag", 30)) {
            Debug.log("[MOUSE]", "drag (" + e.getX() + "," + e.getY() + ")");
        }
        if (startPort != null) repaintCallback.run();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Port clickedPort = findPortAt(e.getPoint());
        if (clickedPort != null) Debug.log("[CLICK]", "on " + portDesc(clickedPort));
        else Debug.log("[CLICK]", "canvas (" + e.getX() + "," + e.getY() + ")");

        // --- Timeline
        int barWidth = 300, barHeight = 20;
        int barX = 1000 - barWidth - 40;
        int barY = 700 - barHeight - 40;
        int x = e.getX(), y = e.getY();
        if (x >= barX && x <= barX + barWidth && y >= barY && y <= barY + barHeight) {
            double clickPercent = (x - barX) / (double) barWidth;
            env.simulateFastForward(clickPercent);
            repaintCallback.run();
            return;
        }

        // --- حذف سریع وایر
        if (clickedPort != null && clickedPort.getSide() == 1) {
            Wire wire = env.findWireByStartPort(clickedPort);
            if (wire != null) {
                wire.getStartPort().setEmpty(true);
                wire.getEndPort().setEmpty(true);
                wire.getStartSystem().removeOutputWire(wire);
                env.removeWire(wire);
                env.setRemainingWireLength(env.getRemainingWireLength() + wire.getLength());
                repaintCallback.run();
                return;
            }
        }

        // --- شروع کشیدن سیم
        if (clickedPort != null) {
            startPort = clickedPort;
            Port candidateEnd = tryFindCompatibleEmptyEndPort(e.getPoint(), startPort);
            env.setWirePreview(startPort, e.getPoint(), candidateEnd);
            repaintCallback.run();
            return;
        }

        // === خم‌کردن سیم
        // 1) اول روی نودهای موجود هیت‌تست کن؛ اگر روی نودی کلیک شد، همان را برای drag انتخاب کن
        for (Wire w : env.getWires()) {
            int n = w.getAnchorCount();
            for (int i = 0; i < n; i++) {
                var a = w.getAnchor(i);
                if (dist(e.getX(), e.getY(), a.x, a.y) <= HANDLE_HIT_R) {
                    bendingWire = w;
                    bendingAnchorIdx = i;
                    repaintCallback.run();
                    return;
                }
            }
        }
        // 2) اگر روی هیچ نودی نبودیم ولی نزدیک مسیر سیم بودیم → (فقط در صورت مجاز بودن) نود جدید بساز
        for (Wire w : env.getWires()) {
            if (isNearWirePath(w, e.getPoint())) {
                int idx = w.addAnchorAtNearest(e.getX(), e.getY()); // باید max 3 را رعایت کند
                if (idx >= 0) {
                    bendingWire = w;
                    bendingAnchorIdx = idx;
                    repaintCallback.run();
                    return;
                }
            }
        }

        // --- دکمه on/off
        for (NetworkSystem s : env.getSystems()) {
            if (toggleRectFor(s).contains(e.getPoint())) {
                boolean nowEnabled = !s.isEnabled();
                s.setEnabled(nowEnabled);
                try {
                    s.getClass().getMethod("setReenableTimerSec", double.class)
                            .invoke(s, nowEnabled ? 0.0 : 5.0);
                } catch (Exception ignore) { }
                repaintCallback.run();
                return;
            }
        }

        // --- درگ سیستم
        NetworkSystem sys = findSystemAt(e.getPoint());
        if (sys != null) {
            draggingSystem = sys;
            dragOffsetX = e.getX() - (int) sys.getX();
            dragOffsetY = e.getY() - (int) sys.getY();
            return;
        }
    }

    private Rectangle toggleRectFor(NetworkSystem s) {
        int boxW = 120, indR = 12, indM = 5;
        int size = 14, gapY = 4, shiftX = 4;
        int x = (int) s.getX() + boxW - size - indM - shiftX;
        int y = (int) s.getY() + indM + indR + gapY;
        return new Rectangle(x, y, size, size);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // پایان درگِ نود
        if (bendingWire != null) {
            bendingWire = null;
            bendingAnchorIdx = -1;
            repaintCallback.run();
            return;
        }

        // پایان درگ سیستم
        if (draggingSystem != null) {
            draggingSystem = null;
            env.recalcWireBudget();
            repaintCallback.run();
            return;
        }

        // پایان کشیدن سیم
        if (startPort != null) {
            Port endPort = findPortAt(e.getPoint());
            if (endPort != null &&
                    startPort.getSystem() != endPort.getSystem() &&
                    Objects.equals(startPort.getType(), endPort.getType()) &&
                    startPort.getIsEmpty() && endPort.getIsEmpty()) {

                double length = startPort.distanceTo(endPort, env);
                if (env.getRemainingWireLength() >= length) {
                    startPort.setEmpty(false);
                    endPort.setEmpty(false);

                    double sx = startPort.getCenterX(), sy = startPort.getCenterY();
                    double ex = endPort.getCenterX(),   ey = endPort.getCenterY();

                    java.util.List<Point> points = java.util.Collections.emptyList();
                    Wire newWire = new Wire(
                            startPort, endPort,
                            startPort.getSystem(), startPort.getType(),
                            endPort.getSystem(), endPort.getType(),
                            length, sx, sy, ex, ey,
                            points, env
                    );
                    startPort.getSystem().addOutputWire(newWire);
                    endPort.getSystem().addInputWire(newWire);
                    env.addWire(newWire);
                    env.setRemainingWireLength(env.getRemainingWireLength() - length);
                }
            }
            startPort = null;
            currentMouse = null;
            env.clearWirePreview();
            if (endPort != null) endPort.getSystem().checkIndicator();
            repaintCallback.run();
        }
    }

    public Port getStartPort()     { return startPort; }
    public Point getCurrentMouse() { return currentMouse; }

    private Port findPortAt(Point p) {
        for (NetworkSystem sys : env.getSystems()) {
            for (Port port : sys.getInputPorts())  if (port.contains(p)) return port;
            for (Port port : sys.getOutputPorts()) if (port.contains(p)) return port;
        }
        return null;
    }

    private Port tryFindCompatibleEmptyEndPort(Point p, Port start) {
        Port end = findPortAt(p);
        if (end == null) return null;
        if (start.getSystem() == end.getSystem()) return null;
        if (!Objects.equals(start.getType(), end.getType())) return null;
        if (!start.getIsEmpty() || !end.getIsEmpty()) return null;
        return end;
    }

    // ====== Helpers for bending ======
    private static double dist(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2, dy = y1 - y2;
        return Math.hypot(dx, dy);
    }

    private boolean isNearWirePath(Wire w, Point p) {
        final int N = 24;
        double prevX = w.getStartx(), prevY = w.getStarty();
        for (int i = 1; i <= N; i++) {
            double t = i / (double) N;
            var pt = w.getPointAt(t);
            double d = pointToSegmentDistance(p.x, p.y, prevX, prevY, pt.x, pt.y);
            if (d <= PATH_HIT_DIST) return true;
            prevX = pt.x; prevY = pt.y;
        }
        return false;
    }

    private static double pointToSegmentDistance(double px, double py,
                                                 double x1, double y1,
                                                 double x2, double y2) {
        double vx = x2 - x1, vy = y2 - y1;
        double wx = px - x1, wy = py - y1;
        double c1 = vx * wx + vy * wy;
        if (c1 <= 0) return Math.hypot(px - x1, py - y1);
        double c2 = vx * vx + vy * vy;
        if (c2 <= c1) return Math.hypot(px - x2, py - y2);
        double b = c1 / c2;
        double bx = x1 + b * vx, by = y1 + b * vy;
        return Math.hypot(px - bx, py - by);
    }
}
