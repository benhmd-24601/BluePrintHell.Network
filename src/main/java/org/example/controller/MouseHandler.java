package org.example.controller;

import org.example.model.*;
import org.example.util.Debug;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class MouseHandler extends MouseAdapter {
    private final GameEnv env;
    private final Runnable repaintCallback;

    private Port startPort;
    private Point currentMouse;


    // برای گزارش hover فقط وقتی تغییر می‌کند
    private Port lastHoverPort = null;



    public MouseHandler(GameEnv env, Runnable repaintCallback) {
        this.env = env;
        this.repaintCallback = repaintCallback;
    }



    private String portDesc(Port p) {
        String side = p.getSide() == 1 ? "OUT" : "IN";
        return side + " " + p.getType() + " port@(" + (int)p.getX() + "," + (int)p.getY() +
                ") of sys@(" + (int)p.getSystem().getX() + "," + (int)p.getSystem().getY() + ")";
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

        // 2) مختصات لحظه‌ای موس در درگ (با throttle)
        if (Debug.throttle("mouseDrag", 30)) {
            Debug.log("[MOUSE]", "drag (" + e.getX() + "," + e.getY() + ")");
        }

        if (startPort != null) {
            repaintCallback.run();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {



        Port clickedPort = findPortAt(e.getPoint());
        if (clickedPort != null) {
            Debug.log("[CLICK]", "on " + portDesc(clickedPort));
        } else {
            Debug.log("[CLICK]", "canvas (" + e.getX() + "," + e.getY() + ")");
        }



        // Timeline hit-test (from GameView drawing constants)
        int barWidth = 300, barHeight = 20;
        int barX = 1000 - barWidth - 40; // approx width of frame
        int barY = 700 - barHeight - 40;

        int x = e.getX() ;
        int y = e.getY();

        if (x >= barX && x <= barX + barWidth && y >= barY && y <= barY + barHeight) {
            double clickPercent = (x - barX) / (double) barWidth;
            env.simulateFastForward(clickPercent);
            repaintCallback.run();
            return;
        }

        // quick remove when clicking output with wire
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
        startPort = clickedPort;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
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

                    List<Point> points = new ArrayList<>();
                    points.add(new Point((int) startPort.getX(), (int) startPort.getY()));
                    points.add(new Point((int) startPort.getX() + 25, (int) startPort.getY()));
                    points.add(new Point((int) (startPort.getX() + endPort.getX()) / 2,
                            (int) (startPort.getY() + endPort.getY()) / 2));
                    points.add(new Point((int) endPort.getX() - 25, (int) endPort.getY()));
                    points.add(new Point((int) endPort.getX(), (int) endPort.getY()));

                    Wire newWire = new Wire(
                            startPort, endPort,
                            startPort.getSystem(), startPort.getType(),
                            endPort.getSystem(), endPort.getType(),
                            length, startPort.getX(), startPort.getY(),
                            endPort.getX(), endPort.getY(),
                            points, env
                    );
                    startPort.getSystem().addOutputWire(newWire);
                    endPort.getSystem().addInputWire(newWire);
                    env.addWire(newWire);
                    env.setRemainingWireLength(env.getRemainingWireLength() - length);
                }
            }
            // reset
            startPort = null;
            currentMouse = null;
            if (endPort != null) endPort.getSystem().checkIndicator();
            repaintCallback.run();
        }
    }


    public Port getStartPort() { return startPort; }
    public Point getCurrentMouse() { return currentMouse; }

    private Port findPortAt(Point p) {
        for (NetworkSystem sys : env.getSystems()) {
            for (Port port : sys.getInputPorts())
                if (port.contains(p)) return port;
            for (Port port : sys.getOutputPorts())
                if (port.contains(p)) return port;
        }
        return null;
    }
}
