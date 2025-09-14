package org.example.view.render;

import java.awt.*;

public class BackgroundRenderer implements LayerRenderer {
    @Override public int zIndex() { return 0; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        int w = ctx.getWidth(), h = ctx.getHeight();

        // checker
        int square = 40;
        for (int y = 0; y < h; y += square) {
            for (int x = 0; x < w; x += square) {
                g2.setColor(((x / square + y / square) % 2 == 0) ? new Color(40, 40, 40) : new Color(60, 60, 60));
                g2.fillRect(x, y, square, square);
            }
        }

        // bottom bar
        int barHeight = 120;
        g2.setColor(Color.BLACK);
        g2.fillRect(0, h - barHeight, w, barHeight);
    }
}
