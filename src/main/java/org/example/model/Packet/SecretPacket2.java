package org.example.model.Packet;

import org.example.model.Wire;

import java.util.List;
import static org.example.model.ModelConfig.*;

public class SecretPacket2 extends Packet {
    public SecretPacket2(double x, double y) { super(x, y); }

    @Override public String getCompatibilityKey() { return null; }
    @Override protected void onEnterWire(Wire w) { setSpeed(2.0); setAccel(0); }

    @Override public void updatePosition(double delta) {
        // حفظ فاصله با پکت‌های دیگر روی همان سیم (جلو/عقب رفتن جزئی)
        if (wire != null) {
            Packet nearest = null;
            double nearestDist = Double.MAX_VALUE;
            List<Packet> all = wire.getEnv().getPackets();
            for (Packet p : all) {
                if (p == this) continue;
                if (p.getWire() != wire) continue;
                double d = Math.abs(p.getProgress() - this.getProgress());
                if (d < nearestDist) { nearestDist = d; nearest = p; }
            }
            if (nearest != null) {
                if (nearestDist < SECRET2_DESIRED_GAP) {
                    setDirectionBackward();  // کمی عقب برو
                } else {
                    setDirectionForward();
                }
            }
        }
        super.updatePosition(delta);
    }

    @Override public int getSize() { return 6; }
    @Override public int getCoinValue() { return COIN_SECRET2; }
}
