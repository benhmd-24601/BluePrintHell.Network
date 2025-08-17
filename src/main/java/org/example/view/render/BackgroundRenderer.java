package org.example.view.render;

import java.awt.*;

public class BackgroundRenderer implements LayerRenderer {
    private static final int SQUARE = 40;
    private static final int BOTTOM_BAR_HEIGHT = 120;

    @Override public int zIndex() { return 0; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        int w = ctx.getWidth(), h = ctx.getHeight();

        // پس‌زمینه شطرنجی
        for (int y = 0; y < h; y += SQUARE) {
            for (int x = 0; x < w; x += SQUARE) {
                boolean dark = ((x / SQUARE) + (y / SQUARE)) % 2 == 0;
                g2.setColor(dark ? new Color(40, 40, 40) : new Color(60, 60, 60));
                g2.fillRect(x, y, SQUARE, SQUARE);
            }
        }

        // نوار پایین
        g2.setColor(Color.BLACK);
        g2.fillRect(0, h - BOTTOM_BAR_HEIGHT, w, BOTTOM_BAR_HEIGHT);
    }
}
