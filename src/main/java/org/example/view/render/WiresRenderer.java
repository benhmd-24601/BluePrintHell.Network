package org.example.view.render;

import org.example.model.GameEnv;
import org.example.model.Port;
import org.example.model.Wire;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

public class WiresRenderer implements LayerRenderer {

    @Override
    public int zIndex() { return 10; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<Wire> wires = ctx.getEnv().getWires();
        g2.setStroke(new BasicStroke(2f));

        for (Wire w : wires) {
            boolean overBudget = ctx.getEnv().isOverBudget();
            boolean crossing   = w.crossesAnySystem(ctx.getEnv());

            if (overBudget || crossing) g2.setColor(Color.RED);
            else {
                switch (w.getStartPortType()) {
                    case "square"   -> g2.setColor(Color.GREEN);
                    case "triangle" -> g2.setColor(Color.YELLOW);
                    case "circle"   -> g2.setColor(Color.BLACK);
                    default         -> g2.setColor(Color.LIGHT_GRAY);
                }
            }

            if (w.getAnchorCount() == 0) {
                g2.drawLine((int)Math.round(w.getStartx()), (int)Math.round(w.getStarty()),
                        (int)Math.round(w.getEndX()),   (int)Math.round(w.getEndY()));
            } else {
                Path2D.Double path = new Path2D.Double();
                Point2D.Double P0 = new Point2D.Double(w.getStartx(), w.getStarty());
                path.moveTo(P0.x, P0.y);
                int segs = w.getSegmentCount();
                for (int i = 0; i < segs; i++) {
                    Wire.BezierSegment bz = w.getBezierForSegment(i);
                    path.curveTo(bz.c1.x, bz.c1.y, bz.c2.x, bz.c2.y, bz.p1.x, bz.p1.y);
                }
                g2.draw(path);
            }

            // knobs (anchors)
            int r = 6;
            for (int i = 0; i < w.getAnchorCount(); i++) {
                Point2D.Double A = w.getAnchor(i);
                Shape knob = new Ellipse2D.Double(A.x - r, A.y - r, 2 * r, 2 * r);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fill(knob);
                g2.setColor(Color.DARK_GRAY);
                g2.draw(knob);
            }
            // ===== NEW: میدان‌های فعال روی این سیم (Aergia / Eliphas)
            var fields = ctx.getEnv().getFieldsOnWire(w);
            for (GameEnv.WireField f : fields) {
                int rr = (int) Math.round(f.radius);

                // رنگ‌ها
                Color ring = (f.type == GameEnv.WireField.Type.AERGIA)
                        ? new Color(205, 120, 255, 200) // بنفش برای Aergia
                        : new Color( 80, 220, 255, 200); // فیروزه‌ای برای Eliphas

                // هاله‌ی ملایم داخل شعاع
                Color halo = new Color(ring.getRed(), ring.getGreen(), ring.getBlue(), 40);
                g2.setColor(halo);
                g2.fill(new Ellipse2D.Double(f.x - rr, f.y - rr, 2.0 * rr, 2.0 * rr));

                // حلقه‌ی نقطه‌چین دور شعاع
                Stroke old = g2.getStroke();
                g2.setStroke(new BasicStroke(
                        2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[]{6f, 6f}, 0f
                ));
                g2.setColor(ring);
                g2.draw(new Ellipse2D.Double(f.x - rr, f.y - rr, 2.0 * rr, 2.0 * rr));
                g2.setStroke(old);

                // نقطهٔ مرکزی
                g2.setColor(Color.WHITE);
                g2.fill(new Ellipse2D.Double(f.x - 3, f.y - 3, 6, 6));
                g2.setColor(ring.darker());
                g2.draw(new Ellipse2D.Double(f.x - 3, f.y - 3, 6, 6));

                // لیبل کوچک نوع فیلد (A/E)
                String label = (f.type == GameEnv.WireField.Type.AERGIA) ? "A" : "E";
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(label);
                int th = fm.getAscent();
                int lx = (int)Math.round(f.x) + 8;
                int ly = (int)Math.round(f.y) - 8;

                // پس‌زمینه‌ی نیمه‌شفاف برای لیبل
                g2.setColor(new Color(0,0,0,120));
                g2.fillRoundRect(lx - 4, ly - th, tw + 8, th + 6, 8, 8);

                g2.setColor(ring);
                g2.drawString(label, lx, ly);
            }

        }

        // ===== PREVIEW DRAW =====
        Port s = ctx.getEnv().getPreviewStartPort();
        Point mp = ctx.getEnv().getPreviewMousePoint();
        Port e  = ctx.getEnv().getPreviewEndPort();
        if (s != null && mp != null) {
            double sx = s.getCenterX(), sy = s.getCenterY();
            double ex = (e != null) ? e.getCenterX() : mp.x;
            double ey = (e != null) ? e.getCenterY() : mp.y;

            // رنگ بر اساس نوع پورت شروع (یا قرمز اگر طول از بودجه بیشتر است)
            Color base;
            switch (s.getType()) {
                case "square"   -> base = Color.GREEN;
                case "triangle" -> base = Color.YELLOW;
                case "circle"   -> base = Color.BLACK;
                default         -> base = Color.LIGHT_GRAY;
            }

            double len = Math.hypot(ex - sx, ey - sy);
            boolean over = ctx.getEnv().getRemainingWireLength() < len;
            g2.setColor(over ? Color.RED : base);

            Stroke old = g2.getStroke();
            float[] dash = {8f, 8f};
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g2.drawLine((int)Math.round(sx), (int)Math.round(sy), (int)Math.round(ex), (int)Math.round(ey));
            g2.setStroke(old);

            // نقطهٔ پایانی پریویو
            g2.setColor(new Color(0,0,0,60));
            g2.fillOval((int)Math.round(ex)-3, (int)Math.round(ey)-3, 6, 6);

            // اگر روی پورت سازگار هستیم، هالهٔ سبز/زرد
            if (e != null) {
                g2.setColor(new Color(0, 200, 0, 70));
                g2.fillOval((int)Math.round(ex)-9, (int)Math.round(ey)-9, 18, 18);
            }
        }
    }
}
