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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class BaseSystemRenderer<T extends NetworkSystem> implements SystemRenderer<T> {
    protected final RendererRegistry registry;

    public BaseSystemRenderer(RendererRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void paint(T system, Graphics2D g2, RenderContext ctx) {
        drawBox(g2, system);
        drawPorts(g2, system);
        drawBulbLampTopLeft(g2, system);

        drawIndicator(g2, system);
        drawStorageBadge(g2, system);
        drawCooldownTimer(g2, system);
        drawStorage(g2, system);
        drawToggleSwitchTopRight(g2, system);
    }

    // BaseSystemRenderer.java
    protected void drawCooldownTimer(Graphics2D g2, NetworkSystem s) {
        int panelX = (int) s.getX() + 10;
        int panelY = (int) s.getY() + 2;
        // زمان باقیمانده کول‌داون
        double rem = (s != null) ? s.getReenableTimerSec() : 0.0;
        boolean active = (rem > 0.0); // حتی اگر سیستم enabled شد، تا وقتی rem>0 نمایش فعال باشد

        // فونت/ابعاد ثابت تا عرض پنل نلرزد
        Font oldF = g2.getFont();
        Stroke oldSt = g2.getStroke();
        try {
            Font f = new Font("Consolas", Font.BOLD, 12);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();

            String proto = "88.8s";             // پهنا بر اساس حداکثر
            int padX = 6, padY = 4;
            int w = fm.stringWidth(proto) + padX * 2;
            int h = fm.getAscent() + fm.getDescent() + padY * 2;

            // رنگ‌های روشن/خاموش
            Color bgOn = new Color(5, 15, 5);
            Color frameOn = new Color(0, 120, 0);
            Color textOn = new Color(100, 255, 100);

            Color bgOff = new Color(12, 12, 12);
            Color frameOff = new Color(40, 40, 40);
            // متن خاموش: نمایش نمی‌دهیم تا حس «خاموش» بدهد

            // زمینه و قاب
            g2.setColor(active ? bgOn : bgOff);
            g2.fillRoundRect(panelX, panelY, w, h, 6, 6);
            g2.setColor(active ? frameOn : frameOff);
            g2.drawRoundRect(panelX, panelY, w, h, 6, 6);

            // اگر فعاله، متن سبز را بکش
            if (active) {
                String text = String.format("%.1fs", rem);
                int tx = panelX + padX;
                int ty = panelY + padY + fm.getAscent();
                g2.setColor(textOn);
                g2.drawString(text, tx, ty);
            }
        } finally {
            g2.setFont(oldF);
            g2.setStroke(oldSt);
        }
    }


    protected void drawBulbLampTopLeft(Graphics2D g2, NetworkSystem s) {
        int boxX = (int) s.getX();
        int boxY = (int) s.getY();

        // --- پایه‌ی متصل به بدنه (بالا-چپِ جعبه)
        int plateW = 6, plateH = 16;
        int plateX = boxX - plateW + 2;  // 2px داخل بدنه تا «چسبیده» دیده شود
        int plateY = boxY + 2;

        Color steel = new Color(70, 70, 70);
        Color steelDark = new Color(45, 45, 45);

        g2.setColor(steel);
        g2.fillRoundRect(plateX, plateY, plateW, plateH, 3, 3);
        g2.setColor(steelDark);
        g2.drawRoundRect(plateX, plateY, plateW, plateH, 3, 3);

        // --- بازوی اتصال (کوتاه، کمی قوسی) از پایه تا سوکت
        Stroke oldSt = g2.getStroke();
        Composite oldCp = g2.getComposite();
        Paint oldPt = g2.getPaint();

        int armStartX = plateX + plateW - 1;     // لبهٔ راستِ پایه (چسبیده به جعبه)
        int armStartY = plateY + 3;
        // جای حباب (بیرونِ جعبه، بالا-چپ)
        int bulbCx = boxX - 22;
        int bulbCy = boxY - 10;

        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(steel);
        QuadCurve2D.Double arm = new QuadCurve2D.Double(
                armStartX, armStartY,
                boxX - 10, boxY - 4,     // نقطهٔ کنترل (بالای گوشهٔ چپ-بالا)
                bulbCx, bulbCy + 4       // انتهای بازو کنار سوکت
        );
        g2.draw(arm);

        // --- سوکت
        int socketW = 12, socketH = 7;
        int socketX = bulbCx - socketW / 2;
        int socketY = bulbCy + 1;
        g2.setColor(new Color(50, 50, 50));
        g2.fillRoundRect(socketX, socketY, socketW, socketH, 3, 3);
        g2.setColor(steelDark);
        g2.drawRoundRect(socketX, socketY, socketW, socketH, 3, 3);

        // --- حباب (دایره‌ای)
        int r = 10;
        Ellipse2D.Double bulb = new Ellipse2D.Double(bulbCx - r, bulbCy - r, 2 * r, 2 * r);
        boolean on = s.isEnabled();  // روشن/خاموش بودن از وضعیت سیستم

        if (on) {
            // گرادیان داخل حباب
            RadialGradientPaint rgp = new RadialGradientPaint(
                    new Point2D.Double(bulbCx, bulbCy + 2),
                    r + 6f,
                    new float[]{0f, 0.6f, 1f},
                    new Color[]{
                            new Color(255, 236, 180, 230),
                            new Color(255, 210, 110, 110),
                            new Color(255, 210, 110, 0)
                    }
            );
            g2.setPaint(rgp);
            g2.fill(bulb);

            // هالهٔ نور لطیف
            g2.setComposite(AlphaComposite.SrcOver.derive(0.22f));
            g2.setColor(new Color(255, 210, 110));
            g2.fillOval(bulbCx - (r + 8), bulbCy - (r + 6), (r + 8) * 2, (r + 8) * 2);
            g2.setComposite(AlphaComposite.SrcOver.derive(0.12f));
            g2.fillOval(bulbCx - (r + 14), bulbCy - (r + 12), (r + 14) * 2, (r + 14) * 2);
            g2.setComposite(oldCp);
        } else {
            // خاموش: حباب مات
            g2.setColor(new Color(200, 200, 200, 70));
            g2.fill(bulb);
        }

        // لبهٔ حباب
        g2.setPaint(oldPt);
        g2.setColor(new Color(220, 220, 220));
        g2.draw(bulb);

        // فیلامان ساده داخل حباب (برای حس «حبابی»)
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(on ? new Color(255, 170, 40) : new Color(110, 95, 70));
        int fx = bulbCx - 5, fy = bulbCy;
        g2.drawLine(fx, fy, fx + 10, fy);
        g2.drawArc(bulbCx - 6, bulbCy - 4, 12, 8, 200, 140);

        g2.setStroke(oldSt);
        g2.setComposite(oldCp);
    }

    private static final int BOX_W = 120;
    private static final int INDICATOR_R = 12;
    private static final int INDICATOR_MARGIN = 5;

    private static final int TOGGLE_SIZE = 14;
    private static final int TOGGLE_GAP_Y = 4;   // فاصله از زیرِ اندیکاتور
    private static final int TOGGLE_SHIFT_X = 4; // کمی چپ‌تر از لبه راست

    protected void drawToggleSwitchTopRight(Graphics2D g2, NetworkSystem s) {
        int x = (int) s.getX() + BOX_W - TOGGLE_SIZE - INDICATOR_MARGIN - TOGGLE_SHIFT_X;
        int y = (int) s.getY() + INDICATOR_MARGIN + INDICATOR_R + TOGGLE_GAP_Y;

        boolean enabled = s.isEnabled();

        // پس‌زمینه + قاب
        g2.setColor(enabled ? new Color(70, 70, 70) : new Color(150, 35, 35));
        g2.fillRect(x, y, TOGGLE_SIZE, TOGGLE_SIZE);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, TOGGLE_SIZE, TOGGLE_SIZE);

        // آیکن داخل: روشن=نماد پاور، خاموش=X
        if (enabled) {
            g2.setColor(Color.WHITE);
            int cx = x + TOGGLE_SIZE / 2, cy = y + TOGGLE_SIZE / 2;
            int r = TOGGLE_SIZE / 2 - 3;
            g2.drawOval(cx - r, cy - r, 2 * r, 2 * r);
            g2.drawLine(cx, cy - r + 2, cx, cy + r - 2);
        } else {
            g2.setColor(Color.WHITE);
            g2.drawLine(x + 3, y + 3, x + TOGGLE_SIZE - 3, y + TOGGLE_SIZE - 3);
            g2.drawLine(x + TOGGLE_SIZE - 3, y + 3, x + 3, y + TOGGLE_SIZE - 3);
        }
    }


    /**
     * لامپ وضعیت فعال/غیرفعال سیستم در گوشه‌ی بالا-چپ
     */
    protected void drawEnabledLamp(Graphics2D g2, NetworkSystem s) {
        int r = 12;
        int x = (int) s.getX() + 5;
        int y = (int) s.getY() + 5;

        // رنگ: روشن (سبز) اگر enabled، خاموش (خاکستری تیره) اگر disabled
        Color fill = s.isEnabled() ? new Color(0, 210, 110) : new Color(80, 80, 80);
        g2.setColor(fill);
        g2.fillOval(x, y, r, r);

        // هاله‌ی خیلی لطیف وقتی روشن است
        if (s.isEnabled()) {
            g2.setColor(new Color(0, 255, 140, 120));
            g2.fillOval(x - 3, y - 3, r + 6, r + 6);
        }

        // کادر ظریف
        g2.setColor(Color.BLACK);
        g2.drawOval(x, y, r, r);
    }

    /**
     * نشان دادن تعداد پکت‌های انبار (یا received در Sink) به صورت badge بالای سیستم
     */
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
        int x = (boxX + (boxW - w) / 2) + 10;
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
        int w = 120, h = 150;
        int x = (int) s.getX(), y = (int) s.getY();
        g2.setColor(Color.GRAY);
        g2.fillRect(x, y, w, h);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 5, y + 50, w - 10, h - 60);
    }

    protected void drawPorts(Graphics2D g2, NetworkSystem s) {
        int port = 15;
        for (Port p : s.getInputPorts()) {
            int px = (int) p.getX(), py = (int) p.getY();
            if ("square".equals(p.getType())) {
                g2.setColor(Color.GREEN);
                g2.fillRect(px, py, port, port);
            } else if ("triangle".equals(p.getType())) {
                g2.setColor(Color.YELLOW);
                Polygon tri = new Polygon();
                tri.addPoint(px, py + port / 2);
                tri.addPoint(px + port, py);
                tri.addPoint(px + port, py + port);
                g2.fillPolygon(tri);
            } else {
                g2.setColor(Color.BLACK);
                g2.fillOval(px, py, port, port);
            }

        }
        for (Port p : s.getOutputPorts()) {
            int px = (int) p.getX(), py = (int) p.getY();
            if ("square".equals(p.getType())) {
                g2.setColor(Color.GREEN);
                g2.fillRect(px, py, port, port);
            } else if ("triangle".equals(p.getType())) {
                g2.setColor(Color.YELLOW);
                Polygon tri = new Polygon();
                tri.addPoint(px + port, py + port / 2);
                tri.addPoint(px, py);
                tri.addPoint(px, py + port);
                g2.fillPolygon(tri);
            } else {
                g2.setColor(Color.black);
                g2.fillOval(px, py, port, port);
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
        int startY = (int) s.getY() + 70;
        int maxPerRow = 5, hGap = 15, vGap = 15;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 12));
        List<Packet> packets =
                (s instanceof SinkSystem sink) ? sink.getReceivedPackets()
                        : s.getPacketStorage();

        int cell = 14, gap = 4, cols = 5;


        // عنوان
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Consolas", Font.BOLD, 10));
        String title = "Storage";
        g2.drawString(title, startX - 8, startY - 25);

        // آیکون‌ها
        for (int i = 0; i < packets.size(); i++) {
            Packet p = packets.get(i);
            int col = i % cols, row = i / cols;
            int x = startX + col * (cell + gap);
            int y = startY + row * (cell + gap);
            MiniPacketPainter.drawMini(g2, p, x, y);
        }

    }

    /**
     * نمایش محتوای استوریج (یا received در Sink) به صورت آیکون‌های کوچک
     */
    protected void paintStorage(Graphics2D g, NetworkSystem sys) {

    }
}
