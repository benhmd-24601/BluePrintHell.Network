package org.example.view.render.systems;

import org.example.model.Systems.AntiTrojanSystem;
import org.example.view.render.*;

import java.awt.*;

public class AntiTrojanSystemRenderer extends BaseSystemRenderer<AntiTrojanSystem> {
    public AntiTrojanSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(AntiTrojanSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        int cx = (int) s.getX() + 60, cy = (int) s.getY() + 22;
        g2.setColor(new Color(255, 120, 120));
        g2.drawOval(cx - 16, cy - 16, 32, 32);
        g2.drawOval(cx - 10, cy - 10, 20, 20);
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("ANTI-T", cx - 18, cy + 4);
        //paintStorage(g2 ,s);
    }
}
