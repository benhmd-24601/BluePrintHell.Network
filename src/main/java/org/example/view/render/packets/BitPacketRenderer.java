package org.example.view.render.packets;

import org.example.model.Packet.BitPacket;
import org.example.view.render.*;

import java.awt.*;

public class BitPacketRenderer implements PacketRenderer<BitPacket> {
    @Override
    public void paint(BitPacket p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int r = (int) Math.round(4 * scale);
        int x = (int) p.getX();
        int y = (int) p.getY() + 5;
        g2.setColor(Color.CYAN);
        g2.fillOval(x, y, r, r);
    }
}
