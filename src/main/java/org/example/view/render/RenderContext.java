package org.example.view.render;

import javax.swing.JComponent;
import org.example.model.GameEnv;

public class RenderContext {
    private final GameEnv env;
    private final JComponent surface;
    private final int hudHeight;

    public RenderContext(GameEnv env, JComponent surface, int hudHeight) {
        this.env = env;
        this.surface = surface;
        this.hudHeight = hudHeight;
    }

    public GameEnv getEnv() { return env; }
    public JComponent getSurface() { return surface; }
    public int getWidth() { return surface.getWidth(); }
    public int getHeight() { return surface.getHeight(); }
    public int getHudHeight() { return hudHeight; }
}
