package org.example.view.render;

import org.example.model.GameEnv;

import java.awt.*;

public class HUDRenderer implements LayerRenderer {
    private final int hudHeight = 60;

    @Override public int zIndex() { return 100; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        GameEnv env = ctx.getEnv();

        // Top bar
        g2.setColor(new Color(25,25,25));
        g2.fillRect(0, 0, ctx.getWidth(), hudHeight);
        g2.setColor(new Color(70,70,70));
        g2.drawLine(0, hudHeight - 1, ctx.getWidth(), hudHeight - 1);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 14));
        g2.drawString("Packets: " + env.getPackets().size(), 20, 20);
        g2.drawString("Wires: " + env.getWires().size(), 20, 38);
        g2.drawString("Systems: " + env.getSystems().size(), 160, 20);

        g2.drawString("Coins: " + env.getCoins(), 160, 38);
        g2.drawString("Progress: " + (int) env.getTemporalProgress(), 280, 20);

        g2.setColor(env.getTotalPacketLossPercent() >= 50.0 ? Color.RED : Color.WHITE);
        g2.drawString(String.format("Packet Loss: %.1f%%", env.getTotalPacketLossPercent()), 280, 38);

//        // Wire usage bar
//        double usage = 1.0 - (env.getRemainingWireLength() / env.getInitialWireLength());
//        int barWidth = 200, barHeight = 12;
//        int barX = ctx.getWidth() - barWidth - 30;
//        int barY = 20;
//
//        g2.setColor(Color.DARK_GRAY);
//        g2.fillRect(barX, barY, barWidth, barHeight);
//        g2.setColor(Color.GREEN);
//        g2.fillRect(barX, barY, (int) (barWidth * usage), barHeight);
//        g2.setColor(Color.WHITE);
//        g2.drawRect(barX, barY, barWidth, barHeight);
//        g2.drawString("Wire Used: " + (int) (usage * 100) + "%", barX, barY + 25);



        double used = env.getUsedWireLength();
        double total = env.getInitialWireLength();
        double usage = (total <= 0) ? 0 : Math.min(1.0, Math.max(0.0, used / total));

        int barWidth = 200, barHeight = 12;
        int barX = ctx.getWidth() - barWidth - 30;
        int barY = 20;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barWidth, barHeight);
        g2.setColor(env.isOverBudget() ? Color.RED : Color.GREEN);
        int fill = (int) (barWidth * Math.min(usage, 1.0));
        g2.fillRect(barX, barY, fill, barHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barWidth, barHeight);

        String label = String.format("Wire: %.0f / %.0f%s",
                Math.max(0, used), total, env.isOverBudget() ? " (OVER)" : "");
        g2.drawString(label, barX, barY + 25);



        // Timeline (bottom right)
        int tWidth = 300, tHeight = 20;
        int tX = ctx.getWidth() - tWidth - 40;
        int tY = ctx.getHeight() - tHeight - 40;
        double progress = Math.min(env.getTemporalProgress() / 100.0, 1.0);

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(tX, tY, tWidth, tHeight);
        g2.setColor(Color.CYAN);
        g2.fillRect(tX, tY, (int) (tWidth * progress), tHeight);
        g2.setColor(Color.WHITE);
        g2.drawRect(tX, tY, tWidth, tHeight);
        g2.drawString("Timeline: " + (int) (progress * 100) + "%", tX, tY - 10);
    }
}
