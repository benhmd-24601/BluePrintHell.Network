package org.example.view.render.packets;

import org.example.model.Packet.SecretPacket1;
import org.example.view.render.*;

import java.awt.*;

public class SecretPacket1Renderer implements PacketRenderer<SecretPacket1> {
    @Override
    public void paint(SecretPacket1 p, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale) {
        int s = (int) Math.round(12 * scale);
        int x = (int) p.getX() - s/2;
        int y = (int) p.getY() - s/2;

        g2.setColor(new Color(100, 150, 255));
        g2.fillOval(x, y, s, s);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, Math.max(8, (int)(10*scale))));
        g2.drawString("S1", x - 2, y + s + 10);
    }
}
