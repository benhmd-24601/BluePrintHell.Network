package org.example.model.Packet;

import org.example.model.GameEnv;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Wire;

import java.util.ArrayList;
import java.util.List;
import static org.example.model.ModelConfig.*;

public abstract class Packet {

    // --- وضعیت مکانی روی سیم
    protected double wireX, wireY;
    protected double offsetX = 0, offsetY = 0;
    private static final double OFF_WIRE_THRESHOLD = 7.0;

    // سیم/پیشروی
    protected Wire wire;
    protected double progress = 0.0;
    protected int direction = +1;               // +1 جلو، -1 عقب
    private long lastUpdateNs = 0;
    private double timeOnThisWireSec = 0.0;

    // حرکت
    protected double speed = BASE_SPEED_MSG;    // سرعت لحظه‌ای
    protected double accel = 0.0;               // شتاب لحظه‌ای

    // نویز
    protected double noiseLevel = 0.0;

    // متفرقه
    private final List<Packet> currentCollisions = new ArrayList<>();
    private final List<Packet> currentImpact = new ArrayList<>();
    private int id;

    protected Packet(double x, double y) {
        this.wireX = x;
        this.wireY = y;
    }

    // --- API عمومی
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }

    public Wire getWire() { return wire; }
    public void setWire(Wire w) {
        this.wire = w;
        this.timeOnThisWireSec = 0.0;
        this.lastUpdateNs = System.nanoTime();
        if (direction > 0) this.progress = 0.0; else this.progress = w.getLength();
        onEnterWire(w);
    }

    public double getX() { return wireX + offsetX; }
    public double getY() { return wireY + offsetY; }
    public double getProgress() { return progress; }
    public double getInstantSpeed() { return Math.abs(speed); }
    public double getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(double v) { noiseLevel = v; }

    public void setSpeed(double v) { speed = Math.max(SPEED_MIN, v); }
    public void setAccel(double a) { accel = a; }

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

    // --- قلاب‌های پُلیمورفیک
    /** کلید سازگاری (برای پورت‌ها). برای محرمانه/حجیم ممکن است null باشد. */
    public String getCompatibilityKey() { return null; }
    /** آیا «می‌تواند» روی سِیمی با startPortType مشخص گذاشته شود؟ */
    public boolean canEnterWireWithStartType(String startType) {
        String key = getCompatibilityKey();
        return key == null || key.equals(startType);
    }
    /** ورود به سیم: زیرکلاس‌ها سرعت/شتاب را طبق سازگار/ناسازگار تنظیم کنند */
    protected abstract void onEnterWire(Wire w);
    /** اندازه‌ی هندسی (برای نویز/برخوردها) */
    public abstract int getSize();
    /** سکه‌ی اضافه‌شونده هنگام تحویل به سیستم */
    public abstract int getCoinValue();

    /** به مقصد تحویل شد (system فعال)، کوین بده و رفتار ویژه انجام بده */
    public void onDelivered(GameEnv env, NetworkSystem dst) {
        env.setCoins(env.getCoins() + getCoinValue());
    }

    // --- حرکت
    public void updatePosition(double delta) {
        if (wire == null) return;

        long now = System.nanoTime();
        if (lastUpdateNs == 0) lastUpdateNs = now;
        timeOnThisWireSec += (now - lastUpdateNs) / 1_000_000_000.0;
        lastUpdateNs = now;

        if (timeOnThisWireSec > PACKET_WIRE_TIMEOUT) {
            wire.getEnv().markAsLost(this);
            if (wire.getCurrentPacket() == this) wire.setCurrentPacket(null);
            return;
        }


        // v = v0 + a*dt
        speed += accel * delta;

        double totalLen = wire.getLength();
        double dp = speed * delta * (direction > 0 ? +1 : -1);
        progress = Math.max(0, Math.min(totalLen, progress + dp));

        double t = (totalLen == 0) ? (isGoingForward()? 1.0 : 0.0) : (progress / totalLen);
        double sx = wire.getStartx(), sy = wire.getStarty();
        double ex = wire.getEndX(),  ey = wire.getEndY();
        double baseX = sx + (ex - sx) * t;
        double baseY = sy + (ey - sy) * t;
        this.wireX = baseX;
        this.wireY = baseY;

        // رسیدن به انتهای مسیر
        if (reachedDestination()) wire.deliverCurrentPacket();
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
    public String getPortKey() {
        return null; // پیش‌فرض: پکتی که پورت‌محور نیست
    }

}
