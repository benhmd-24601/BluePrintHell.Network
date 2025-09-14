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

    private NetworkSystem findSystemAt(Point p) {
        // Hit-test مستطیل بدنهٔ سیستم مطابق رندر: 120×160
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

        // 2) مختصات لحظه‌ای موس (با throttle هر 30ms)
        if (Debug.throttle("mouseMove", 30)) {
            Debug.log("[MOUSE]", "move (" + e.getX() + "," + e.getY() + ")");
        }

        // 3) اگر ماوس رفت روی پورت/از پورت خارج شد
        Port hover = findPortAt(e.getPoint());
        if (hover != lastHoverPort) {
            if (hover != null) {
                Debug.log("[HOVER]", "over " + portDesc(hover));
            } else {
                Debug.log("[HOVER]", "left port");
            }
            lastHoverPort = hover;
        }

        repaintCallback.run();
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        currentMouse = e.getPoint();

        // اگر در حال درگ سیستم هستیم
        if (draggingSystem != null) {
            int nx = e.getX() - dragOffsetX;
            int ny = e.getY() - dragOffsetY;
            env.moveSystem(draggingSystem, nx, ny); // پورت‌ها و بودجه سیم به‌روز می‌شوند
            repaintCallback.run();
            return;
        }

        // در غیر این صورت: اگر در حال کشیدن سیم هستیم
        if (Debug.throttle("mouseDrag", 30)) {
            Debug.log("[MOUSE]", "drag (" + e.getX() + "," + e.getY() + ")");
        }
        if (startPort != null) repaintCallback.run();
    }


    @Override
    public void mousePressed(MouseEvent e) {
        Port clickedPort = findPortAt(e.getPoint());
        if (clickedPort != null) {
            Debug.log("[CLICK]", "on " + portDesc(clickedPort));
        } else {
            Debug.log("[CLICK]", "canvas (" + e.getX() + "," + e.getY() + ")");
        }

        // --- Timeline hit-test (مثل قبل)
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

        // --- اگر روی خروجی با وایر کلیک شد: حذف سریع وایر (مثل قبل)
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

        // --- شروع کشیدن سیم اگر روی پورت بود
        if (clickedPort != null) {
            startPort = clickedPort;
            return;
        }
        for (NetworkSystem s : env.getSystems()) {
            if (toggleRectFor(s).contains(e.getPoint())) {
                boolean nowEnabled = !s.isEnabled();
                s.setEnabled(nowEnabled);
                // اگر setter تایمر داری:
                try {
                    s.getClass().getMethod("setReenableTimerSec", double.class)
                            .invoke(s, nowEnabled ? 0.0 : 5.0);
                } catch (Exception ignore) { /* اگر نداری، نادیده بگیر */ }

                repaintCallback.run();
                return; // مصرف رویداد
            }
        }

        // --- در غیر این‌صورت: شروع درگِ سیستم
        NetworkSystem sys = findSystemAt(e.getPoint());
        if (sys != null) {
            draggingSystem = sys;
            dragOffsetX = e.getX() - (int) sys.getX();
            dragOffsetY = e.getY() - (int) sys.getY();
            return;
        }

        // otherwise: هیچ کاری لازم نیست
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
        // پایان درگ سیستم
        if (draggingSystem != null) {
            draggingSystem = null;
            env.recalcWireBudget(); // اطمینان از نهایی شدن محاسبه
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
                    double ex = endPort.getCenterX(), ey = endPort.getCenterY();

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
            if (endPort != null) endPort.getSystem().checkIndicator();
            repaintCallback.run();
        }
    }


    public Port getStartPort() {
        return startPort;
    }

    public Point getCurrentMouse() {
        return currentMouse;
    }

    private Port findPortAt(Point p) {
        for (NetworkSystem sys : env.getSystems()) {
            for (Port port : sys.getInputPorts())
                if (port.contains(p)) return port;
            for (Port port : sys.getOutputPorts())
                if (port.contains(p)) return port;
        }
        return null;
    }
    // هم‌اندازه portSize در View

}
