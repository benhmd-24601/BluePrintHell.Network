package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.Packet;
import org.example.model.Packet.TrojanPacket;

import static org.example.model.ModelConfig.*;

public class AntiTrojanSystem extends NetworkSystem {
    private double cooldown = 0.0;

    public AntiTrojanSystem(double x, double y) { super(x, y, 1); }

    @Override
    public void update() {
        if (!isEnabled()) {
            // شمارش معکوس خنک‌سازی
            if (cooldown > 0) cooldown -= 1.0/60.0; // فرض loop ~60fps
            if (cooldown <= 0) setEnabled(true);
            return;
        }

        // جست‌وجوی تروجان نزدیک
        GameEnv env = getEnv();
        for (Packet p : env.getPackets()) {
            if (!(p instanceof TrojanPacket troj)) continue;
            double dx = p.getX() - getX();
            double dy = p.getY() - getY();
            if (Math.hypot(dx, dy) <= ANTITROJAN_RADIUS) {
                // تبدیل به پیام‌رسان اصلی
                Packet orig = troj.getOriginal();
                // جایگزینی درجا
                orig.setWire(p.getWire());
                orig.setProgress(p.getProgress());
                orig.setDirection(+1);
                orig.setSpeed(Math.max(SPEED_MIN, orig.getInstantSpeed()));
                orig.setAccel(0);
                if (orig.getWire() != null && orig.getWire().getCurrentPacket() == p) {
                    orig.getWire().setCurrentPacket(orig);
                }
                env.getPackets().remove(p);
                env.getPackets().add(orig);

                // خودم را غیرفعال کن
                setEnabled(false);
                cooldown = ANTITROJAN_COOLDOWN;
                break;
            }
        }
    }
}
