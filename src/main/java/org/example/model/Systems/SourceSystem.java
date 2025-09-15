package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.*;
import org.example.model.Port;
import org.example.model.Wire;
import org.example.util.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SourceSystem extends NetworkSystem {
    private int packetCount;
    private int generationFrequency;
    private final int type;
    private int tickCounter = 0;
    private boolean isGenerating = false;
    private GameEnv env;
    private final ArrayList<Packet> allPackets = new ArrayList<>();
    private int allpackets;

    private int nextKind = 0;

    // ← شمارندهٔ پیام‌رسان‌ها برای تولید Heavy هر 3 تا
    private int messengerSinceLastHeavy = 0;
    private final Random rnd = new Random();

    public SourceSystem(double x, double y, int packetCount, int generationFrequency , int type) {
        super(x, y, 1);
        this.allpackets = packetCount;
        this.packetCount = packetCount;
        this.generationFrequency = generationFrequency;
        this.type = type;
        getInputPorts().clear();
    }

    public int getAllpackets() { return allpackets; }
    @Override public int getPacketCount() { return packetCount; }

    public List<Packet> updateAndGeneratePackets() {
        tickCounter++;
        List<Packet> placed = new ArrayList<>(); // فقط آن‌هایی که واقعاً روی سیم گذاشته می‌شوند

        if (tickCounter >= generationFrequency && packetCount > 0) {
            tickCounter = 0;
if (type==1) {
    // 1) ساخت پیام‌رسان به نوبت
    Packet messenger;
    if (nextKind == 0) {
        messenger = new SquarePacket(getX() + 60, getY() + 80);
    } else if (nextKind == 1) {
        messenger = new TrianglePacket(getX() + 60, getY() + 80);
    } else {
        messenger = new CirclePacket(getX() + 60, getY() + 80);
    }
    nextKind = (nextKind + 1) % 3;
    // تلاش برای گذاشتن روی سیم «سازگار»
    Wire wMsg = findCompatibleFreeWireForMessenger(messenger);
    if (wMsg != null && (wMsg.getEndSystem() == null || wMsg.getEndSystem().isEnabled())) {
        wMsg.setCurrentPacket(messenger);
        Debug.log("[WIRE]", "source put " + Debug.p(messenger) + " on " + Debug.wire(wMsg));
        placed.add(messenger);
    } else {
        // اگر الان سیم نبود، صف کن تا بعداً برداشته شود
        super.addPacket(messenger);
        Debug.log("[SRC]", "queued messenger " + Debug.p(messenger));
    }
    packetCount--;
}
else if (type == 2){
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

        }

        allPackets.addAll(placed);
        return placed;
    }

    /** وایر «سازگار» برای پیام‌رسان‌ها */
    private Wire findCompatibleFreeWireForMessenger(Packet packet) {
        GameEnv envRef = getEnv();
        if (envRef == null) return null;
        for (Port out : getOutputPorts()) {
            Wire w = envRef.findWireByStartPort(out);
            if (w == null) continue;
            if (w.isBusy()) continue;
            if (w.getEndSystem() != null && !w.getEndSystem().isEnabled()) continue;
            if (packet.canEnterWireWithStartType(w.getStartPortType())) return w;
        }
        return null;
    }

    /** اولین وایر آزاد، بدون توجه به سازگاری (برای Heavy) */
    private Wire findAnyFreeWire() {
        GameEnv envRef = getEnv();
        if (envRef == null) return null;
        for (Port out : getOutputPorts()) {
            Wire w = envRef.findWireByStartPort(out);
            if (w == null) continue;
            if (w.isBusy()) continue;
            if (w.getEndSystem() != null && !w.getEndSystem().isEnabled()) continue;
            return w;
        }
        return null;
    }

    /** تا وقتی در صف هستند، Heavy باید با هر وایری خارج شود (سازگاری مهم نیست) */
    @Override
    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        ArrayList<Packet> store = getPacketStorage();

        // 1) اول Heavy، بدون توجه به startPortType
        for (Packet p : store) {
            if (p instanceof HeavyPacket) {
                removePacket(p);
                return p;
            }
        }
        // 2) بعد پیام‌رسان سازگار
        for (Packet p : store) {
            if (p.canEnterWireWithStartType(startPortType)) {
                removePacket(p);
                return p;
            }
        }
        return null;
    }

    public void setGenerating(boolean generating) { isGenerating = generating; }
    public ArrayList<Packet> getAllPackets() { return allPackets; }
    public boolean isGenerating() { return isGenerating; }
    @Override public boolean isSourceSystem() { return true; }
    public void reset() { tickCounter = 0; }
    public void setEnv(GameEnv env) { this.env = env; }

    public int getType() {
        return type;
    }
}
