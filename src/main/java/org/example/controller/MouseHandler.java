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

    private Port lastHoverPort = null;

    private NetworkSystem draggingSystem = null;
    private int dragOffsetX = 0, dragOffsetY = 0;

    // === خم‌کردن سیم (Multi-Anchor)
    private Wire bendingWire = null;
    private int  bendingAnchorIdx = -1;
    private static final int HANDLE_HIT_R = 10;
    private static final int PATH_HIT_DIST = 6;

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
        return side + " " + p.getType() + " port@(" + (int) p.getX() + "," + (int) p.getY() + ")"
                + " of sys@(" + (int) p.getSystem().getX() + "," + (int) p.getSystem().getY() + ")";
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

        if (startPort != null) {
            Port candidateEnd = tryFindCompatibleEmptyEndPort(e.getPoint(), startPort);
            env.setWirePreview(startPort, e.getPoint(), candidateEnd);
        } else {
            env.clearWirePreview();
        }

        repaintCallback.run();
    }

    // Sisyphus (لوکال)
    private boolean sisyphusActive = false;
    private double sisyphusOx = 0, sisyphusOy = 0;

    @Override
    public void mousePressed(MouseEvent e) {
        currentMouse = e.getPoint();

        boolean shift = (e.isShiftDown());
        boolean ctrl  = (e.isControlDown());
        boolean alt   = (e.isAltDown());

        // 1) اول اگر Shift نگه‌داشته شده و روی سیستم غیرمرجع کلیک شده → شروع Sisyphus
        if (shift) {
            NetworkSystem sys = findSystemAt(e.getPoint());
            if (sys != null && !sys.isSourceSystem()) {
                if (env.getCoins() >= 15) {
                    env.setCoins(env.getCoins() - 15);
                    draggingSystem = sys;
                    sisyphusActive = true;
                    sisyphusOx = sys.getX();
                    sisyphusOy = sys.getY();
                    dragOffsetX = e.getX() - (int) sys.getX();
                    dragOffsetY = e.getY() - (int) sys.getY();
                } else {
                    org.example.util.Debug.log("[SISYPHUS]", "not enough coins");
                }
                repaintCallback.run();
                return;
            }
        }

        // 2) اگر روی مسیر سیم کلیک شد با کلیدهای مخصوص
        Wire hitWire = null;
        for (Wire w : env.getWires()) {
            if (isNearWirePath(w, e.getPoint())) { hitWire = w; break; }
        }
        if (hitWire != null) {
            if (ctrl) { // Aergia
                if (env.getAergiaCooldown() <= 0 && env.getCoins() >= 10) {
                    env.setCoins(env.getCoins() - 10);
                    env.placeField(hitWire, e.getX(), e.getY(), GameEnv.WireField.Type.AERGIA);
                    repaintCallback.run();
                } else {
                    org.example.util.Debug.log("[AERGIA]", "cooldown/coins");
                }
                return;
            }
            if (alt) { // Eliphas
                if (env.getCoins() >= 20) {
                    env.setCoins(env.getCoins() - 20);
                    env.placeField(hitWire, e.getX(), e.getY(), GameEnv.WireField.Type.ELIPHAS);
                    repaintCallback.run();
                } else {
                    org.example.util.Debug.log("[ELIPHAS]", "not enough coins");
                }
                return;
            }
            if (shift) { // Curve point (anchor)
                if (!env.consumeCurvePoint()) {
                    org.example.util.Debug.log("[CURVE]", "no curve charges");
                    return;
                }
                int idx = hitWire.addAnchorAtNearest(e.getX(), e.getY()); // max 3
                if (idx >= 0) {
                    bendingWire = hitWire;
                    bendingAnchorIdx = idx;
                    env.recalcWireBudget();
                    repaintCallback.run();
                } else {
                    // اگر جا نشد (۳ تا پر بود)، فعلاً capacity رو پس نمی‌دیم
                    org.example.util.Debug.log("[CURVE]", "anchors full");
                }
                return;
            }
        }

        // === باقی رفتارها مثل قبل ===
        // (سیم‌کشی، تایم‌لاین، حذف سیم از پورت خروجی، ...)
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

        Port clickedPort = findPortAt(e.getPoint());
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

        if (clickedPort != null) {
            startPort = clickedPort;
            Port candidateEnd = tryFindCompatibleEmptyEndPort(e.getPoint(), startPort);
            env.setWirePreview(startPort, e.getPoint(), candidateEnd);
            repaintCallback.run();
            return;
        }

        // درگ مستقیم نودِ موجود (برای جابجایی نود کرو)
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
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentMouse = e.getPoint();

        // جابجایی نود کرو
        if (bendingWire != null && bendingAnchorIdx >= 0) {
            bendingWire.setAnchor(bendingAnchorIdx, e.getX(), e.getY());
            env.recalcWireBudget();
            repaintCallback.run();
            return;
        }

        // Sisyphus: درگ محدود
        if (sisyphusActive && draggingSystem != null) {
            double tx = e.getX() - dragOffsetX;
            double ty = e.getY() - dragOffsetY;
            double dx = tx - sisyphusOx, dy = ty - sisyphusOy;
            double d  = Math.hypot(dx, dy);
            if (d > GameEnv.SISYPHUS_RADIUS) {
                double s = GameEnv.SISYPHUS_RADIUS / d;
                dx *= s; dy *= s;
            }
            double nx = sisyphusOx + dx, ny = sisyphusOy + dy;
            env.tryMoveSystemRespectingConstraints(draggingSystem, nx, ny);
            repaintCallback.run();
            return;
        }

        // پریویو سیم
        if (startPort != null) {
            Port candidateEnd = tryFindCompatibleEmptyEndPort(e.getPoint(), startPort);
            env.setWirePreview(startPort, e.getPoint(), candidateEnd);
            repaintCallback.run();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // پایان درگ نود
        if (bendingWire != null) {
            bendingWire = null;
            bendingAnchorIdx = -1;
            repaintCallback.run();
            return;
        }

        // پایان Sisyphus
        if (sisyphusActive) {
            draggingSystem = null;
            sisyphusActive = false;
            env.recalcWireBudget();
            repaintCallback.run();
            return;
        }

        // پایان سیم‌کشی (مثل قبل)
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
