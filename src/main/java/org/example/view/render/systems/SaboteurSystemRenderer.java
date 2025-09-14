package org.example.view.render.systems;

import org.example.model.Systems.SaboteurSystem;
import org.example.view.render.*;

import java.awt.*;

public class SaboteurSystemRenderer extends BaseSystemRenderer<SaboteurSystem> {
    public SaboteurSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(SaboteurSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        int x = (int) s.getX() + 40, y = (int) s.getY() + 10;
        g2.setColor(Color.ORANGE);
        Polygon tri = new Polygon();
        tri.addPoint(x + 20, y);
        tri.addPoint(x, y + 20);
        tri.addPoint(x + 40, y + 20);
        g2.fillPolygon(tri);
        g2.setColor(Color.BLACK);
        g2.drawString("!", x + 18, y + 16);
        //paintStorage(g2 ,s);
    }
}
