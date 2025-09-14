package org.example.view.render.packets;

import org.example.model.Packet.Packet;
import org.example.view.render.LayerRenderer;
import org.example.view.render.RenderContext;
import org.example.view.render.RendererRegistry;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class PacketsRenderer implements LayerRenderer {
    private final RendererRegistry registry;

    public PacketsRenderer(RendererRegistry registry) { this.registry = registry; }

    @Override public int zIndex() { return 30; }

    @Override
    public void paint(Graphics2D g2, RenderContext ctx) {
        for (Packet p : ctx.getEnv().getPackets()) {
            if (p == null || p.getWire() == null) continue;

            boolean mismatch = p.getPortKey() != null &&
                    !java.util.Objects.equals(p.getPortKey(), p.getWire().getStartPortType());

            Graphics2D gp = (Graphics2D) g2.create(); // ← هر پکت یک G مستقل
            try {
                registry.findPacketRenderer(p).paint(p, gp, ctx, mismatch, 1.0);
            } finally {
                gp.dispose();
            }
        }
    }

}
