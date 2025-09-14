package org.example.view.render.packets;

import org.example.model.Packet.Packet;
import org.example.view.render.RenderContext;

import java.awt.Graphics2D;

public interface PacketRenderer<T extends Packet> {
    void paint(T packet, Graphics2D g2, RenderContext ctx, boolean mismatch, double scale);
}
