package org.example.view.render.systems;

import org.example.model.Systems.VPNSystem;
import org.example.view.render.*;

import java.awt.*;

public class VPNSystemRenderer extends BaseSystemRenderer<VPNSystem> {
    public VPNSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(VPNSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);
        int cx = (int) s.getX() + 60, cy = (int) s.getY() + 22;
        g2.setColor(new Color(120, 200, 255));
        g2.fillOval(cx - 16, cy - 10, 32, 20);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.drawString("VPN", cx - 12, cy + 4);

    }
}
