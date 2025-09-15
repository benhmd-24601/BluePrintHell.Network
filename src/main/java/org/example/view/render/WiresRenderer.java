package org.example.view.render;

import org.example.model.Wire;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.List;

public class WiresRenderer implements LayerRenderer {

    @Override
    public int zIndex() {
        return 10;
    }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        List<Wire> wires = ctx.getEnv().getWires();
        g2.setStroke(new BasicStroke(2f));
        boolean over = ctx.getEnv().isOverBudget();

        for (Wire w : wires) {
            // رنگ سیم
            if (over) g2.setColor(Color.RED);
            else {
                switch (w.getStartPortType()) {
                    case "square" -> g2.setColor(Color.GREEN);
                    case "triangle" -> g2.setColor(Color.YELLOW);
                    case "circle" -> g2.setColor(Color.BLACK);
                    default -> g2.setColor(Color.LIGHT_GRAY);
                }
            }

            double sx = w.getStartx(), sy = w.getStarty();
            double ex = w.getEndX(),  ey = w.getEndY();

            if (w.hasControlPoint()) {
                // کنترل‌پوینت واقعی از روی Anchor
                Point2D.Double C = w.getQuadraticControlFromAnchor();
                QuadCurve2D.Double qc = new QuadCurve2D.Double(sx, sy, C.x, C.y, ex, ey);
                g2.draw(qc);

                // دکمه‌ی درگ (روی خود مسیر = Anchor)
                Point2D.Double A = w.getControlPoint();
                int r = 6;
                Shape knob = new Ellipse2D.Double(A.x - r, A.y - r, 2 * r, 2 * r);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fill(knob);
                g2.setColor(Color.DARK_GRAY);
                g2.draw(knob);
            } else {
                g2.drawLine((int) sx, (int) sy, (int) ex, (int) ey);
            }
        }
    }
}
