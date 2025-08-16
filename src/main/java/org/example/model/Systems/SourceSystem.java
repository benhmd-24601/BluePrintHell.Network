package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.CirclePacket;
import org.example.model.Packet.Packet;
import org.example.model.Packet.SquarePacket;
import org.example.model.Packet.TrianglePacket;
import org.example.model.Port;
import org.example.model.Wire;

import java.util.ArrayList;
import java.util.List;

public class SourceSystem extends NetworkSystem {
    private int packetCount;
    private int generationFrequency;
    private int tickCounter = 0;
    private boolean isGenerating = false;
    private GameEnv env;
    private final ArrayList<Packet> allPackets = new ArrayList<>();
    //    private final JButton generateButton;
    private int allpackets;

    private int nextKind = 0;

    public SourceSystem(double x, double y, int packetCount, int generationFrequency) {
        super(x, y, 1);
        this.allpackets = packetCount;
        this.packetCount = packetCount;
        this.generationFrequency = generationFrequency;
        getInputPorts().clear();

    }

    public int getAllpackets() {
        return allpackets;
    }

    @Override
    public int getPacketCount() {
        return packetCount;
    }

    private boolean alternateType = true;

    public List<Packet> updateAndGeneratePackets() {
        tickCounter++;
        List<Packet> generated = new ArrayList<>();
        if (tickCounter >= generationFrequency && packetCount > 0) {
            tickCounter = 0;

            Packet packet;
            if (nextKind == 0) {
                packet = new SquarePacket(getX() + 60, getY() + 80);
            } else if (nextKind == 1) {
                packet = new TrianglePacket(getX() + 60, getY() + 80);
            } else {
                packet = new CirclePacket(getX() + 60, getY() + 80);
            }
            nextKind = (nextKind + 1) % 3;

            Wire suitableWire = findSuitableWireForPacket(packet);
            if (suitableWire != null && (suitableWire.getEndSystem() == null || suitableWire.getEndSystem().isEnabled())) {
                // پیشنهاد: برای اطمینان از فعال‌بودن مقصد همین‌جا هم چک شود
                suitableWire.setCurrentPacket(packet);  // setWire → onEnterWireConfigure + بوست
                generated.add(packet);
                packetCount--;
            } else {
                // سیمی برای حرکت پیدا نشد — می‌تونی packet را دور بیندازی
                // یا برای تلاش مجدد نگه داری؛ فعلاً کاری نمی‌کنیم
            }
        }
        allPackets.addAll(generated);
        return generated;
    }

    private Wire findSuitableWireForPacket(Packet packet) {
        for (Port out : getOutputPorts()) {
            if (out.getType().equals(packet.getPortKey())) {
                Wire connected = env.findWireByStartPort(out);
                if (connected != null && !connected.isBusy()) return connected;
            }
        }
        return null;
    }

    public void setGenerating(boolean generating) {
        isGenerating = generating;
    }

    public ArrayList<Packet> getAllPackets() {
        return allPackets;
    }

    public boolean isGenerating() {
        return isGenerating;
    }

    //    public JButton getGenerateButton() { return generateButton; }
    @Override
    public boolean isSourceSystem() {
        return true;
    }

    public void reset() {
        tickCounter = 0;
    }

    public void setEnv(GameEnv env) {
        this.env = env;
    }
}
