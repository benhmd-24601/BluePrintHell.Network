package org.example.view;

import org.example.controller.GameLoop;
import org.example.model.*;
import org.example.model.Packet.Packet;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Systems.SinkSystem;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class GameView extends JPanel {
    private final GameEnv env;
    private GameLoop gameLoop;

    private final int hudHeight = 60;

    // store toggles
    private final JToggleButton atarToggle = new JToggleButton("Atar");
    private final JToggleButton airyToggle = new JToggleButton("Airyaman");
    private final JToggleButton anaToggle = new JToggleButton("Anahita");

    // back button callback
    private Runnable onBackToMenu;

    public GameView(GameEnv env) {
        this.env = env;
        setLayout(null);

        // source buttons
//        for (NetworkSystem system : env.getSystems()) {
//            if (system instanceof SourceSystem src) {
//                JButton btn = src.getGenerateButton();
//                btn.setBounds((int) src.getX(), (int) src.getY() - 25, 60, 20);
//                add(btn);
//            }
//        }
//
//        JButton backButton = new JButton("⏎ Menu");
//        backButton.setBounds(850, 65, 100, 30);
//        styleHudButton(backButton);
//        backButton.addActionListener(e -> { if (onBackToMenu != null) onBackToMenu.run(); });
//        add(backButton);
//
//        // store button
//        JButton storeButton = new JButton("Store");
//        storeButton.setBounds(30, 750, 100, 30);
//        styleHudButton(storeButton);
//        storeButton.addActionListener(e -> {
//            JFrame storeFrame = new JFrame("Store");
//            storeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//            storeFrame.setSize(400, 300);
//            storeFrame.setLocationRelativeTo(this);
//            storeFrame.setContentPane(new Store(env, storeFrame::dispose));
//            storeFrame.setVisible(true);
//        });
//        add(storeButton);
//
//        // power bar (ability toggles)
//        JPanel powerBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 5));
//        powerBar.setOpaque(false);
//        powerBar.setBounds(0, getHeight() - 50, getWidth(), 50);
//
//        styleHudButton(atarToggle);
//        styleHudButton(airyToggle);
//        styleHudButton(anaToggle);
//        atarToggle.setEnabled(false);
//        airyToggle.setEnabled(false);
//        anaToggle.setEnabled(false);
//
//        atarToggle.addActionListener(ev -> {
//            if (atarToggle.isSelected()) env.activateAtar();
//            else env.setActiveAtar(false);
//            refreshPowerBar();
//        });
//        airyToggle.addActionListener(ev -> {
//            if (airyToggle.isSelected()) env.activateAiryaman();
//            else env.setActiveAiryaman(false);
//            refreshPowerBar();
//        });
//        anaToggle.addActionListener(ev -> {
//            env.setActiveAnahita(anaToggle.isSelected());
//            if (env.isActiveAnahita()) env.applyEffect("Anahita");
//        });
//        atarToggle.setPreferredSize(new Dimension(80, 30));
//        airyToggle.setPreferredSize(new Dimension(100, 30));
//        anaToggle.setPreferredSize(new Dimension(90, 30));
//
//        powerBar.add(atarToggle);
//        powerBar.add(airyToggle);
//        powerBar.add(anaToggle);
//        add(powerBar);
//
//        addComponentListener(new java.awt.event.ComponentAdapter() {
//            @Override public void componentResized(java.awt.event.ComponentEvent e) {
//                powerBar.setBounds(0, getHeight() - 50, getWidth(), 50);
//            }
//        });
//        setComponentZOrder(powerBar, 0);
    }

    public void setOnBackToMenu(Runnable r) { this.onBackToMenu = r; }
    public void setGameLoop(GameLoop loop) { this.gameLoop = loop; }

    public void refresh() {
        repaint();
        revalidate();
//        refreshPowerBar();
    }
//
//    public void refreshPowerBar() {
//        atarToggle.setEnabled(env.hasAtar());
//        airyToggle.setEnabled(env.hasAiryaman());
//        anaToggle.setEnabled(env.hasAnahita());
//
//        atarToggle.setSelected(env.isActiveAtar());
//        airyToggle.setSelected(env.isActiveAiryaman());
//        anaToggle.setSelected(env.isActiveAnahita());
//    }


    // متد جدید برای افزودن کامپوننت‌های سفارشی توسط کنترلر
    public void addCustomComponent(Component component) {
        add(component);
    }

    // متد جدید برای حذف کامپوننت‌های سفارشی
    public void removeCustomComponent(Component component) {
        remove(component);
    }

//    private void styleHudButton(AbstractButton b) {
//        b.setBackground(Color.DARK_GRAY);
//        b.setForeground(Color.WHITE);
//        b.setFocusPainted(false);
//        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
//    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // background checker
        int squareSize = 40;
        for (int y = 0; y < getHeight(); y += squareSize) {
            for (int x = 0; x < getWidth(); x += squareSize) {
                g.setColor(((x/squareSize + y/squareSize) % 2 == 0) ? new Color(40,40,40) : new Color(60,60,60));
                g.fillRect(x, y, squareSize, squareSize);
            }
        }

        // bottom bar
        int barHeight = 120;
        int y = getHeight() - barHeight;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, y, getWidth(), barHeight);

        drawHUD(g2d);
        for (NetworkSystem system : env.getSystems()) drawNetworkSystem(g2d, system);
        drawStraightWires(g2d, env.getWires());
        drawPackets(g2d, env.getPackets());
        drawTimeline(g2d);
        drawDraggingWire(g2d);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(new Color(25,25,25));
        g2d.fillRect(0, 0, getWidth(), hudHeight);
        g2d.setColor(new Color(70,70,70));
        g2d.drawLine(0, hudHeight - 1, getWidth(), hudHeight - 1);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2d.drawString("Packets: " + env.getPackets().size(), 20, 20);
        g2d.drawString("Wires: " + env.getWires().size(), 20, 38);
        g2d.drawString("Systems: " + env.getSystems().size(), 160, 20);

        g2d.drawString("Coins: " + env.getCoins(), 160, 38);
        g2d.drawString("Progress: " + (int) env.getTemporalProgress(), 280, 20);

        g2d.setColor(env.getTotalPacketLossPercent() >= 50.0 ? Color.RED : Color.WHITE);
        g2d.drawString(String.format("Packet Loss: %.1f%%", env.getTotalPacketLossPercent()), 280, 38);

        double usage = 1.0 - (env.getRemainingWireLength() / env.getInitialWireLength());
        int barWidth = 200, barHeight = 12;
        int barX = getWidth() - barWidth - 30;
        int barY = 20;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(barX, barY, (int) (barWidth * usage), barHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
        g2d.drawString("Wire Used: " + (int) (usage * 100) + "%", barX, barY + 25);
    }

    private void drawNetworkSystem(Graphics2D g2d, NetworkSystem system) {
        int sysWidth = 120, sysHeight = 160;
        int x = (int) system.getX();
        int y = (int) system.getY();

        g2d.setColor(Color.GRAY);
        g2d.fillRect(x, y, sysWidth, sysHeight);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x + 5, y + 30, sysWidth - 10, sysHeight - 30);

        int portSize = 15;

        for (Port port : system.getInputPorts()) {
            int px = (int) port.getX();
            int py = (int) port.getY();
            if (port.getType().equals("square")) {
                g2d.setColor(Color.GREEN);
                g2d.fillRect(px, py, portSize, portSize);
            } else {
                g2d.setColor(Color.YELLOW);
                Polygon triangle = new Polygon();
                triangle.addPoint(px, py + portSize / 2);
                triangle.addPoint(px + portSize, py);
                triangle.addPoint(px + portSize, py + portSize);
                g2d.fillPolygon(triangle);
            }
        }

        for (Port port : system.getOutputPorts()) {
            int px = (int) port.getX();
            int py = (int) port.getY();
            if (port.getType().equals("square")) {
                g2d.setColor(Color.GREEN);
                g2d.fillRect(px, py, portSize, portSize);
            } else {
                g2d.setColor(Color.YELLOW);
                Polygon triangle = new Polygon();
                triangle.addPoint(px + portSize, py + portSize / 2);
                triangle.addPoint(px, py);
                triangle.addPoint(px, py + portSize);
                g2d.fillPolygon(triangle);
            }
        }

        if (!system.isSourceSystem()) {
            int lightRadius = 12;
            int lightX = x + 120 - lightRadius - 5;
            int lightY = y + 5;
            g2d.setColor(system.isIndicatorOn() ? Color.GREEN : Color.DARK_GRAY);
            g2d.fillOval(lightX, lightY, lightRadius, lightRadius);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(lightX, lightY, lightRadius, lightRadius);
        }
        drawStoredPackets(g2d, system);
    }

    private void drawStoredPackets(Graphics2D g2d, NetworkSystem system) {
        List<Packet> packets =
                (system instanceof SinkSystem sink) ? sink.getReceivedPackets()
                        : system.getPacketStorage();

        int startX = (int) system.getX() + 20;
        int startY = (int) system.getY() + 40;
        int maxPerRow = 5;
        int hGap = 15, vGap = 15;

        g2d.setColor(Color.WHITE);
        g2d.drawString(system.isSourceSystem() ? "Source" : "Storage:", startX + 8, startY - 8);

        for (int i = 0; i < packets.size(); i++) {
            Packet p = packets.get(i);
            int row = i / maxPerRow, col = i % maxPerRow;
            int x = startX + col * hGap;
            int y = startY + row * vGap;

            if (Objects.equals(p.getPortKey(), "square")) {
                g2d.setColor(Color.GREEN);
                g2d.fillRect(x, y, 10, 10);
            } else {
                g2d.setColor(Color.RED);
                int[] xPoints = {x, x + 10, x + 5};
                int[] yPoints = {y + 10, y + 10, y};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
            g2d.drawString(String.valueOf(p.getId()), x, y - 2);
        }
    }

//    private void drawCurveWires(Graphics2D g2d, List<Wire> wires) {
//        for (Wire wire : wires) {
//            for (int i = 0; i < wire.getPoints().size() - 2; i += 2) {
//                Point start = wire.getPoints().get(i);
//                Point control = wire.getPoints().get(i + 1);
//                Point end = wire.getPoints().get(i + 2);
//                g2d.setColor(Objects.equals(wire.getStartPortType(), "square") ? Color.GREEN : Color.YELLOW);
//                QuadCurve2D q = new QuadCurve2D.Float();
//                q.setCurve(start.getX(), start.getY() , control.getX(), control.getY() - 20, end.getX(), end.getY() );
//                g2d.draw(q);
//            }
//        }
//    }
private void drawStraightWires(Graphics2D g2d, List<Wire> wires) {
    for (Wire wire : wires) {
        int x1 = (int) wire.getStartx();
        int y1 = (int) wire.getStarty();
        int x2 = (int) wire.getEndX();
        int y2 = (int) wire.getEndY();

        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(Objects.equals(wire.getStartPortType(), "square") ? Color.GREEN : Color.YELLOW);
        g2d.drawLine(x1, y1, x2, y2);
    }
}

    private void drawPackets(Graphics g, List<Packet> packets) {
        for (Packet p : packets) {
            if (p == null || p.getWire() == null) continue;
            int drawX = (int) p.getX();
            int drawY = (int) p.getY() + 5;

            boolean mismatch = !Objects.equals(p.getPortKey(), p.getWire().getStartPortType());
            if (mismatch) g.setColor(new Color(255, 165, 0));
            if (p.getPortKey().equals("square")) {
                if (!mismatch) g.setColor(Color.GREEN);
                g.fillRect(drawX, drawY, 10, 10);
            } else {
                if (!mismatch) g.setColor(Color.RED);
                int[] xPoints = {drawX, drawX + 10, drawX + 5};
                int[] yPoints = {drawY + 10, drawY + 10, drawY};
                g.fillPolygon(xPoints, yPoints, 3);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.PLAIN, 12));
            g.drawString(String.valueOf(p.getId()), drawX, drawY - 2);
        }
    }

    private void drawDraggingWire(Graphics2D g2d) {
        // MouseHandler state گرفتن از لیسنر رجیستری سراسری سخت می‌شود؛
        // این نمایش «سیم درگ» را می‌توان با MouseMotionListener جداگانه و repaint callback هندل کرد.
        // (برای سادگی در این نسخه از نمایش سیم درگ صرف‌نظر شده — اگر خواستی، state را از MouseHandler دریافت کن.)
    }

    private void drawTimeline(Graphics2D g2d) {
        int barWidth = 300, barHeight = 20;
        int barX = getWidth() - barWidth - 40;
        int barY = getHeight() - barHeight - 40;

        double progress = Math.min(env.getTemporalProgress() / 100.0, 1.0);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        g2d.setColor(Color.CYAN);
        g2d.fillRect(barX, barY, (int) (barWidth * progress), barHeight);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
        g2d.drawString("Timeline: " + (int) (progress * 100) + "%", barX, barY - 10);
    }
}
