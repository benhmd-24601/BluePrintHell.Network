package org.example.model.Packet;

import org.example.model.ModelConfig;
import org.example.model.Wire;

import static org.example.model.ModelConfig.HEAVY1_ACCEL_ON_CURVE;

public class HeavyPacket8 extends HeavyPacket {
    public HeavyPacket8(double x, double y) { super(x, y); }

    @Override protected void onEnterWire(Wire w) {
        super.onEnterWire(w);
        // روی «سیم منحنی» شتاب ثابت
        if (w.isCurved()) setAccel(HEAVY1_ACCEL_ON_CURVE);
        else setAccel(0);
    }
    @Override public int getSize() { return 8; }
    @Override public int getCoinValue() { return ModelConfig.COIN_HEAVY8; }
}
