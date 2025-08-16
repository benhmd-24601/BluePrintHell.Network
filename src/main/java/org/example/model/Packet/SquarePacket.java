package org.example.model.Packet;

import org.example.model.Wire;

import static org.example.model.ModelConfig.*;

public class SquarePacket extends Packet {
    public SquarePacket(double x, double y) {
        super(x, y);
        this.speed = 2.0;  // مقدار پایه
        this.accel = 0.0;
    }
    // پورت‌های سازگار: "square"
    @Override public String getCompatibilityKey() { return "square"; }

    @Override protected void onEnterWire(Wire w) {
        boolean compat = "square".equals(w.getStartPortType());
        // روی پورت سازگار سرعت 2، روی ناسازگار 4
        setAccel(0);
        setSpeed( compat ? 2.0 : 4.0 );
    }

    @Override public int getSize() { return 2; }
    @Override public int getCoinValue() { return COIN_SQUARE; }
    @Override public String getPortKey() { return "square"; }
}
