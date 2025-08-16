package org.example.model.Packet    ;

import org.example.model.Wire;

import java.util.concurrent.ThreadLocalRandom;
import static org.example.model.ModelConfig.*;

public class CirclePacket extends Packet {

    public CirclePacket(double x, double y) {
        super(x, y);
        this.speed = 2.0;
        this.accel = 0.0;
    }
    private final String preferredPort = ThreadLocalRandom.current().nextBoolean() ? "square" : "triangle";

    @Override public String getCompatibilityKey() { return preferredPort; }

    @Override protected void onEnterWire(Wire w) {
        boolean compat = preferredPort.equals(w.getStartPortType());
        setSpeed(2.0);
        if (compat) {
            setAccel(CIRCLE_ACCEL_COMPAT);               // شتاب ثابت
        } else {
            // شتاب نزولی از 0.4 تا 0
            setAccel(Math.max(0.0, accel));              // شروع از 0.4 اگر اولین باره
            if (accel == 0.0) setAccel(CIRCLE_ACCEL_INCOMPAT_START);
        }
    }

    @Override public void updatePosition(double delta) {
        super.updatePosition(delta);
        // روی ناسازگار، شتاب نزولی تا صفر
        Wire w = getWire();
        if (w != null && !preferredPort.equals(w.getStartPortType())) {
            if (accel > 0) setAccel(Math.max(0, accel - 0.15 * delta));
        }
    }

    @Override public int getSize() { return 1; }
    @Override public int getCoinValue() { return COIN_CIRCLE; }
}
