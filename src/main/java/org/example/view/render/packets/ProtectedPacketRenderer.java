package org.example.view.render.packets;

import org.example.model.Packet.ProtectedPacket;
import org.example.view.render.*;

import java.awt.*;

public class ProtectedPacketRenderer implements PacketRenderer<ProtectedPacket> {
    @Override
    public void paint(ProtectedPacket p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int s = (int) Math.round(12 * scale);
        int x = (int) p.getX() - s/2;
        int y = (int) p.getY() - s/2;

        g2.setColor(new Color(100, 200, 255, 120));
        g2.fillOval(x - 3, y - 3, s + 6, s + 6);

        g2.setColor(mismatch ? new Color(255,165,0) : Color.WHITE);
        g2.fillOval(x, y, s, s);

        // <-- اضافه: نمایش ID بالای پکت
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, Math.max(8, (int)(10*scale))));
        g2.drawString(String.valueOf(p.getId()), x, y - 2);
    }
}
