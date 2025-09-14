package org.example.view.render.systems;

import org.example.model.Systems.SourceSystem;
import org.example.view.render.*;

import java.awt.*;

public class SourceSystemRenderer extends BaseSystemRenderer<SourceSystem> {
    public SourceSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(SourceSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("SOURCE", (int)s.getX() + 28, (int)s.getY() + 22);
    }
}
