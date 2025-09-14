package org.example.view.render.packets;

import org.example.model.Packet.SquarePacket;
import org.example.view.render.*;

import java.awt.*;

public class SquarePacketRenderer implements PacketRenderer<SquarePacket> {
    @Override
    public void paint(SquarePacket p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int s = (int) Math.round(10 * scale);
        int x = (int) p.getX();
        int y = (int) p.getY() + 5;
        g2.setColor(mismatch ? new Color(255,165,0) : Color.GREEN);
        g2.fillRect(x, y, s, s);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, Math.max(8, (int)(10*scale))));
        g2.drawString(String.valueOf(p.getId()), x, y - 2);
    }
}
