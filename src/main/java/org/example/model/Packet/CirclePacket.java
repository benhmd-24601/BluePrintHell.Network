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



    @Override public void updatePosition(double delta) {
        super.updatePosition(delta);
        // روی ناسازگار، شتاب نزولی تا صفر
        Wire w = getWire();
        if (w != null && !getPortKey().equals(w.getStartPortType())) {
            if (accel > 0) setAccel(Math.max(0, accel - 0.15 * delta));
        }
    }

    @Override public int getSize() { return 1; }
    @Override public int getCoinValue() { return COIN_CIRCLE; }
    @Override public String getPortKey() { return "circle"; }
    @Override public String getCompatibilityKey() { return "circle"; }

    @Override
    protected void onEnterWire(Wire w) {

    }

}
