package org.example.view.render.packets;

import org.example.model.Packet.HeavyPacket8;
import org.example.view.render.*;

import java.awt.*;

public class HeavyPacket8Renderer implements PacketRenderer<HeavyPacket8> {
    @Override
    public void paint(HeavyPacket8 p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int w = (int) Math.round(18 * scale);
        int h = (int) Math.round(12 * scale);
        int x = (int) p.getX() - w/2;
        int y = (int) p.getY() - h/2;
        g2.setColor(new Color(80,180,255));
        g2.fillRoundRect(x, y, w, h, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString("8", x + w/2 - 3, y + h/2 + 4);
    }
}
