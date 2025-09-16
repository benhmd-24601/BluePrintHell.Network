package org.example.model;

import org.example.model.Packet.Packet;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Systems.SinkSystem;
import org.example.model.Systems.SourceSystem;
import org.example.util.Debug;

import javax.swing.*;
import java.util.*;
import java.awt.Point;

public class GameEnv {

    private final int levelNumber;

    private final List<NetworkSystem> systems = new ArrayList<>();
    private final List<Wire> wires = new ArrayList<>();
    private final List<Packet> packets = new ArrayList<>();

    private int coins;
    private double packetLoss; // count (not percent)
    private double temporalProgress; // 0..100
    private double remainingWireLength;
    private double initialWireLength;
    private int totalPacketsGenerated;

    // ================== NEW: Store / Scrolls / Curve Points ==================
    // ظرفیت نودهای کرو که بازیکن از استور خریده:
    private int curvePoints = 0;

    // «فیلد»‌هایی که روی سیم‌ها فعال می‌شوند (Aergia / Eliphas)

    public static class WireField {
        public enum Type { AERGIA, ELIPHAS }
        public Type type;
        public Wire wire;
        public double x, y;
        public double radius;
        public long endAtMs;   // زمان پایان اثر به ms
    }
    private final List<WireField> wireFields = new ArrayList<>();


    // مُدهای موقتیِ «در حال کاشت/استفاده» (برای کلیک روی بورد)
    public enum PlacementMode {
        NONE,
        PLACE_AERGIA,
        PLACE_ELIPHAS,
        SISYPHUS_SELECT,
        SISYPHUS_DRAG
    }
    private PlacementMode placementMode = PlacementMode.NONE;

    // سیزیفوس (درگ محدود یک سیستم)
    private NetworkSystem sisyphusTarget = null;
    private double sisyphusOriginX = 0, sisyphusOriginY = 0;

    // کانفیگ‌ها (می‌تونی بعداً منتقلشون کنی به ModelConfig)
    public static final double AERGIA_COST = 10.0;
    public static final double SISYPHUS_COST = 15.0;
    public static final double ELIPHAS_COST = 20.0;

    public static final long AERGIA_DURATION_MS = 20_000L;
    public static final long ELIPHAS_DURATION_MS = 30_000L;

    private static double toSeconds(double d) { return d > 5.0 ? d / 1000.0 : d; }
    public static final double AERGIA_COOLDOWN = 15.0;   // ثانیه
    public static final double AERGIA_RADIUS = 35.0;     // px
    public static final double ELIPHAS_RADIUS = 35.0;    // px

    public static final double SISYPHUS_RADIUS = 120.0;  // شعاع مجاز جابجایی
    private double aergiaCooldown = 0.0;

    // ========================================================================

    // Store (قدیمی)
    private boolean hasAtar = false;
    private boolean hasAiryaman = false;
    private boolean hasAnahita = false;
    private boolean activeAtar, activeAiryaman, activeAnahita;
    private double atarTimer = 0;
    private double airyTimer = 0;

    private Runnable onGameOver;
    private boolean gameOverFired = false;

    public GameEnv(int levelNumber) {
        this.levelNumber = levelNumber;
        coins = 100;
        packetLoss = 0;
        temporalProgress = 0;
    }

    // region Abilities (قدیمی)
    public boolean hasAtar() { return hasAtar; }
    public boolean hasAiryaman() { return hasAiryaman; }
    public boolean hasAnahita() { return hasAnahita; }
    public boolean isActiveAtar() { return activeAtar; }
    public boolean isActiveAiryaman() { return activeAiryaman; }
    public boolean isActiveAnahita() { return activeAnahita; }

    public boolean canBuyAtar() { return !hasAtar && coins >= 3; }
    public boolean canBuyAiryaman() { return !hasAiryaman && coins >= 4; }
    public boolean canBuyAnahita() { return !hasAnahita && coins >= 5; }

    public void buyAtar() { if (canBuyAtar()) { hasAtar = true; coins -= 3; } }
    public void buyAiryaman() { if (canBuyAiryaman()) { hasAiryaman = true; coins -= 4; } }
    public void buyAnahita() { if (canBuyAnahita()) { hasAnahita = true; coins -= 5; } }

    public void setActiveAtar(boolean a) { activeAtar = a; }
    public void setActiveAiryaman(boolean a) { activeAiryaman = a; }
    public void setActiveAnahita(boolean a) { activeAnahita = a; }

    public void activateAtar() { if (hasAtar) { activeAtar = true; atarTimer = 10.0; } }
    public void activateAiryaman() { if (hasAiryaman) { activeAiryaman = true; airyTimer = 5.0; } }
    // endregion

    // region Accessors
    public List<NetworkSystem> getSystems() { return systems; }
    public List<Wire> getWires() { return wires; }
    public List<Packet> getPackets() { return packets; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public void addCoins(int amount) { coins += amount; }

    public double getPacketLoss() { return packetLoss; } // count
    public void increasePacketLoss(double value) { packetLoss += value; }

    public double getTemporalProgress() { return temporalProgress; }
    public void setTemporalProgress(double progress) { this.temporalProgress = progress; }

    public double getRemainingWireLength() { return remainingWireLength; }
    public void setRemainingWireLength(double length) { this.remainingWireLength = length; }

    public double getInitialWireLength() { return initialWireLength; }

    public int getTotalPacketsGenerated() { return totalPacketsGenerated; }
    public void incrementTotalPacketCount() { totalPacketsGenerated++; }
    // NEW:
    public int getCurvePoints() { return curvePoints; }
    public double getAergiaCooldown() { return aergiaCooldown; }
    public PlacementMode getPlacementMode() { return placementMode; }
    public List<WireField> getFieldsOnWire(Wire w) {
        ArrayList<WireField> out = new ArrayList<>();
        for (WireField f : wireFields) if (f.wire == w) out.add(f);
        return out;
    }
    // endregion

    // region Build
    public void applyStage(Stage stage) {
        systems.clear();
        wires.clear();
        packets.clear();
        wireFields.clear();           // NEW: فیلدها ریست شوند
        placementMode = PlacementMode.NONE;
        sisyphusTarget = null;
        aergiaCooldown = 0.0;
        curvePoints = 0;              // ظرفیت کرو مخصوص این مرحله است

        remainingWireLength = stage.getInitialWireLength();
        temporalProgress = 0;
        packetLoss = 0;

        for (NetworkSystem sys : stage.getSystems()) {
            sys.setEnv(this);
            systems.add(sys);
            if (sys instanceof SourceSystem src) {
                src.setEnv(this);
            }
        }
        initialWireLength = stage.getInitialWireLength();
        updateTotalPacketsFromSources();
    }
    // endregion

    // region Game loop
    private long lastPowerUpdateTime;
    public void update(double delta) {
        long currentTime = System.currentTimeMillis();
        if (lastPowerUpdateTime == 0) lastPowerUpdateTime = currentTime;
        long elapsed = currentTime - lastPowerUpdateTime;

        if (activeAtar) {
            atarTimer -= elapsed / 1000.0;
            if (atarTimer <= 0) { activeAtar = false; atarTimer = 0; }
        }
        if (activeAiryaman) {
            airyTimer -= elapsed / 1000.0;
            if (airyTimer <= 0) { activeAiryaman = false; airyTimer = 0; }
        }
        lastPowerUpdateTime = currentTime;

        // NEW: کاهش کول‌داون Aergia و عمر فیلدها
        if (aergiaCooldown > 0) aergiaCooldown = Math.max(0, aergiaCooldown - delta);
        pruneExpiredFields();

        for (NetworkSystem s : systems) s.update();

        generate(delta);

        if (!movementPaused) {
            processWires(delta);
            cleanup();
        }

        if (!gameOverFired && isGameOver()) {
            gameOverFired = true;
            if (onGameOver != null) SwingUtilities.invokeLater(onGameOver);
            return;
        }
    }
    // endregion

    private void pruneExpiredFields() {
        long now = System.currentTimeMillis();
        wireFields.removeIf(f -> f.endAtMs <= now);
    }


    public boolean hasAnyWireCrossing() {
        for (Wire w : wires) {
            if (w.crossesAnySystem(this)) return true;
        }
        return false;
    }
    public void setOnGameOver(Runnable onGameOver) { this.onGameOver = onGameOver; }

    public boolean isGameOver() {
        int allpackets = 0;
        double droppedPackets = 0;
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem source) {
                allpackets = source.getAllpackets();
            }
            droppedPackets += system.getPacketsDropped();
        }
        if (allpackets <= 0) return false;
        double lossPercent = (packetLoss + droppedPackets) / allpackets * 100.0;
        return lossPercent >= 50.0;
    }

    public double getTotalPacketLossPercent() {
        int allpackets = 0;
        double droppedPackets = 0;
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem source) {
                allpackets = source.getAllpackets();
            }
            droppedPackets += system.getPacketsDropped();
        }
        if (allpackets <= 0) return 0;
        return (packetLoss + droppedPackets) / allpackets * 100.0;
    }

    private boolean gameisWon() {
        double allPackets = 0;
        double reached = 0;
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem) allPackets = ((SourceSystem) system).getAllpackets();
            if (system instanceof SinkSystem) reached = ((SinkSystem) system).getTotalReceived();
        }
        return allPackets > 0 && (reached / allPackets >= 0.5);
    }

    // region Packets
    private int nextPacketId = 0;

    public void generate(double delta) {
        if (hasAnyWireCrossing()) {
            Debug.log("[SAFE]", "Generation blocked: a wire crosses a system body.");
            return;
        }
        List<Packet> newPackets = new ArrayList<>();
        for (NetworkSystem system : systems) {
            if (system instanceof SourceSystem src && src.isGenerating()) {
                newPackets.addAll(src.updateAndGeneratePackets());
            }
        }
        for (Packet p : newPackets) p.setId(nextPacketId++);

        for (Packet p : newPackets) {
            Debug.log("[GEN]", "created " + Debug.p(p) + " @(" + (int)p.getX() + "," + (int)p.getY() + ")");
        }

        packets.addAll(newPackets);
    }

    public void processWires(double delta) {
        for (Wire wire : wires) {
            wire.update();
            if (wire.getCurrentPacket() != null) {
                wire.getCurrentPacket().updatePosition(delta);
            }
        }
    }

    public void cleanup() {
        packets.removeIf(p ->
                p.getWire() == null || (p.getWire().getCurrentPacket() != p)
        );

        for (int i = 0; i < packets.size(); i++) {
            Packet p1 = packets.get(i);
            for (int j = i + 1; j < packets.size(); j++) {
                Packet p2 = packets.get(j);
                if (p1.getWire() != p2.getWire() && areColliding(p1, p2)) {
                    if (!activeAiryaman) {
                        p1.onCollision(p2);
                    }
                    if (!activeAtar) {
                        triggerImpact(p1.getX(), p1.getY(), p1);
                    }
                }
            }
        }
    }

    private boolean areColliding(Packet p1, Packet p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.hypot(dx, dy) < 10;
    }

    public void triggerImpact(double cx, double cy, Packet source) {
        double maxRadius = 60;
        double baseStrength = 5;
        List<Packet> toRemove = new ArrayList<>();

        for (Packet p : new ArrayList<>(packets)) {
            double dx = p.getX() - cx;
            double dy = p.getY() - cy;
            double dist = Math.hypot(dx, dy);

            if (p.getNoiseLevel() > p.getSize()) {
                toRemove.add(p);
                increasePacketLoss(1);
            }
            if (dist < maxRadius && dist > 0) {
                if (!source.getCurrentImpact().contains(p)) {
                    source.getCurrentImpact().add(p);
                    double factor = 1 - (dist / maxRadius);
                    double impact = baseStrength * factor;
                    double ux = dx / dist, uy = dy / dist;
                    p.applyImpact(ux * impact, uy * impact);
                }
                if (p.isCompletelyOffWire()) toRemove.add(p);
            }
        }
        for (Packet p : toRemove) markAsLost(p, "off_wire_impact");
    }

    public void markAsLost(Packet p) {
        if (p == null) return;
        Wire w = p.getWire();
        if (w != null && w.getCurrentPacket() == p) w.setCurrentPacket(null);
        packets.remove(p);
        packetLoss++;
        if (!gameOverFired && isGameOver()) {
            gameOverFired = true;
            if (onGameOver != null) SwingUtilities.invokeLater(onGameOver);
        }
    }
    public void markAsLost(Packet p, String reason) {
        if (p == null) return;
        Debug.log("[LOSS]", Debug.p(p) + (reason==null? "" : " reason=" + reason) +
                " wire=" + (p.getWire()==null? "-" : Debug.wire(p.getWire())));
        markAsLost(p);
    }
    // endregion

    public void addWire(Wire wire) { wires.add(wire); }
    public void removeWire(Wire wire) {
        if (wire == null) return;
        wires.remove(wire);
        if (wire.getStartSystem() != null) wire.getStartSystem().removeOutputWire(wire);
        if (wire.getEndSystem() != null) wire.getEndSystem().removeInputWire(wire);
    }

    public Wire findWireByStartPort(Port p) {
        for (Wire w : wires) if (w.getStartPort() == p) return w;
        return null;
    }

    public void updateTotalPacketsFromSources() {
        int total = 0;
        for (NetworkSystem system : systems)
            if (system instanceof SourceSystem src) total = src.getPacketCount();
        totalPacketsGenerated = total;
    }

    public void resetGameState() {
        packets.clear();
        for (Wire w : wires) w.setCurrentPacket(null);
        for (NetworkSystem sys : systems) {
            if (sys instanceof SourceSystem src) src.reset();
            else sys.getPacketStorage().clear();
        }
        wireFields.clear();
        placementMode = PlacementMode.NONE;
        sisyphusTarget = null;
        aergiaCooldown = 0;
        curvePoints = 0;
    }

    public void simulateFastForward(double targetPercent) {
        double targetProgress = targetPercent * 100;
        if (targetProgress == temporalProgress) return;
        if (targetProgress < temporalProgress) {
            resetGameState();
            temporalProgress = 0;
        }
        int frames = (int)((targetProgress - temporalProgress) / 0.5);
        for (int i = 0; i < frames; i++) update(0.016);
        temporalProgress = targetProgress;
    }

    public void applyEffect(String effect) {
        switch (effect) {
            case "Anahita" -> {
                for (NetworkSystem system : systems) {
                    if (system instanceof SourceSystem src) {
                        for (Packet p : src.getAllPackets()) p.setNoiseLevel(0);
                    }
                }
            }
            case "Airyaman" -> setActiveAiryaman(true);
            case "Atar" -> setActiveAtar(true);
        }
    }

    // ======= NEW: خرید «نقطه‌ی کرو» =======
    public boolean buyCurvePoint() {
        if (coins >= 1) {
            coins -= 1;
            curvePoints += 1;
            return true;
        }
        return false;
    }
    public boolean consumeCurvePoint() {
        if (curvePoints > 0) { curvePoints--; return true; }
        return false;
    }

    // ======= NEW: شروع/انجام کاشت فیلدهای Aergia/Eliphas =======
    public boolean tryStartAergiaPlacement() {
        if (coins >= AERGIA_COST && aergiaCooldown <= 0) {
            coins -= (int)AERGIA_COST;
            placementMode = PlacementMode.PLACE_AERGIA;
            return true;
        }
        return false;
    }
    public boolean tryStartEliphasPlacement() {
        if (coins >= ELIPHAS_COST) {
            coins -= (int)ELIPHAS_COST;
            placementMode = PlacementMode.PLACE_ELIPHAS;
            return true;
        }
        return false;
    }

    public void placeField(Wire wire, double x, double y, WireField.Type type) {
        WireField f = new WireField();
        f.type = type;
        f.wire = wire;
        f.x = x; f.y = y;

        if (type == WireField.Type.AERGIA) {
            f.radius = AERGIA_RADIUS;
            f.endAtMs = System.currentTimeMillis() + AERGIA_DURATION_MS;
            aergiaCooldown = AERGIA_COOLDOWN; // همچنان برحسب ثانیه
        } else {
            f.radius = ELIPHAS_RADIUS;
            f.endAtMs = System.currentTimeMillis() + ELIPHAS_DURATION_MS;
        }
        wireFields.add(f);
        placementMode = PlacementMode.NONE;
    }

    public void cancelPlacement() { placementMode = PlacementMode.NONE; }

    // ======= NEW: Sisyphus – جابجایی محدود سیستم =======
    public boolean tryStartSisyphus() {
        if (coins >= SISYPHUS_COST) {
            coins -= (int)SISYPHUS_COST;
            placementMode = PlacementMode.SISYPHUS_SELECT;
            sisyphusTarget = null;
            return true;
        }
        return false;
    }
    public void beginSisyphusDrag(NetworkSystem s) {
        sisyphusTarget = s;
        sisyphusOriginX = s.getX();
        sisyphusOriginY = s.getY();
        placementMode = PlacementMode.SISYPHUS_DRAG;
    }
    public boolean isSisyphusDragActive() {
        return placementMode == PlacementMode.SISYPHUS_DRAG && sisyphusTarget != null;
    }
    public double getSisyphusOriginX() { return sisyphusOriginX; }
    public double getSisyphusOriginY() { return sisyphusOriginY; }
    public NetworkSystem getSisyphusTarget() { return sisyphusTarget; }

    /** تلاش برای جابجایی با رعایت محدودیت طول سیم و عدم عبور سیم‌ها از بدنه سیستم‌ها */
    public boolean tryMoveSystemRespectingConstraints(NetworkSystem sys, double newX, double newY) {
        double oldX = sys.getX(), oldY = sys.getY();
        moveSystem(sys, newX, newY);
        boolean invalid = isOverBudget() || hasAnyWireCrossing();
        if (invalid) {
            moveSystem(sys, oldX, oldY); // rollback
            return false;
        }
        return true;
    }

    public void finishSisyphus() {
        placementMode = PlacementMode.NONE;
        sisyphusTarget = null;
    }

    // داخل کلاس GameEnv
    private boolean movementPaused = false;

    public boolean isMovementPaused() { return movementPaused; }
    public void setMovementPaused(boolean paused) { this.movementPaused = paused; }
    public void pauseMovement() { movementPaused = true; }
    public void resumeMovement() { movementPaused = false; }

    private boolean overBudget = false;

    public boolean isOverBudget() { return overBudget; }

    /** طولِ مصرف‌شده = مجموع طول تمام سیم‌ها از روی موقعیت فعلی پورت‌ها */
    public double getUsedWireLength() { return initialWireLength - remainingWireLength; }

    /** وقتی سیمی اضافه/حذف/طولش عوض شد (مثلاً با جابجایی سیستم‌ها) این را صدا بزن */
    public void recalcWireBudget() {
        double used = 0.0;
        for (Wire w : wires) used += w.getLength();
        remainingWireLength = initialWireLength - used;
        overBudget = (remainingWireLength < 0);
    }

    /** جابجایی یک سیستم (مدل: هم خود سیستم هم تمام پورت‌هایش جابجا شوند) */
    public void moveSystem(NetworkSystem sys, double newX, double newY) {
        double dx = newX - sys.getX();
        double dy = newY - sys.getY();
        sys.setX(newX);
        sys.setY(newY);
        for (Port p : sys.getInputPorts())  p.translate(dx, dy);
        for (Port p : sys.getOutputPorts()) p.translate(dx, dy);
        recalcWireBudget();
    }

    private Port  previewStartPort = null;
    private Point previewMouse     = null;
    private Port  previewEndPort   = null; // اگر روی پورت compatible هستیم

    public void setWirePreview(Port start, Point mouse, Port end) {
        this.previewStartPort = start;
        this.previewMouse = mouse;
        this.previewEndPort = end;
    }
    public void clearWirePreview() {
        this.previewStartPort = null;
        this.previewMouse = null;
        this.previewEndPort = null;
    }
    public Port  getPreviewStartPort() { return previewStartPort; }
    public Point getPreviewMousePoint(){ return previewMouse; }
    public Port  getPreviewEndPort()   { return previewEndPort; }
}
