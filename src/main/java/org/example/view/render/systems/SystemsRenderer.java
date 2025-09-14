    package org.example.view.render.systems;

    import org.example.model.Systems.NetworkSystem;
    import org.example.view.render.LayerRenderer;
    import org.example.view.render.RenderContext;
    import org.example.view.render.RendererRegistry;

    import java.awt.*;
    import java.util.List;

    public class SystemsRenderer implements LayerRenderer {
        private final RendererRegistry registry;

        public SystemsRenderer(RendererRegistry registry) {
            this.registry = registry;
        }

        @Override public int zIndex() { return 20; }


        @Override
        public void paint(Graphics2D g2, RenderContext ctx) {
            for (NetworkSystem s : ctx.getEnv().getSystems()) {
                Graphics2D gs = (Graphics2D) g2.create(); // ← هر سیستم یک G مستقل
                try {
                    registry.findSystemRenderer(s).paint(s, gs, ctx);
                } finally {
                    gs.dispose();
                }
            }
        }

    }
