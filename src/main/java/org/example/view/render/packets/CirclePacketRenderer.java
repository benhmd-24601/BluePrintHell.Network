package org.example.view.render.packets;

import org.example.model.Packet.CirclePacket;
import org.example.view.render.*;

import java.awt.*;

public class CirclePacketRenderer implements PacketRenderer<CirclePacket> {
    @Override
    public void paint(CirclePacket p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int s = (int) Math.round(10 * scale);
        int x = (int) p.getX();
        int y = (int) p.getY() + 5;
        g2.setColor(mismatch ? new Color(255,165,0) : new Color(220,220,255));
        g2.fillOval(x, y, s, s);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, Math.max(8, (int)(10*scale))));
        g2.drawString(String.valueOf(p.getId()), x, y - 2);
    }
}
