package org.example.model.Packet;

import org.example.model.GameEnv;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Wire;

import java.util.ArrayList;
import java.util.List;
import static org.example.model.ModelConfig.*;

public abstract class Packet {

    protected double wireX, wireY;
    protected double offsetX = 0, offsetY = 0;
    private static final double OFF_WIRE_THRESHOLD = 7.0;

    protected Wire wire;
    protected double progress = 0.0;
    protected int direction = +1;
    private long lastUpdateNs = 0;
    private double timeOnThisWireSec = 0.0;

    protected double speed = BASE_SPEED_MSG;
    protected double accel = 0.0;

    // NEW: وقتی توسط Aergia «شتاب» قفل شد (صفر بماند)
    private boolean accelFrozen = false;

    protected double noiseLevel = 0.0;

    private final List<Packet> currentCollisions = new ArrayList<>();
    private final List<Packet> currentImpact = new ArrayList<>();
    private int id;

    protected Packet(double x, double y) {
        this.wireX = x;
        this.wireY = y;
    }

    public void setId(int id) { this.id = id; }
    public int getId() { return id; }

    public Wire getWire() { return wire; }
    public void setWire(Wire w) {
        this.wire = w;
        this.timeOnThisWireSec = 0.0;
        this.lastUpdateNs = System.nanoTime();
        if (direction > 0) this.progress = 0.0; else this.progress = (w == null ? 0.0 : w.getLength());
        this.accelFrozen = false; // ریست وقتی وارد سیم می‌شود
        onEnterWire(w);
    }

    public double getX() { return wireX + offsetX; }
    public double getY() { return wireY + offsetY; }
    public double getProgress() { return progress; }
    public double getInstantSpeed() { return Math.abs(speed); }
    public double getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(double v) { noiseLevel = v; }

    public void setSpeed(double v) { speed = Math.max(SPEED_MIN, v); }
    public void setAccel(double a) {
        accel = a;
        if (a != 0.0) accelFrozen = false; // طبق متن: «تا زمانی که مجدداً شتابی به آنها اعمال نشده باشد»
    }
    public void freezeAccelToZero() {
        accel = 0.0;
        accelFrozen = true;
    }

    public void setDirectionForward(){ direction = +1; }
    public void setDirectionBackward(){ direction = -1; }
    public boolean isGoingForward(){ return direction > 0; }

    public boolean isCompletelyOffWire() {
        return Math.hypot(offsetX, offsetY) > OFF_WIRE_THRESHOLD;
    }
    public void applyImpact(double dx, double dy) { offsetX += dx; offsetY += dy; }
    public List<Packet> getCurrentImpact(){ return currentImpact; }

    public void onCollision(Packet other){
        if (!currentCollisions.contains(other)){
            currentCollisions.add(other);
            this.noiseLevel += 1;
            other.noiseLevel += 1;
            this.speed *= 0.7;
        }
    }

    public void bounceBackFromEnd() {
        setDirectionBackward();
        if (wire != null) this.progress = Math.max(0, wire.getLength() - 0.0001);
    }

    public boolean reachedDestination() {
        if (wire == null) return false;
        return (direction > 0 && progress >= wire.getLength()) ||
                (direction < 0 && progress <= 0);
    }

    public String getCompatibilityKey() { return null; }
    public boolean canEnterWireWithStartType(String startType) {
        String key = getCompatibilityKey();
        return key == null || key.equals(startType);
    }
    protected abstract void onEnterWire(Wire w);
    public abstract int getSize();
    public abstract int getCoinValue();

    public void onDelivered(GameEnv env, NetworkSystem dst) {
        env.setCoins(env.getCoins() + getCoinValue());
    }

    // --- حرکت (نسخه‌ی با مسیر منحنی + اثر فیلدها)
    public void updatePosition(double delta) {
        if (wire == null) return;

        long now = System.nanoTime();
        if (lastUpdateNs == 0) lastUpdateNs = now;
        timeOnThisWireSec += (now - lastUpdateNs) / 1_000_000_000.0;
        lastUpdateNs = now;

        if (timeOnThisWireSec > PACKET_WIRE_TIMEOUT) {
            wire.getEnv().markAsLost(this , "timeout_on_wire>" + PACKET_WIRE_TIMEOUT + "s");
            if (wire.getCurrentPacket() == this) wire.setCurrentPacket(null);
            return;
        }

        // اگر Aergia ما را فریز نکرده، شتاب اعمال می‌شود
        if (!accelFrozen) {
            speed += accel * delta;
        }

        double totalLen = wire.getLength();
        double dp = speed * delta * (direction > 0 ? +1 : -1);
        progress = Math.max(0.0, Math.min(totalLen, progress + dp));

        double t = (totalLen <= 1e-9) ? (isGoingForward() ? 1.0 : 0.0) : (progress / totalLen);
        java.awt.geom.Point2D.Double pt = wire.getPointAt(t);
        this.wireX = pt.x;
        this.wireY = pt.y;

        // ===== NEW: اعمال فیلدهای Aergia / Eliphas
        var fields = wire.getEnv().getFieldsOnWire(wire);
        for (GameEnv.WireField f : fields) {
            double dx = wireX - f.x, dy = wireY - f.y;
            double dist = Math.hypot(dx, dy);
            if (dist <= f.radius) {
                if (f.type == GameEnv.WireField.Type.AERGIA) {
                    // شتاب را صفر و فریز می‌کنیم → حرکت با سرعت ثابت
                    freezeAccelToZero();
                } else {
                    // ELIPHAS: بازگردانی پیوسته CoM به راستای سیم
                    // یک دَمپینگ پیوسته: offset *= exp(-k*dt)
                    double k = 8.0; // نرخ برگشت
                    double damper = Math.exp(-k * delta);
                    offsetX *= damper;
                    offsetY *= damper;
                }
            }
        }

        if (reachedDestination()) wire.deliverCurrentPacket();
    }

    public void setProgress(double progress) { this.progress = progress; }
    public void setDirection(int direction) { this.direction = direction; }

    public String getPortKey() { return null; }

    private boolean teleported = false;
    public boolean isTeleported() { return teleported; }
    public void setTeleported(boolean t) { this.teleported = t; }
    public void markTeleported() { this.teleported = true; }
}
