package org.example.model.Packet;

import org.example.model.Wire;

import static org.example.model.ModelConfig.*;

public class TrianglePacket extends Packet {
    public TrianglePacket(double x, double y) {
        super(x, y);
        this.speed = 2.0;  // سرعت ثابت پایه
    }
    @Override public String getCompatibilityKey() { return "triangle"; }

    @Override protected void onEnterWire(Wire w) {
        boolean compat = "triangle".equals(w.getStartPortType());
        setSpeed(2.0);
        // شتاب روی سازگار 0، روی ناسازگار 0.4
        setAccel( compat ? 0.0 : TRIANGLE_ACCEL_INCOMPAT );
    }

    @Override public int getSize() { return 3; }
    @Override public int getCoinValue() { return COIN_TRIANGLE; }
    @Override public String getPortKey() { return "triangle"; }
}
