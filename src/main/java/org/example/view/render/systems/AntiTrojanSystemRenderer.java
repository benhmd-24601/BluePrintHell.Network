package org.example.view.render.systems;

import org.example.model.Systems.AntiTrojanSystem;
import org.example.view.render.*;

import java.awt.*;

public class AntiTrojanSystemRenderer extends BaseSystemRenderer<AntiTrojanSystem> {
    public AntiTrojanSystemRenderer(RendererRegistry reg) { super(reg); }

    @Override
    public void paint(AntiTrojanSystem s, Graphics2D g2, RenderContext ctx) {
        super.paint(s, g2, ctx);

        int cx = (int) s.getX() + 60;
        int cy = (int) s.getY() + 22;
        int R  = (int) org.example.model.ModelConfig.ANTITROJAN_RADIUS;

        Composite oldC = g2.getComposite();
        Stroke oldS = g2.getStroke();

        // پر کردن هاله‌ی شعاع
        g2.setComposite(AlphaComposite.SrcOver.derive(s.isEnabled() ? 0.10f : 0.06f));
        g2.setColor(s.isEnabled() ? new Color(0, 255, 180) : new Color(160,160,160));
        g2.fillOval(cx - R, cy - R, 2*R, 2*R);

        // قاب نقطه‌چین
        g2.setComposite(AlphaComposite.SrcOver);
        float[] dash = {6f, 6f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g2.setColor(s.isEnabled() ? new Color(0, 255, 180, 200) : new Color(160,160,160,160));
        g2.drawOval(cx - R, cy - R, 2*R, 2*R);

        g2.setStroke(oldS);
        g2.setComposite(oldC);

        // لوگوی کوچک وسط (اختیاری)
        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("ANTI-T", cx - 18, cy + 4);
    }
}
