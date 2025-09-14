package org.example.view.render.systems;

import org.example.model.Systems.SinkSystem;
import org.example.view.render.*;

import java.awt.*;

public class SinkSystemRenderer extends BaseSystemRenderer<SinkSystem> {
    public SinkSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(SinkSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("SINK", (int)s.getX() + 45, (int)s.getY() + 22);
       // paintStorage(g2 ,s);
    }
}
