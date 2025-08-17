package org.example.view.render;

import org.example.model.Packet.Packet;
import org.example.model.Port;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Systems.SinkSystem;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class SystemsRenderer implements LayerRenderer {
    @Override public int zIndex() { return 20; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        for (NetworkSystem system : ctx.getEnv().getSystems()) {
            drawSystemBox(g2, system);
            drawPorts(g2, system);
            drawIndicator(g2, system);
            drawStoredPackets(g2, system);
        }
    }

    private void drawSystemBox(Graphics2D g2, NetworkSystem system) {
        int sysWidth = 120, sysHeight = 160;
        int x = (int) system.getX();
        int y = (int) system.getY();

        g2.setColor(Color.GRAY);
        g2.fillRect(x, y, sysWidth, sysHeight);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 5, y + 30, sysWidth - 10, sysHeight - 30);
    }

    private void drawPorts(Graphics2D g2, NetworkSystem system) {
        int portSize = 15;

        for (Port port : system.getInputPorts()) {
            int px = (int) port.getX();
            int py = (int) port.getY();
            if ("square".equals(port.getType())) {
                g2.setColor(Color.GREEN);
                g2.fillRect(px, py, portSize, portSize);
            } else {
                g2.setColor(Color.YELLOW);
                Polygon tri = new Polygon();
                tri.addPoint(px, py + portSize / 2);
                tri.addPoint(px + portSize, py);
                tri.addPoint(px + portSize, py + portSize);
                g2.fillPolygon(tri);
            }
        }

        for (Port port : system.getOutputPorts()) {
            int px = (int) port.getX();
            int py = (int) port.getY();
            if ("square".equals(port.getType())) {
                g2.setColor(Color.GREEN);
                g2.fillRect(px, py, portSize, portSize);
            } else {
                g2.setColor(Color.YELLOW);
                Polygon tri = new Polygon();
                tri.addPoint(px + portSize, py + portSize / 2);
                tri.addPoint(px, py);
                tri.addPoint(px, py + portSize);
                g2.fillPolygon(tri);
            }
        }
    }

    private void drawIndicator(Graphics2D g2, NetworkSystem system) {
        if (system.isSourceSystem()) return;
        int lightRadius = 12;
        int x = (int) system.getX() + 120 - lightRadius - 5;
        int y = (int) system.getY() + 5;
        g2.setColor(system.isIndicatorOn() ? Color.GREEN : Color.DARK_GRAY);
        g2.fillOval(x, y, lightRadius, lightRadius);
        g2.setColor(Color.BLACK);
        g2.drawOval(x, y, lightRadius, lightRadius);
    }

    private void drawStoredPackets(Graphics2D g2, NetworkSystem system) {
        List<Packet> packets = (system instanceof SinkSystem sink)
                ? sink.getReceivedPackets()
                : system.getPacketStorage();

        int startX = (int) system.getX() + 20;
        int startY = (int) system.getY() + 40;
        int maxPerRow = 5;
        int hGap = 15, vGap = 15;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.drawString(system.isSourceSystem() ? "Source" : "Storage:", startX + 8, startY - 8);

        for (int i = 0; i < packets.size(); i++) {
            Packet p = packets.get(i);
            int row = i / maxPerRow, col = i % maxPerRow;
            int x = startX + col * hGap;
            int y = startY + row * vGap;

            if (Objects.equals(p.getPortKey(), "square")) {
                g2.setColor(Color.GREEN);
                g2.fillRect(x, y, 10, 10);
            } else {
                g2.setColor(Color.RED);
                int[] xPoints = {x, x + 10, x + 5};
                int[] yPoints = {y + 10, y + 10, y};
                g2.fillPolygon(xPoints, yPoints, 3);
            }

            g2.setColor(Color.WHITE);
            g2.drawString(String.valueOf(p.getId()), x, y - 2);
        }
    }
}
