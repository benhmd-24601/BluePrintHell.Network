package org.example.model.Packet;

import org.example.model.Systems.NetworkSystem;
import org.example.model.Wire;

import static org.example.model.ModelConfig.*;

public class SecretPacket1 extends Packet {
    public SecretPacket1(double x, double y) { super(x, y); }

    @Override public String getCompatibilityKey() { return null; } // با هیچ پورتی خاص نیست
    @Override protected void onEnterWire(Wire w) { setSpeed(2.0); setAccel(0); }

    @Override public void updatePosition(double delta) {
        // اگر مقصد ذخیره دارد، نزدیک ورودی نگه می‌دارد
        if (wire != null && isGoingForward()) {
            NetworkSystem dst = wire.getEndSystem();
            if (dst != null && !dst.getPacketStorage().isEmpty()) {
                // توقّف در فاصله‌ی مشخص
                double stopAt = Math.max(0, wire.getLength() - SECRET_APPROACH_HOLD_DIST);
                double totalLen = wire.getLength();
                double dp = speed * delta;
                progress = Math.min(progress + dp, stopAt);
                // جایابی
                double t = totalLen==0 ? 1.0 : (progress/totalLen);
                double sx=wire.getStartx(), sy=wire.getStarty(), ex=wire.getEndX(), ey=wire.getEndY();
                wireX = sx+(ex-sx)*t; wireY = sy+(ey-sy)*t;
                return;
            }
        }
        super.updatePosition(delta);
    }

    @Override public int getSize() { return 4; }
    @Override public int getCoinValue() { return COIN_SECRET1; }
}
