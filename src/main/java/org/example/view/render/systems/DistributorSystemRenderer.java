package org.example.view.render.systems;

import org.example.model.Systems.DistributorSystem;
import org.example.view.render.*;

import java.awt.*;

public class DistributorSystemRenderer extends BaseSystemRenderer<DistributorSystem> {
    public DistributorSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(DistributorSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.drawString("DIST", (int)s.getX() + 44, (int)s.getY() + 22);
        //paintStorage(g2 ,s);
    }
}
