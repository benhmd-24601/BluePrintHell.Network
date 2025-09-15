package org.example.model.Systems;


import org.example.model.GameEnv;
import org.example.model.Packet.*;
import org.example.model.Port;
import org.example.model.Wire;
import org.example.util.Debug;

import java.util.ArrayList;
import java.util.List;

public class SourceSystemWithHeavy extends NetworkSystem {
    private int packetCount;
    private int generationFrequency;
    private int tickCounter = 0;
    private boolean isGenerating = false;
    private GameEnv env;
    private final ArrayList<Packet> allPackets = new ArrayList<>();
    private int allpackets;

    public SourceSystemWithHeavy(double x, double y, int packetCount, int generationFrequency) {
        super(x, y, 1);
        this.allpackets = packetCount;   // برای آمار/پایان بازی
        this.packetCount = packetCount;  // تعداد باقی‌مانده برای ساخت
        this.generationFrequency = generationFrequency;
        getInputPorts().clear();
    }

    public int getAllpackets() { return allpackets; }
    @Override public int getPacketCount() { return packetCount; }

    /** فقط HeavyPacket8 تولید می‌کند */
    public List<Packet> updateAndGeneratePackets() {
        tickCounter++;
        List<Packet> placed = new ArrayList<>();

        if (tickCounter >= generationFrequency && packetCount > 0) {
            tickCounter = 0;

            // فقط هِوی ۸
            Packet heavy = new HeavyPacket8(getX() + 60, getY() + 80);

            // بدون توجه به سازگاری: اولین سیم آزاد
            Wire w = findAnyFreeWire();
            if (w != null && (w.getEndSystem() == null || w.getEndSystem().isEnabled())) {
                w.setCurrentPacket(heavy);
                // برای رندر/آپدیت
                getEnv().getPackets().add(heavy);
                Debug.log("[WIRE]", "source put HEAVY " + Debug.p(heavy) + " on " + Debug.wire(w));
                placed.add(heavy);
            } else {
                // سیم نبود → صف
                super.addPacket(heavy);
                Debug.log("[SRC]", "queued HEAVY " + Debug.p(heavy));
            }

            packetCount--;
        }

        allPackets.addAll(placed);
        return placed;
    }

    /** اولین وایر آزاد، بدون توجه به سازگاری (برای Heavy) */
    private Wire findAnyFreeWire() {
        GameEnv envRef = getEnv();
        if (envRef == null) return null;
        for (Port out : getOutputPorts()) {
            Wire w = envRef.findWireByStartPort(out);
            if (w == null || w.isBusy()) continue;
            if (w.getEndSystem() != null && !w.getEndSystem().isEnabled()) continue;
            return w;
        }
        return null;
    }

    /** Heavyهای صف‌شده با هر وایری خارج شوند (سازگاری مهم نیست) */
    @Override
    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        ArrayList<Packet> store = getPacketStorage();
        for (Packet p : store) {
            if (p instanceof HeavyPacket) {
                removePacket(p);
                return p; // اهمیتی به startPortType نمی‌دهیم
            }
        }
        return null; // چون دیگر مسنجر تولید نمی‌کنیم
    }

    public void setGenerating(boolean generating) { isGenerating = generating; }
    public ArrayList<Packet> getAllPackets() { return allPackets; }
    public boolean isGenerating() { return isGenerating; }
    @Override public boolean isSourceSystem() { return true; }
    public void reset() { tickCounter = 0; }
    public void setEnv(GameEnv env) { this.env = env; }
}
