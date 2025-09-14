package org.example.view;

import org.example.controller.GameLoop;
import org.example.model.GameEnv;
import org.example.model.Packet.*;
import org.example.model.Systems.*;
import org.example.view.render.*;
import org.example.view.render.packets.*;
import org.example.view.render.systems.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameView extends JPanel {
    private final GameEnv env;
    private GameLoop gameLoop;

    private final RendererRegistry registry = new RendererRegistry();
    private final List<LayerRenderer> layers = new ArrayList<>();

    private Runnable onBackToMenu; // اگر لازم شد

    public GameView(GameEnv env) {
        this.env = env;
        setLayout(null);
        setDoubleBuffered(true);

        // --- ثبت Packet Renderers
        registry.registerPacketRenderer(Packet.class,            new DefaultPacketRenderer());
        registry.registerPacketRenderer(SquarePacket.class,      new SquarePacketRenderer());
        registry.registerPacketRenderer(TrianglePacket.class,    new TrianglePacketRenderer());
        registry.registerPacketRenderer(CirclePacket.class,      new CirclePacketRenderer());
        registry.registerPacketRenderer(BitPacket.class,         new BitPacketRenderer());
        registry.registerPacketRenderer(HeavyPacket8.class,      new HeavyPacket8Renderer());
        registry.registerPacketRenderer(HeavyPacket10.class,     new HeavyPacket10Renderer());
        registry.registerPacketRenderer(ProtectedPacket.class,   new ProtectedPacketRenderer());
        registry.registerPacketRenderer(TrojanPacket.class,      new TrojanPacketRenderer());
        registry.registerPacketRenderer(SecretPacket1.class,     new SecretPacket1Renderer());
        registry.registerPacketRenderer(SecretPacket2.class,     new SecretPacket2Renderer());

        // --- ثبت System Renderers
        registry.registerSystemRenderer(NetworkSystem.class,      new BaseSystemRenderer(registry)); // default
        registry.registerSystemRenderer(SourceSystem.class,       new SourceSystemRenderer(registry));
        registry.registerSystemRenderer(SinkSystem.class,         new SinkSystemRenderer(registry));
        registry.registerSystemRenderer(VPNSystem.class,          new VPNSystemRenderer(registry));
        registry.registerSystemRenderer(AntiTrojanSystem.class,   new AntiTrojanSystemRenderer(registry));
        registry.registerSystemRenderer(SaboteurSystem.class,     new SaboteurSystemRenderer(registry));
        registry.registerSystemRenderer(SpySystem.class,          new SpySystemRenderer(registry));
        registry.registerSystemRenderer(DistributorSystem.class,  new DistributorSystemRenderer(registry));
        registry.registerSystemRenderer(MergerSystem.class,       new MergerSystemRenderer(registry));

        // --- لایه‌ها
        layers.add(new BackgroundRenderer());
        layers.add(new WiresRenderer());
        layers.add(new SystemsRenderer(registry));
        layers.add(new PacketsRenderer(registry));
        layers.add(new HUDRenderer());
        layers.sort(Comparator.comparingInt(LayerRenderer::zIndex));
    }




    public void setOnBackToMenu(Runnable r) { this.onBackToMenu = r; }
    public void setGameLoop(GameLoop loop) { this.gameLoop = loop; }

    public void refresh() {
        repaint();
        revalidate();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        RenderContext ctx = new RenderContext(env, this);

        Graphics2D root = (Graphics2D) g.create();
        root.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (LayerRenderer lr : layers) {
            Graphics2D lg = (Graphics2D) root.create(); // ← هر لایه یک G مستقل
            try {
                lr.paint(lg, ctx);
            } finally {
                lg.dispose();
            }
        }
        root.dispose();
    }


    // اختیاری: افزودن/حذف UI سفارشی توسط کنترلر
    public void addCustomComponent(Component c) { add(c); }
    public void removeCustomComponent(Component c) { remove(c); }
}
