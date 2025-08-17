package org.example.view.render;

import java.awt.Graphics2D;

public interface LayerRenderer {
    /** عدد کوچکتر یعنی زیرین‌تر (اختیاری، اگر خواستی خودت مرتب‌سازی کنی می‌تونی حذفش کنی) */
    default int zIndex() { return 0; }
    void paint(Graphics2D g2, RenderContext ctx);
}
