package org.example.view.render.systems;

import org.example.model.Systems.SpySystem;
import org.example.view.render.*;

import java.awt.*;

public class SpySystemRenderer extends BaseSystemRenderer<SpySystem> {
    public SpySystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(SpySystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        int cx = (int) s.getX() + 60, cy = (int) s.getY() + 22;
        g2.setColor(new Color(180, 255, 180));
        g2.drawOval(cx - 14, cy - 14, 28, 28);
        g2.drawLine(cx - 8, cy, cx + 8, cy);
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("SPY", cx - 10, cy + 22);
        //paintStorage(g2 ,s);
    }
}
