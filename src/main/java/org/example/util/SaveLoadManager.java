package org.example.util;

import org.example.model.GameEnv;
import org.example.model.Port;
import org.example.model.Wire;
import org.example.model.Systems.SourceSystem;

import java.awt.geom.Point2D;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Save/Load manager:
 * - نگهداری پیشرفت مراحل (آنلاک مرحلهٔ بعد)
 * - ذخیرهٔ لحظه‌ای وضعیت یک مرحله (AutoSave) در فایل جداگانهٔ همان مرحله
 *
 * Autosave روی ترد جدا انجام می‌شود.
 */
public class SaveLoadManager {

    // ===== مسیرها =====
    private static final Path SAVES_DIR = Path.of("saves");
    private static final String PROGRESS_FILE = "progress.dat";
    private static boolean[] passedLevels = new boolean[0];

    // ===== Autosave (non-blocking) =====
    private static volatile long lastAutosaveAt = 0L;
    private static long AUTOSAVE_INTERVAL_MS = 2_000L;
    private static final AtomicBoolean saving = new AtomicBoolean(false);
    private static final ExecutorService IO_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "autosave-io");
        t.setDaemon(true);
        return t;
    });

    public static void setAutosaveIntervalMs(long ms) {
        AUTOSAVE_INTERVAL_MS = Math.max(250L, ms);
    }

    // ===== init/load progress =====
    public static void init(int numLevels) {
        try { Files.createDirectories(SAVES_DIR); } catch (IOException ignored) {}

        passedLevels = new boolean[numLevels];
        Path progress = SAVES_DIR.resolve(PROGRESS_FILE);
        if (Files.exists(progress)) {
            try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(progress)))) {
                boolean[] loaded = (boolean[]) in.readObject();
                if (loaded != null) {
                    if (loaded.length == numLevels) {
                        passedLevels = loaded;
                    } else {
                        boolean[] resized = new boolean[numLevels];
                        System.arraycopy(loaded, 0, resized, 0, Math.min(loaded.length, numLevels));
                        passedLevels = resized;
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private static void saveProgressFile() {
        Path progress = SAVES_DIR.resolve(PROGRESS_FILE);
        Path tmp = progress.resolveSibling(progress.getFileName() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeObject(passedLevels);
        } catch (Exception e) {
            e.printStackTrace();
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            return;
        }
        try {
            Files.move(tmp, progress, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tmp, progress, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        }
    }

    // ===== قفل/آنلاک مرحله =====
    public static void markPassed(int levelIndex) {
        if (levelIndex >= 0 && levelIndex < passedLevels.length) {
            passedLevels[levelIndex] = true;
            saveProgressFile();
        }
    }

    public static boolean isUnlocked(int levelIndex) {
        if (levelIndex <= 0) return true;
        if (levelIndex < passedLevels.length) return passedLevels[levelIndex - 1];
        return false;
    }

    // ===== فایل سیو هر مرحله =====
    private static Path savePathForLevel(int levelNumber) {
        return SAVES_DIR.resolve("level_" + levelNumber + ".sav");
    }
    public static void clearLevelSave(int levelNumber) {
        try { Files.deleteIfExists(savePathForLevel(levelNumber)); } catch (IOException ignored) {}
    }

    // ===== ذخیره/لود =====
    public static void saveLevelNow(GameEnv env) {
        if (env == null) return;
        GameState snap = GameState.fromEnv(env);
        writeGameStateAtomic(savePathForLevel(env.getLevelNumber()), snap);
    }

    public static boolean loadLevelIfExists(GameEnv env, int levelNumber) {
        Path p = savePathForLevel(levelNumber);
        if (!Files.exists(p)) return false;
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(p)))) {
            GameState snap = (GameState) in.readObject();
            if (snap != null) {
                snap.applyTo(env);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /** هر فریم صدا بزن (Throttle + IO روی ترد جدا) */
    public static void maybeAutosave(GameEnv env) {
        long now = System.currentTimeMillis();
        if (now - lastAutosaveAt < AUTOSAVE_INTERVAL_MS) return;
        if (!saving.compareAndSet(false, true)) return;

        lastAutosaveAt = now;

        GameState snap = GameState.fromEnv(env);
        Path path = savePathForLevel(env.getLevelNumber());

        IO_EXEC.submit(() -> {
            try {
                writeGameStateAtomic(path, snap);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                saving.set(false);
            }
        });
    }

    private static void writeGameStateAtomic(Path target, GameState state) {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
            try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            return;
        }
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        }
    }

    // ======================================================================
    // =======================  SNAPSHOT (Serializable)  =====================
    // ======================================================================

    public static class GameState implements Serializable {
        public int    levelNumber;

        public int    coins;
        public double packetLoss;
        public double temporalProgress;
        public double remainingWireLength;
        public double initialWireLength;

        public boolean hasAtar, hasAiryaman, hasAnahita;
        public boolean activeAtar, activeAiryaman, activeAnahita;
        public double  atarTimer, airyTimer;

        public double  aergiaCooldown;
        public int     curvePoints;

        // NEW: برای ادامه دقیق حالت بازی
        public boolean movementPaused;

        public java.util.List<SystemState> systems = new ArrayList<>();
        public java.util.List<WireState>   wires   = new ArrayList<>();
        public java.util.List<FieldState>  fields  = new ArrayList<>();

        // ---- ساخت Snapshot ----
        public static GameState fromEnv(GameEnv env) {
            GameState s = new GameState();
            s.levelNumber        = env.getLevelNumber();
            s.coins              = env.getCoins();
            try { s.packetLoss   = env.getPacketLoss(); } catch (Throwable ignored) {}
            s.temporalProgress   = env.getTemporalProgress();
            s.remainingWireLength= env.getRemainingWireLength();
            s.initialWireLength  = env.getInitialWireLength();

            s.hasAtar            = env.hasAtar();
            s.hasAiryaman        = env.hasAiryaman();
            s.hasAnahita         = env.hasAnahita();
            s.activeAtar         = env.isActiveAtar();
            s.activeAiryaman     = env.isActiveAiryaman();
            s.activeAnahita      = env.isActiveAnahita();

            try {
                s.atarTimer = env.__debug_getAtarTimer();
                s.airyTimer = env.__debug_getAiryTimer();
            } catch (Throwable ignored) {}

            s.aergiaCooldown     = env.getAergiaCooldown();
            try { s.curvePoints  = env.getCurvePoints(); } catch (Throwable ignored) {}

            // NEW: وضعیت توقف/حرکت
            try { s.movementPaused = env.isMovementPaused(); } catch (Throwable ignored) {}

            // systems (با وضعیت تولید برای SourceSystem)
            for (int i = 0; i < env.getSystems().size(); i++) {
                var sys = env.getSystems().get(i);
                SystemState ss = new SystemState();
                ss.className = sys.getClass().getName();
                ss.x = sys.getX();
                ss.y = sys.getY();
                ss.enabled = sys.isEnabled();

                if (sys instanceof SourceSystem src) {
                    ss.sourceGenerating = src.isGenerating();
                    try {
                        var m = SourceSystem.class.getMethod("getSpawnTimer");
                        Object vv = m.invoke(src);
                        if (vv instanceof Number n) ss.sourceSpawnTimer = n.doubleValue();
                    } catch (Throwable ignored) {}
                }
                s.systems.add(ss);
            }

            // wires + current packet
            for (Wire w : env.getWires()) {
                WireState ws = new WireState();
                ws.startSystemIndex = env.getSystems().indexOf(w.getStartSystem());
                ws.endSystemIndex   = env.getSystems().indexOf(w.getEndSystem());
                ws.startPortType    = w.getStartPortType();
                ws.endPortType      = w.getEndPortType();
                ws.heavyPasses      = w.getHeavyPasses();

                for (int i = 0; i < w.getAnchorCount(); i++) {
                    Point2D.Double a = w.getAnchor(i);
                    ws.anchors.add(new DPoint(a.x, a.y));
                }

                if (w.getCurrentPacket() != null) {
                    var p = w.getCurrentPacket();
                    PacketState ps = new PacketState();
                    ps.className   = p.getClass().getName();
                    ps.compatKey   = p.getCompatibilityKey();
                    ps.forward     = p.isGoingForward();
                    ps.speed       = p.getInstantSpeed();
                    try { ps.id    = p.getId(); } catch (Throwable ignored) {}
                    ps.sOnWire     = approxDistanceOnWire(w, p.getX(), p.getY());
                    ws.currentPacket = ps;
                }
                s.wires.add(ws);
            }

            // fields
            for (int wi = 0; wi < env.getWires().size(); wi++) {
                Wire w = env.getWires().get(wi);
                for (var f : env.getFieldsOnWire(w)) {
                    FieldState fs = new FieldState();
                    fs.wireIndex = wi;
                    fs.type = (f.type == GameEnv.WireField.Type.AERGIA) ? "AERGIA" : "ELIPHAS";
                    fs.x = f.x; fs.y = f.y; fs.radius = f.radius; fs.endAtMs = f.endAtMs;
                    s.fields.add(fs);
                }
            }
            return s;
        }

        // ---- اعمال Snapshot ----
        public void applyTo(GameEnv env) {
            if (env.getLevelNumber() != this.levelNumber) {
                System.err.println("Save belongs to level " + levelNumber +
                        " but env is level " + env.getLevelNumber());
            }

            // پاکسازی سیم/پکت و آزاد کردن پورت‌ها
            var toRemove = new ArrayList<>(env.getWires());
            for (Wire w : toRemove) {
                try { if (w.getStartPort()!=null) w.getStartPort().setEmpty(true); } catch (Throwable ignored) {}
                try { if (w.getEndPort()!=null)   w.getEndPort().setEmpty(true); }   catch (Throwable ignored) {}
                env.removeWire(w);
            }
            env.getPackets().clear();

            // وضعیت کلی
            env.setCoins(this.coins);
            env.setTemporalProgress(this.temporalProgress);
            env.setRemainingWireLength(this.remainingWireLength);

            if (this.hasAtar)      env.buyAtar();
            if (this.hasAiryaman)  env.buyAiryaman();
            if (this.hasAnahita)   env.buyAnahita();
            env.setActiveAtar(this.activeAtar);
            env.setActiveAiryaman(this.activeAiryaman);
            env.setActiveAnahita(this.activeAnahita);
            try {
                env.__debug_setAtarTimer(this.atarTimer);
                env.__debug_setAiryTimer(this.airyTimer);
            } catch (Throwable ignored) {}

            try {
                env.__debug_setAergiaCooldown(this.aergiaCooldown);
                env.__debug_setCurvePoints(this.curvePoints);
            } catch (Throwable ignored) {}

            // NEW: حالت توقف/حرکت
            try { env.setMovementPaused(this.movementPaused); } catch (Throwable ignored) {}

            // مکان سیستم‌ها + بازگردانی حالت تولید در SourceSystem
            for (int i = 0; i < Math.min(this.systems.size(), env.getSystems().size()); i++) {
                var src = this.systems.get(i);
                var sys = env.getSystems().get(i);
                env.moveSystem(sys, src.x, src.y);
                sys.setEnabled(src.enabled);

                if (sys instanceof SourceSystem s && src.sourceGenerating != null) {
                    setSourceGeneratingSafely(s, src.sourceGenerating);
                    if (src.sourceSpawnTimer != null) {
                        try {
                            var m = SourceSystem.class.getMethod("setSpawnTimer", double.class);
                            m.invoke(s, src.sourceSpawnTimer.doubleValue());
                        } catch (Throwable ignored) {}
                    }
                }
            }

            // بازسازی سیم‌ها + پکت‌ها
            int maxId = -1;
            for (WireState ws : this.wires) {
                if (ws.startSystemIndex < 0 || ws.endSystemIndex < 0) continue;
                if (ws.startSystemIndex >= env.getSystems().size() ||
                        ws.endSystemIndex   >= env.getSystems().size()) continue;

                var sSys = env.getSystems().get(ws.startSystemIndex);
                var eSys = env.getSystems().get(ws.endSystemIndex);

                Port start = findEmptyPortByType(sSys.getOutputPorts(), ws.startPortType);
                Port end   = findEmptyPortByType(eSys.getInputPorts(),  ws.endPortType);
                if (start == null || end == null) continue;

                double len = start.distanceTo(end, env);
                double sx = start.getCenterX(), sy = start.getCenterY();
                double ex = end.getCenterX(),   ey = end.getCenterY();

                Wire newWire = new Wire(start, end,
                        sSys, ws.startPortType, eSys, ws.endPortType,
                        len, sx, sy, ex, ey, Collections.emptyList(), env);

                // پورت‌ها را مشغول کن
                start.setEmpty(false);
                end.setEmpty(false);

                // لینک پورت به سیم
                linkPortToWireIfPossible(start, newWire);
                linkPortToWireIfPossible(end,   newWire);

                sSys.addOutputWire(newWire);
                eSys.addInputWire(newWire);
                env.addWire(newWire);

                // انکرها (با fallback)
                if (!ws.anchors.isEmpty()) {
                    List<Point2D.Double> exact = new ArrayList<>();
                    for (DPoint dp : ws.anchors) exact.add(new Point2D.Double(dp.x, dp.y));
                    applyAnchorsExactOrNearest(newWire, exact);
                }

                newWire.setHeavyPasses(ws.heavyPasses);

                // پکت جاری روی سیم
                if (ws.currentPacket != null) {
                    try {
                        var ps = ws.currentPacket;
                        Class<?> k = Class.forName(ps.className);
                        Object obj = k.getDeclaredConstructor().newInstance();
                        org.example.model.Packet.Packet pkt = (org.example.model.Packet.Packet) obj;

                        try { pkt.setId(ps.id); } catch (Throwable ignored) {}
                        if (ps.id > maxId) maxId = ps.id;

                        // جهت را مستقیم ست کن؛ bounceBackFromEnd نزن (چون به progress کار داریم)
                        if (ps.forward) pkt.setDirectionForward(); else pkt.setDirectionBackward();
                        pkt.setSpeed(ps.speed);

                        // 1) اول پکت را روی سیم بگذار (این progress را ریست می‌کند)
                        newWire.setCurrentPacket(pkt);

                        // 2) سپس پیشروی دقیق سیو شده را برگردان
                        pkt.setProgress(ps.sOnWire);

                        // 3) و x/y را با s هم‌خوان کن
                        Point2D.Double pt = newWire.getPointAtDistance(ps.sOnWire);
                        try {
                            k.getMethod("setX", double.class).invoke(pkt, pt.x);
                            k.getMethod("setY", double.class).invoke(pkt, pt.y);
                        } catch (Throwable t) {
                            try { var fx = k.getDeclaredField("x"); fx.setAccessible(true); fx.set(pkt, pt.x); } catch (Throwable ignored) {}
                            try { var fy = k.getDeclaredField("y"); fy.setAccessible(true); fy.set(pkt, pt.y); } catch (Throwable ignored) {}
                        }
                        try { k.getMethod("setCompatibilityKey", String.class).invoke(pkt, ps.compatKey); } catch (Throwable ignored) {}

                        env.getPackets().add(pkt);
                    } catch (Throwable creationFailed) {
                        creationFailed.printStackTrace();
                    }
                }
            }

            // nextPacketId را (در صورت وجود) جلو ببریم تا تکراری نشود
            try {
                int next = maxId + 1;
                var f = GameEnv.class.getDeclaredField("nextPacketId");
                f.setAccessible(true);
                int cur = (Integer) f.get(env);
                if (next > cur) f.set(env, next);
            } catch (Throwable ignored) {}

            env.recalcWireBudget();
            env.clearWirePreview();
            env.updateTotalPacketsFromSources();
            rescanPortsBusyFromWires(env);
            forceIndicatorsFromPorts(env);
            refreshConnectionIndicators(env);
        }

        private static Port findEmptyPortByType(List<Port> ports, String type) {
            for (Port p : ports) if (Objects.equals(p.getType(), type) && p.getIsEmpty()) return p;
            for (Port p : ports) if (Objects.equals(p.getType(), type)) return p; // fallback
            return ports.isEmpty()? null : ports.get(0);
        }
    }

    public static class SystemState implements Serializable {
        public String className;
        public double x, y;
        public boolean enabled;

        // فقط برای SourceSystem
        public Boolean sourceGenerating;     // null اگر سیستم سورس نباشد
        public Double  sourceSpawnTimer;     // اختیاری
    }

    public static class WireState implements Serializable {
        public int startSystemIndex;
        public int endSystemIndex;
        public String startPortType;
        public String endPortType;
        public int heavyPasses;
        public java.util.List<DPoint> anchors = new ArrayList<>();
        public PacketState currentPacket; // nullable
    }

    public static class PacketState implements Serializable {
        public String className;
        public String compatKey;
        public boolean forward;
        public double speed;
        public double sOnWire;
        public int    id;
    }

    public static class FieldState implements Serializable {
        public int wireIndex;
        public String type; // "AERGIA" | "ELIPHAS"
        public double x, y, radius;
        public long endAtMs;
    }

    public static class DPoint implements Serializable {
        public double x, y;
        public DPoint() {}
        public DPoint(double x, double y) { this.x = x; this.y = y; }
    }

    // ===== Helpers =====

    /** اگر setGenerating موجود نباشد، سعی می‌کنیم start/stop را پیدا کنیم. */
    private static void setSourceGeneratingSafely(SourceSystem s, boolean gen) {
        try {
            var m = SourceSystem.class.getMethod("setGenerating", boolean.class);
            m.invoke(s, gen);
            return;
        } catch (Throwable ignored) {}
        try {
            if (gen) {
                var m = SourceSystem.class.getMethod("startGenerating");
                m.invoke(s);
            } else {
                var m = SourceSystem.class.getMethod("stopGenerating");
                m.invoke(s);
            }
        } catch (Throwable ignored) {}
    }

    /** اگر Wire.setAnchorsExact نبود، به‌جایش addAnchorAtNearest را صدا می‌زنیم. */
    private static void applyAnchorsExactOrNearest(Wire wire, List<Point2D.Double> exact) {
        try {
            var m = Wire.class.getMethod("setAnchorsExact", List.class);
            m.invoke(wire, exact);
        } catch (Throwable ignored) {
            for (Point2D.Double p : exact) {
                try { wire.addAnchorAtNearest(p.x, p.y); } catch (Throwable ignored2) {}
            }
        }
    }
    // از روی پر/خالی پورت‌ها، اندیکیتور هر سیستم را ست کن (با رفلکشن روی نام‌های متداول)
    private static void forceIndicatorsFromPorts(GameEnv env) {
        for (var sys : env.getSystems()) {
            boolean hasConn = false;
            try {
                for (Port p : sys.getInputPorts())  { if (!p.getIsEmpty()) { hasConn = true; break; } }
                if (!hasConn) {
                    for (Port p : sys.getOutputPorts()) { if (!p.getIsEmpty()) { hasConn = true; break; } }
                }
            } catch (Throwable ignored) {}

            // سِتِرهای رایج
            for (String name : new String[]{"setIndicatorOn","setConnected","setConnectionsIndicator","setIndicator"}) {
                try { sys.getClass().getMethod(name, boolean.class).invoke(sys, hasConn); break; }
                catch (Throwable ignored) {}
            }
            // فیلدهای رایج
            for (String field : new String[]{"indicatorOn","connected","connectionsOk","indicator"}) {
                try { var f = sys.getClass().getDeclaredField(field); f.setAccessible(true); f.set(sys, hasConn); break; }
                catch (Throwable ignored) {}
            }
        }
    }

    /** تخمین s روی سیم (اگر approxDistanceForPoint نباشد، از نمونه‌برداری استفاده می‌کنیم). */
    private static double approxDistanceOnWire(Wire w, double px, double py) {
        try {
            var m = Wire.class.getMethod("approxDistanceForPoint", double.class, double.class);
            Object v = m.invoke(w, px, py);
            return (v instanceof Double) ? (Double) v : 0.0;
        } catch (Throwable ignored) {
            int N = 120;
            double bestD = Double.MAX_VALUE;
            double accum = 0.0;
            Point2D.Double prev = w.getPointAt(0.0);
            double bestS = 0.0;
            for (int i = 1; i <= N; i++) {
                double t = i / (double) N;
                Point2D.Double cur = w.getPointAt(t);
                double seg = prev.distance(cur);
                double d = cur.distance(px, py);
                if (d < bestD) { bestD = d; bestS = accum; }
                accum += seg;
                prev = cur;
            }
            return bestS;
        }
    }

    /** LED/Indicator fix: همسان‌سازی پر/خالی پورت‌ها بر اساس وضعیت واقعی سیم‌ها. */
    private static void rescanPortsBusyFromWires(GameEnv env) {
        for (var sys : env.getSystems()) {
            for (Port p : sys.getInputPorts())  p.setEmpty(true);
            for (Port p : sys.getOutputPorts()) p.setEmpty(true);
        }
        for (Wire w : env.getWires()) {
            if (w.getStartPort()!=null) w.getStartPort().setEmpty(false);
            if (w.getEndPort()!=null)   w.getEndPort().setEmpty(false);
        }
    }

    // لینک پورت↔سیم اگر API داشته باشید
    private static void linkPortToWireIfPossible(Port p, Wire w) {
        if (p == null || w == null) return;
        try {
            var m = p.getClass().getMethod("setWire", Wire.class);
            m.invoke(p, w);
        } catch (Throwable ignored) {
            try {
                var m2 = p.getClass().getMethod("setConnectedWire", Wire.class);
                m2.invoke(p, w);
            } catch (Throwable ignored2) {}
        }
    }

    // بعد از لود، اندیکیتور اتصال سیستم‌ها را رفرش کن
    private static void refreshConnectionIndicators(GameEnv env) {
        for (var sys : env.getSystems()) {
            for (String name : new String[] {
                    "onConnectionsChanged","refreshIndicator","refreshIndicators",
                    "recalcIndicators","updateIndicator","updateIndicators"
            }) {
                try { sys.getClass().getMethod(name).invoke(sys); break; }
                catch (Throwable ignored) {}
            }
        }
    }

    // --- ست‌کردن پیشروی پکت روی سیم: s (طول) یا t (۰..۱)، متد/فیلدهای رایج ---
    private static void setPacketProgressOnWireIfPossible(Object pkt, Wire wire, double s) {
        double len = 0.0;
        try { len = wire.getLength(); } catch (Throwable ignored) {}
        double t = (len > 1e-9) ? Math.max(0, Math.min(1, s / len)) : 0.0;

        // روش‌های متداول به ترتیب:
        String[] methodS = { "setSOnWire", "setS", "setProgress", "setDistanceOnWire", "setDistance" };
        String[] methodT = { "setT", "setParamT", "setProgressT" };
        String[] fieldS  = { "sOnWire", "s", "progress", "distanceOnWire", "distance" };
        String[] fieldT  = { "t", "paramT", "progressT" };
        String[] snap    = { "snapToWire", "snapTo", "syncToWire", "synchronizeToWire" };

        // متدهای s
        for (String m : methodS) {
            try { pkt.getClass().getMethod(m, double.class).invoke(pkt, s); return; }
            catch (Throwable ignored) {}
        }
        // متدهای t
        for (String m : methodT) {
            try { pkt.getClass().getMethod(m, double.class).invoke(pkt, t); return; }
            catch (Throwable ignored) {}
        }
        // فیلدهای s
        for (String f : fieldS) {
            try { var ff = pkt.getClass().getDeclaredField(f); ff.setAccessible(true); ff.set(pkt, s); return; }
            catch (Throwable ignored) {}
        }
        // فیلدهای t
        for (String f : fieldT) {
            try { var ff = pkt.getClass().getDeclaredField(f); ff.setAccessible(true); ff.set(pkt, t); return; }
            catch (Throwable ignored) {}
        }
        // اگر متد snap وجود داشت (بدون تضمین امضا)
        for (String m : snap) {
            try {
                try { pkt.getClass().getMethod(m, Wire.class, double.class).invoke(pkt, wire, s); return; }
                catch (Throwable ignored) {}
                try { pkt.getClass().getMethod(m, double.class).invoke(pkt, s); return; }
                catch (Throwable ignored) {}
                try { pkt.getClass().getMethod(m).invoke(pkt); return; }
                catch (Throwable ignored) {}
            } catch (Throwable ignored) {}
        }
        // در بدترین حالت همان x/y ست شده‌اند و فریم بعدی هم به همان مختصات می‌ماند.
    }
}
