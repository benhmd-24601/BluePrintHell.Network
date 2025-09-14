package org.example.view.render;

import org.example.model.Wire;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class WiresRenderer implements LayerRenderer {
    @Override public int zIndex() { return 10; }

    @Override public void paint(Graphics2D g2, RenderContext ctx) {
        List<Wire> wires = ctx.getEnv().getWires();
        g2.setStroke(new BasicStroke(2f));
        boolean over = ctx.getEnv().isOverBudget();
        for (Wire wire : wires) {
            int x1 = (int) wire.getStartx();
            int y1 = (int) wire.getStarty();
            int x2 = (int) wire.getEndX();
            int y2 = (int) wire.getEndY();
            if (over) g2.setColor(Color.RED);
            else g2.setColor(Objects.equals(wire.getStartPortType(), "square") ? Color.GREEN : Color.YELLOW);
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}
