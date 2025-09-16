package org.example.model;

import org.example.model.Systems.*;

import java.util.ArrayList;
import java.util.List;

public class StageFactory {

    // =========================
    // Level 1
    // - 1x Source
    // - 2x NetworkSystem
    // - 1x Sink
    // =========================
    public static Stage createStage1() {
        List<NetworkSystem> systems = new ArrayList<>();

        systems.add(new SourceSystem(100, 400, 1, 5, 1));
        systems.add(new NetworkSystem(500, 250, 5));
        systems.add(new NetworkSystem(500, 550, 6));
        systems.add(new SinkSystem(950, 400, 8));

        // wire length, ???, goalPercent (همان مقادیری که قبلاً استفاده می‌کردی)
        return new Stage(systems, 10000, 120, 50.0);
    }

    // =========================
    // Level 2
    // - همان الگوی Level 1 +
    // - 1x SaboteurSystem (سیستم خرابکار)
    // =========================
    public static Stage createStage2() {
        List<NetworkSystem> systems = new ArrayList<>();

        systems.add(new SourceSystem(100, 380, 1, 5, 1));
        systems.add(new NetworkSystem(430, 240, 5));
        systems.add(new NetworkSystem(430, 520, 6));
        systems.add(new SinkSystem(900, 380, 8));

        // سیستم خرابکار (نام کلاس را مطابق پیاده‌سازی‌ خودت گذاشتم)
        systems.add(new SaboteurSystem(700, 300));

        return new Stage(systems, 11000, 140, 50.0);
    }

    // =========================
    // Level 3
    // - 1x Source
    // - 1x NetworkSystem
    // - 2x SpySystem (جاوسوس)
    // - 1x Sink
    // =========================
    public static Stage createStage3() {
        List<NetworkSystem> systems = new ArrayList<>();

        systems.add(new SourceSystem(120, 420, 1, 5, 1));
        systems.add(new NetworkSystem(520, 420, 5));
        systems.add(new SinkSystem(900, 420, 8));

        // دو جاسوس
        systems.add(new SpySystem(350, 300));
        systems.add(new SpySystem(700, 520));

        return new Stage(systems, 12000, 150, 50.0);
    }

    // =========================
    // Level 4
    // - الگوی Level 2 +
    // - 1x VPNSystem
    // - 1x TrojanSystem
    // =========================
    public static Stage createStage4() {
        List<NetworkSystem> systems = new ArrayList<>();

        systems.add(new SourceSystem(100, 360, 1, 5, 1));
        systems.add(new NetworkSystem(420, 230, 5));
        systems.add(new NetworkSystem(420, 520, 6));
        systems.add(new SinkSystem(910, 360, 8));

        // خرابکار + VPN + تروجان
        systems.add(new SaboteurSystem(650, 260));
        systems.add(new VPNSystem(680, 460));
        systems.add(new AntiTrojanSystem(300, 560));

        return new Stage(systems, 13500, 170, 50.0);
    }

    // =========================
    // Level 5
    // - 1x Source
    // - 2x NetworkSystem
    // - 1x MergerSystem
    // - 1x DistributorSystem
    // - 1x Sink
    // =========================
    public static Stage createStage5() {
        List<NetworkSystem> systems = new ArrayList<>();

        systems.add(new SourceSystem(120, 420, 1, 5, 1));

        systems.add(new NetworkSystem(420, 280, 5));
        systems.add(new NetworkSystem(420, 560, 6));

        // مرج و دیستریبیوتر (این‌ها معمولاً فقط (x,y) می‌گیرند)
        systems.add(new MergerSystem(650, 420));
        systems.add(new DistributorSystem(800, 420));

        systems.add(new SinkSystem(1000, 420, 8));

        return new Stage(systems, 15000, 200, 50.0);
    }
}
