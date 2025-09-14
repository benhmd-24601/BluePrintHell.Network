package org.example.view.render;

import java.awt.Graphics2D;

public interface LayerRenderer {
    int zIndex(); // ترتیب لایه
    void paint(Graphics2D g2, RenderContext ctx);
}
