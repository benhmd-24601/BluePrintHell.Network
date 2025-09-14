package org.example.view.render.packets;

import java.awt.*;
import org.example.model.Packet.*;

public final class MiniPacketPainter {
    private MiniPacketPainter(){}

    /** یک آیکون 12×12 از نوع پکت رسم می‌کند (بدون اتکا به مختصات جهانی پکت) */
    public static void drawMini(Graphics2D g, Packet p, int x, int y) {
        int s = 12;
        Color oldC = g.getColor();
        Stroke oldS = g.getStroke();
        Font  oldF = g.getFont();
        try {
            if (p instanceof SquarePacket) {
                g.setColor(Color.GREEN);
                g.fillRect(x, y, s, s);

            } else if (p instanceof TrianglePacket) {
                g.setColor(Color.RED);
                int[] xs = {x, x + s, x + s/2};
                int[] ys = {y + s, y + s, y};
                g.fillPolygon(xs, ys, 3);

            } else if (p instanceof CirclePacket) {
                g.setColor(new Color(100,200,255));
                g.fillOval(x, y, s, s);

            } else if (p instanceof SecretPacket1) {
                g.setColor(new Color(120,160,255));
                int[] xs = {x + s/2, x + s, x + s/2, x};
                int[] ys = {y, y + s/2, y + s, y + s/2};
                g.fillPolygon(xs, ys, 4);

            } else if (p instanceof SecretPacket2) {
                g.setColor(new Color(200,120,255));
                int[] xs = {x + s/2, x + s, x + s/2, x};
                int[] ys = {y, y + s/2, y + s, y + s/2};
                g.fillPolygon(xs, ys, 4);
                g.setColor(Color.WHITE);
                g.drawLine(x+3, y+s/2, x+s-3, y+s/2);

            } else if (p instanceof ProtectedPacket) {
                g.setColor(new Color(160,160,160));
                g.fillRoundRect(x, y, s, s, 4, 4);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(x+3, y+5, s-6, s-6, 3, 3);
                g.drawArc(x+3, y+1, s-6, s-6, 200, 140);

            } else if (p instanceof HeavyPacket8) {
                g.setColor(new Color(255,140,0));
                g.fillRect(x, y, s, s);
                g.setColor(Color.BLACK);
                g.setFont(oldF.deriveFont(9f));
                g.drawString("8", x + s/3, y + s - 3);

            } else if (p instanceof HeavyPacket10) {
                g.setColor(new Color(255,100,0));
                g.fillRect(x, y, s, s);
                g.setColor(Color.BLACK);
                g.setFont(oldF.deriveFont(9f));
                g.drawString("10", x + 1, y + s - 3);

            } else if (p instanceof BitPacket) {
                g.setColor(Color.WHITE);
                g.fillOval(x + s/3, y + s/3, s/3, s/3);

            } else if (p instanceof TrojanPacket) {
                g.setColor(new Color(220, 20, 60));
                g.setStroke(new BasicStroke(2f));
                g.drawRect(x+1, y+1, s-2, s-2);
                g.setFont(oldF.deriveFont(Font.BOLD, 9f));
                g.drawString("T", x + s/3, y + s - 3);

            } else {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x, y, s, s);
            }
        } finally {
            g.setColor(oldC);
            g.setStroke(oldS);
            g.setFont(oldF);
        }
    }
}
