package org.example.model.Packet;

import org.example.model.Systems.VPNSystem;
import org.example.model.Wire;

import java.util.concurrent.ThreadLocalRandom;
import static org.example.model.ModelConfig.*;

public class ProtectedPacket extends Packet {
    public enum Mode { SQUARE_LIKE, TRIANGLE_LIKE, CIRCLE_LIKE }

    private final Packet original;    // مرجع نوع اصلی (پیام‌رسان یا محرمانه۱)
    private final Mode mode;
    private boolean protectionActive = true;
    private VPNSystem provider;       // برای dropProtection وقتی VPN خاموش شود
    private final int originalId;
    public ProtectedPacket(Packet original, VPNSystem provider) {
        super(original.getX(), original.getY());

        this.original = original;
        this.provider = provider;
        this.originalId = original.getId();
        // حرکت تصادفی مثل یکی از پیام‌رسان‌ها
        int r = ThreadLocalRandom.current().nextInt(3);
        this.mode = (r==0? Mode.SQUARE_LIKE : r==1? Mode.TRIANGLE_LIKE : Mode.CIRCLE_LIKE);
        // سرعت پایه
        setSpeed(BASE_SPEED_MSG);
    }
    @Override
    public int getId() {                 // <-- همیشه ID اصلی را برگردان
        return originalId;
    }
    public void dropProtection() { protectionActive = false; }

    public Packet getOriginal() { return original; }
    public boolean isProtectionActive(){ return protectionActive; }

    @Override public String getCompatibilityKey() {
        if (!protectionActive) return original.getCompatibilityKey();
        return switch (mode) {
            case SQUARE_LIKE   -> "square";
            case TRIANGLE_LIKE -> "triangle";
            case CIRCLE_LIKE   -> (ThreadLocalRandom.current().nextBoolean() ? "square" : "triangle");
        };
    }

    @Override protected void onEnterWire(Wire w) {
        if (!protectionActive) { original.setWire(w); return; }

        boolean compat = getCompatibilityKey().equals(w.getStartPortType());
        switch (mode) {
            case SQUARE_LIKE -> { setAccel(0); setSpeed(compat? 2.0 : 4.0); }
            case TRIANGLE_LIKE -> { setSpeed(2.0); setAccel(compat? 0.0 : TRIANGLE_ACCEL_INCOMPAT); }
            case CIRCLE_LIKE -> { setSpeed(2.0); setAccel(compat? CIRCLE_ACCEL_COMPAT : CIRCLE_ACCEL_INCOMPAT_START); }
        }
    }

    @Override public void updatePosition(double delta) {
        if (!protectionActive) {
            // رفتار مثل پکت اصلی (اما همین شیء باقی می‌ماند)
            original.wire = this.wire;
            original.progress = this.progress;
            original.direction = this.direction;
            original.offsetX = this.offsetX;
            original.offsetY = this.offsetY;
            original.speed = this.speed;
            original.accel = this.accel;
            original.updatePosition(delta);
            // sync back
            this.wire = original.wire;
            this.progress = original.progress;
            this.direction = original.direction;
            this.offsetX = original.offsetX;
            this.offsetY = original.offsetY;
            this.speed = original.speed;
            this.accel = original.accel;
            this.wireX = original.wireX;
            this.wireY = original.wireY;
            return;
        }
        super.updatePosition(delta);
        if (mode == Mode.CIRCLE_LIKE) {
            // نزولی‌شدن شتاب روی ناسازگار
            if (!getCompatibilityKey().equals(wire.getStartPortType()) && accel > 0) {
                setAccel(Math.max(0, accel - 0.15 * delta));
            }
        }
    }

    @Override public int getSize() {
        int base = original.getSize();
        return base * 2;
    }
    @Override public int getCoinValue() { return COIN_PROTECTED; }
}
