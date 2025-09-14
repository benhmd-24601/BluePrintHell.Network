package org.example.view.render.systems;

import org.example.model.Packet.Packet;
import org.example.model.Port;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Systems.SinkSystem;
import org.example.view.render.RenderContext;
import org.example.view.render.RendererRegistry;
import org.example.view.render.packets.MiniPacketPainter;
import org.example.view.render.systems.SystemRenderer;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class BaseSystemRenderer<T extends NetworkSystem> implements SystemRenderer<T> {
    protected final RendererRegistry registry;

    public BaseSystemRenderer(RendererRegistry registry) { this.registry = registry; }

    @Override
    public void paint(T system, Graphics2D g2, RenderContext ctx) {
        drawBox(g2, system);
        drawPorts(g2, system);
        drawIndicator(g2, system);
        drawStorageBadge(g2, system);

        drawStorage(g2, system);
//        paintStorage(g2,system);
    }
    /** نشان دادن تعداد پکت‌های انبار (یا received در Sink) به صورت badge بالای سیستم */
    protected void drawStorageBadge(Graphics2D g2, NetworkSystem s) {
        // 1) تعداد را بگیر
        int count = (s instanceof SinkSystem sink)
                ? sink.getReceivedPackets().size()
                : s.getPacketStorage().size();

        // اگر دوست نداری وقتی صفر است چیزی نشان بدهی، این شرط را نگه دار:
        // if (count == 0) return;

        // 2) متن و ابعاد
        String label = String.valueOf(count);
        Font oldF = g2.getFont();
        g2.setFont(new Font("Consolas", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        int pad = 6;
        int w = fm.stringWidth(label) + pad * 2;
        int h = fm.getAscent() + fm.getDescent() + 4;

        // 3) جایگاه: وسطِ بالای جعبه
        int boxX = (int) s.getX();
        int boxY = (int) s.getY();
        int boxW = 120; // همان عرضی که در drawBox استفاده می‌کنیم
        int x =( boxX + (boxW - w) / 2 ) + 10;
        int y = boxY - h - 6; // کمی بالاتر از جعبه

        // اگر خیلی بالا رفت (مثلاً سیستم نزدیک لبه‌ی بالا بود) کمی پایین‌ترش بیار
        if (y < 2) y = boxY + 2;

        // 4) پس‌زمینه‌ی نیمه‌شفاف + کادر + متن
        RoundRectangle2D rr = new RoundRectangle2D.Float(x, y, w, h, 10, 10);
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fill(rr);
        g2.setColor(Color.WHITE);
        g2.draw(rr);
        g2.drawString(label, x + pad, y + fm.getAscent() + 1);

        g2.setFont(oldF);
    }

    protected void drawBox(Graphics2D g2, NetworkSystem s) {
        int w = 120, h = 160;
        int x = (int) s.getX(), y = (int) s.getY();
        g2.setColor(Color.GRAY);
        g2.fillRect(x, y, w, h);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 5, y + 30, w - 10, h - 30);
    }

    protected void drawPorts(Graphics2D g2, NetworkSystem s) {
        int port = 15;
        for (Port p : s.getInputPorts()) {
            int px = (int) p.getX(), py = (int) p.getY();
            if ("square".equals(p.getType())) {
                g2.setColor(Color.GREEN);
                g2.fillRect(px, py, port, port);
            } else {
                g2.setColor(Color.YELLOW);
                Polygon tri = new Polygon();
                tri.addPoint(px, py + port/2);
                tri.addPoint(px + port, py);
                tri.addPoint(px + port, py + port);
                g2.fillPolygon(tri);
            }
        }
        for (Port p : s.getOutputPorts()) {
            int px = (int) p.getX(), py = (int) p.getY();
            if ("square".equals(p.getType())) {
                g2.setColor(Color.GREEN);
                g2.fillRect(px, py, port, port);
            } else {
                g2.setColor(Color.YELLOW);
                Polygon tri = new Polygon();
                tri.addPoint(px + port, py + port/2);
                tri.addPoint(px, py);
                tri.addPoint(px, py + port);
                g2.fillPolygon(tri);
            }
        }
    }

    protected void drawIndicator(Graphics2D g2, NetworkSystem s) {
        if (s.isSourceSystem()) return;
        int r = 12;
        int x = (int) s.getX() + 120 - r - 5;
        int y = (int) s.getY() + 5;
        g2.setColor(s.isIndicatorOn() ? Color.GREEN : Color.DARK_GRAY);
        g2.fillOval(x, y, r, r);
        g2.setColor(Color.BLACK);
        g2.drawOval(x, y, r, r);
    }

    protected void drawStorage(Graphics2D g2, NetworkSystem s) {
        List<Packet> list = (s instanceof SinkSystem sink) ? sink.getReceivedPackets() : s.getPacketStorage();
        int startX = (int) s.getX() + 20;
        int startY = (int) s.getY() + 40;
        int maxPerRow = 5, hGap = 15, vGap = 15;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        List<Packet> packets =
                (s instanceof SinkSystem sink) ? sink.getReceivedPackets()
                        : s.getPacketStorage();

        int cell = 14, gap = 4, cols = 6;


        // عنوان
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        String title = s.isSourceSystem() ? "Source" :
                (s instanceof SinkSystem ? "Sink" : "Storage");
        g2.drawString(title, startX + 8, startY );

        // آیکون‌ها
        for (int i = 0; i < packets.size(); i++) {
            Packet p = packets.get(i);
            int col = i % cols, row = i / cols;
            int x = startX + col * (cell + gap);
            int y = startY + row * (cell + gap);
            MiniPacketPainter.drawMini(g2, p, x, y);
        }

    }
    /** نمایش محتوای استوریج (یا received در Sink) به صورت آیکون‌های کوچک */
    protected void paintStorage(Graphics2D g, NetworkSystem sys) {

    }
}
