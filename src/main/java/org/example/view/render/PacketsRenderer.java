package org.example.view.render;

import org.example.model.Packet.Packet;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class PacketsRenderer implements LayerRenderer {
    @Override public int zIndex() { return 30; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        List<Packet> packets = ctx.getEnv().getPackets();

        for (Packet p : packets) {
            if (p == null || p.getWire() == null) continue;
            int drawX = (int) p.getX();
            int drawY = (int) p.getY() + 5;

            boolean mismatch = !Objects.equals(p.getPortKey(), p.getWire().getStartPortType());
            if (mismatch) g2.setColor(new Color(255, 165, 0)); // اورنج برای ناسازگار
            if (Objects.equals(p.getPortKey(), "square")) {
                if (!mismatch) g2.setColor(Color.GREEN);
                g2.fillRect(drawX, drawY, 10, 10);
            } else {
                if (!mismatch) g2.setColor(Color.RED);
                int[] xPoints = {drawX, drawX + 10, drawX + 5};
                int[] yPoints = {drawY + 10, drawY + 10, drawY};
                g2.fillPolygon(xPoints, yPoints, 3);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Consolas", Font.PLAIN, 12));
            g2.drawString(String.valueOf(p.getId()), drawX, drawY - 2);
        }
    }
}
