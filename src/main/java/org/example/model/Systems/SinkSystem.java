package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.BitPacket;
import org.example.model.Packet.Packet;
import org.example.model.Wire;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

public class SinkSystem extends NetworkSystem {
    private final List<Packet> receivedPackets = new ArrayList<>();
    private int sinkCapacity = 10;

    public SinkSystem(double x, double y, int capacity) {
        super(x, y, 3);
        this.sinkCapacity = capacity;
        getOutputPorts().clear();
    }

    @Override
    public void addPacket(Packet packet) {
        GameEnv env = getEnv();

        if (packet instanceof BitPacket bp) {
            // طبق خواسته‌ی تو: رسیدن «بیت‌پکت» به سینک → یک جریمه‌ی ویژه
            // فرمول ارائه‌شده: N - N * N^(1/N)  (توجه: این مقدار منفی می‌شود)
            double loss = lossFromBitArrival(bp.getBulkSize());
            env.increasePacketLoss(loss);
            return; // بیت‌ها در سینک ذخیره نمی‌شوند
        }

        // سایر پکت‌ها مثل قبل
        if (receivedPackets.size() < sinkCapacity) {
            receivedPackets.add(packet);
        }
    }

    @Override public void addInputWire(Wire wire) { }

    public List<Packet> getReceivedPackets() { return receivedPackets; }
    public int getTotalReceived() { return receivedPackets.size(); }
    public int getSinkCapacity() { return sinkCapacity; }

    // --------- helper ----------
    private double lossFromBitArrival(int bulkSize) {
        // فرمول پیشنهادی شما: N - N * N^(1/N)
        // نکته: برای N=8 یا 10 مقدار منفی می‌شود؛ تا وقتی فرمول نهایی را تأیید کنی، منفی را صفر می‌کنیم.
        double v = bulkSize - bulkSize * Math.pow(bulkSize, 1.0 / bulkSize);
        return Math.max(0.0, v);
    }

}