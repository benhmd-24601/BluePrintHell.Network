package org.example.view.render;

import org.example.model.GameEnv;

import java.awt.*;

public class HUDRenderer implements LayerRenderer {
    @Override public int zIndex() { return 40; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        GameEnv env = ctx.getEnv();
        int w = ctx.getWidth();
        int hudHeight = ctx.getHudHeight();

        // نوار بالا
        g2.setColor(new Color(25,25,25));
        g2.fillRect(0, 0, w, hudHeight);
        g2.setColor(new Color(70,70,70));
        g2.drawLine(0, hudHeight - 1, w, hudHeight - 1);

        // نوشته‌ها
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.drawString("Packets: " + env.getPackets().size(), 20, 20);
        g2.drawString("Wires: " + env.getWires().size(), 20, 38);
        g2.drawString("Systems: " + env.getSystems().size(), 160, 20);
        g2.drawString("Coins: " + env.getCoins(), 160, 38);
        g2.drawString("Progress: " + (int) env.getTemporalProgress(), 280, 20);

        g2.setColor(env.getTotalPacketLossPercent() >= 50.0 ? Color.RED : Color.WHITE);
        g2.drawString(String.format("Packet Loss: %.1f%%", env.getTotalPacketLossPercent()), 280, 38);

        // میله مصرف سیم
        double usage = 1.0 - (env.getRemainingWireLength() / env.getInitialWireLength());
        int barWidth = 200, barHeight = 12;
        int barX = w - barWidth - 30;
        int barY = 20;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, barHeight);
        g2.setColor(Color.GREEN);
        g2.fillRect(barX, barY, (int) (barWidth * usage), barHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.drawString("Wire Used: " + (int) (usage * 100) + "%", barX, barY + 25);

        // تایم‌لاین پایین-راست
        drawTimeline(g2, ctx);
    }

    private void drawTimeline(Graphics2D g2, RenderContext ctx) {
        int barWidth = 300, barHeight = 20;
        int barX = ctx.getWidth() - barWidth - 40;
        int barY = ctx.getHeight() - barHeight - 40;

        double progress = Math.min(ctx.getEnv().getTemporalProgress() / 100.0, 1.0);
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, barHeight);
        g2.setColor(Color.CYAN);
        g2.fillRect(barX, barY, (int) (barWidth * progress), barHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);
        g2.drawString("Timeline: " + (int) (progress * 100) + "%", barX, barY - 10);
    }
}
