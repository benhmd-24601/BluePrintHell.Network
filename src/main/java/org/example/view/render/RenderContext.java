package org.example.view.render;

import org.example.model.GameEnv;

import java.awt.*;

public class RenderContext {
    private final GameEnv env;
    private final Component surface;

    public RenderContext(GameEnv env, Component surface) {
        this.env = env;
        this.surface = surface;
    }

    public GameEnv getEnv() { return env; }
    public int getWidth() { return surface.getWidth(); }
    public int getHeight() { return surface.getHeight(); }
}
