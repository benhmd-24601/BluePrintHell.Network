package org.example.view.render.packets;

import org.example.model.Packet.TrojanPacket;
import org.example.view.render.*;

import java.awt.*;

public class TrojanPacketRenderer implements PacketRenderer<TrojanPacket> {
    @Override
    public void paint(TrojanPacket p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int s = (int) Math.round(12 * scale);
        int cx = (int) p.getX();
        int cy = (int) p.getY() + 5;

        g2.setColor(mismatch ? new Color(255,165,0) : Color.MAGENTA);
        Polygon diamond = new Polygon(
                new int[]{cx, cx + s/2, cx, cx - s/2},
                new int[]{cy - s/2, cy, cy + s/2, cy},
                4
        );
        g2.fillPolygon(diamond);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, Math.max(8, (int)(10*scale))));
        g2.drawString("T", cx - 3, cy + 4);
    }
}
