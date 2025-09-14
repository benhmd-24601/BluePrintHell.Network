package org.example.view.render.packets;

import org.example.model.Packet.Packet;
import org.example.view.render.*;

import java.awt.*;

public class DefaultPacketRenderer implements PacketRenderer<Packet> {
    @Override
    public void paint(Packet p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int r = (int) Math.round(6 * scale);
        int x = (int) p.getX() - r/2;
        int y = (int) p.getY() - r/2;
        g2.setColor(mismatch ? new Color(255,165,0) : Color.WHITE);
        g2.fillOval(x, y, r, r);
    }
}
