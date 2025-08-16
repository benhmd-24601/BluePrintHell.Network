package org.example.model.Packet;

import org.example.model.ModelConfig;
import org.example.model.Wire;

import static org.example.model.ModelConfig.*;

public abstract class HeavyPacket extends Packet {
    private double distSinceDrift = 0.0;
    protected HeavyPacket(double x, double y) {
        super(x, y);
    }
    @Override public String getCompatibilityKey() { return null; } // سازگاری بی‌معنا
    @Override protected void onEnterWire(Wire w) { setSpeed(2.0); setAccel(0); }

    @Override public void updatePosition(double delta) {
        double prevProgress = this.progress;
        super.updatePosition(delta);

        // اثر عبور حجیم روی سیم: در deliverCurrentPacket شمارش می‌شود

        // نوع۲: drift هر فاصله‌ی مشخص
        if (this instanceof HeavyPacket10) {
            distSinceDrift += Math.abs(progress - prevProgress);
            if (distSinceDrift >= HEAVY_DRIFT_STEP_DIST) {
                distSinceDrift = 0;
                applyImpact(0, HEAVY_DRIFT_OFFSET); // انحراف عمودی کوچک
            }
        }
    }
}


