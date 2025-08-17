package org.example.view;

import org.example.controller.GameLoop;
import org.example.model.GameEnv;
import org.example.view.render.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameView extends JPanel {
    private final GameEnv env;
    private GameLoop gameLoop;

    private final int hudHeight = 60;

    // لایه‌ها (به ترتیب zIndex)
    private final List<LayerRenderer> layers = new ArrayList<>();

    // callback بازگشت به منو (در صورت نیاز)
    private Runnable onBackToMenu;

    public GameView(GameEnv env) {
        this.env = env;
        setLayout(null);
        setDoubleBuffered(true);

        // ثبت لایه‌ها
        layers.add(new BackgroundRenderer());
        layers.add(new WiresRenderer());
        layers.add(new SystemsRenderer());
        layers.add(new PacketsRenderer());
        layers.add(new HUDRenderer());

        layers.sort(Comparator.comparingInt(LayerRenderer::zIndex));
    }

    public void setOnBackToMenu(Runnable r) { this.onBackToMenu = r; }
    public void setGameLoop(GameLoop loop) { this.gameLoop = loop; }

    // برای کنترلر: افزودن/حذف کامپوننت UI
    public void addCustomComponent(Component c) { add(c); }
    public void removeCustomComponent(Component c) { remove(c); }

    public void refresh() {
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        RenderContext ctx = new RenderContext(env, this, hudHeight);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (LayerRenderer layer : layers) {
            layer.paint(g2, ctx);
        }
        g2.dispose();
    }
}
