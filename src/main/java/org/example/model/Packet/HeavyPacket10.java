package org.example.model.Packet;

import org.example.model.ModelConfig;

public class HeavyPacket10 extends HeavyPacket {
    public HeavyPacket10(double x, double y) { super(x, y); }

    @Override public int getSize() { return 10; }
    @Override public int getCoinValue() { return ModelConfig.COIN_HEAVY10; }
}
