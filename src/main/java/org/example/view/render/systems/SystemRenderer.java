package org.example.view.render.systems;

import org.example.model.Systems.NetworkSystem;
import org.example.view.render.RenderContext;

import java.awt.Graphics2D;

public interface SystemRenderer<T extends NetworkSystem> {
    void paint(T system, Graphics2D g2, RenderContext ctx);
}
