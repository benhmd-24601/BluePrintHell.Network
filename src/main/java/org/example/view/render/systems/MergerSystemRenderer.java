package org.example.view.render.systems;

import org.example.model.Systems.MergerSystem;
import org.example.view.render.*;

import java.awt.*;

public class MergerSystemRenderer extends BaseSystemRenderer<MergerSystem> {
    public MergerSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(MergerSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.drawString("MERGE", (int)s.getX() + 60, (int)s.getY() + 17);
        //paintStorage(g2 ,s);
    }
}
