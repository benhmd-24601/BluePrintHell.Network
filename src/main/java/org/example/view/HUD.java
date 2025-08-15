//package org.example.view;
//
//import javax.swing.*;
//import java.awt.*;
//import java.text.DecimalFormat;
//import org.example.model.GameEnv;
//
//public class HUD extends JPanel {
//    private final GameEnv env;
//    private final Font hudFont;
//    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
//
//    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
//    private final Color WARNING_COLOR = new Color(220, 20, 60);
//    private final Color TEXT_COLOR = new Color(240, 240, 240);
//    private final Color BG_COLOR = new Color(30, 30, 40, 180);
//
//    public HUD(GameEnv env) {
//        this.env = env;
//        setPreferredSize(new Dimension(1000, 40));
//        setBackground(BG_COLOR);
//        hudFont = new Font("Segoe UI", Font.BOLD, 14);
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Graphics2D g2d = (Graphics2D) g;
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        drawHUD(g2d);
//    }
//
//    private void drawHUD(Graphics2D g2d) {
//        int startX = 20;
//        int yPos = 25;
//        int elementWidth = 200;
//
//        drawHudElement(g2d, "Coins", String.valueOf(env.getCoins()), startX, yPos, PRIMARY_COLOR);
//        drawHudElement(g2d, "Time", formatTime(env.getTemporalProgress()), startX + elementWidth, yPos, PRIMARY_COLOR);
//
//        drawProgressElement(g2d, "Packet Loss", env.getTotalPacketLossPercent(), 50, startX + 2*elementWidth, yPos);
//
//        drawHudElement(g2d, "Wire Left", decimalFormat.format(env.getRemainingWireLength()) + "m",
//                startX + 3*elementWidth, yPos, PRIMARY_COLOR);
//    }
//
//    private void drawHudElement(Graphics2D g2d, String title, String value, int x, int y, Color color) {
//        g2d.setFont(hudFont);
//        g2d.setColor(TEXT_COLOR);
//        g2d.drawString(title + ":", x, y);
//        g2d.setColor(color);
//        g2d.drawString(value, x + 70, y);
//    }
//
//    private void drawProgressElement(Graphics2D g2d, String title, double value, double maxValue, int x, int y) {
//        int barWidth = 120, barHeight = 12;
//        int textX = x + barWidth + 10;
//        g2d.setColor(TEXT_COLOR);
//        g2d.drawString(title + ":", x, y);
//
//        g2d.setColor(new Color(50,50,50));
//        g2d.fillRect(x + 70, y - barHeight, barWidth, barHeight);
//
//        double progress = Math.min(value, maxValue);
//        int fillWidth = (int) (barWidth * (progress / maxValue));
//        Color fillColor = value > maxValue * 0.8 ? WARNING_COLOR :
//                value > maxValue * 0.5 ? Color.ORANGE : Color.GREEN;
//
//        g2d.setColor(fillColor);
//        g2d.fillRect(x + 70, y - barHeight, fillWidth, barHeight);
//
//        g2d.setColor(new Color(100,100,100));
//        g2d.drawRect(x + 70, y - barHeight, barWidth, barHeight);
//
//        g2d.setColor(TEXT_COLOR);
//        g2d.drawString(decimalFormat.format(value) + "%", textX, y);
//    }
//
//    private String formatTime(double seconds) {
//        int mins = (int) (seconds / 60);
//        int secs = (int) (seconds % 60);
//        return String.format("%02d:%02d", mins, secs);
//    }
//}
