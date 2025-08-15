package org.example.model;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    public SourceSystem(double x, double y, int packetCount, int generationFrequency) {
        super(x, y, 1);
        this.allpackets = packetCount;
        this.packetCount = packetCount;
        this.generationFrequency = generationFrequency;
        getInputPorts().clear();

//        generateButton = new JButton("◀");
//        generateButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                boolean canStart = true;
//                for (NetworkSystem system : env.getSystems()) {
//                    if (!system.isIndicatorOn() && !system.isSourceSystem()) {
//                        canStart = false; break;
//                    }
//                }
//                if (canStart) {
//                    isGenerating = !isGenerating;
//                    generateButton.setText(isGenerating ? "❚❚" : "◀");
//                    generateButton.setToolTipText(isGenerating ? "Pause" : "Play");
//                }
//            }
//        });
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
            String type = alternateType ? "square" : "triangle";
            alternateType = !alternateType;

            Packet packet = new Packet(type, getX() + 60, getY() + 80);
            Wire suitableWire = findSuitableWireForPacket(packet);
            if (suitableWire != null) {
                suitableWire.setCurrentPacket(packet);
                packet.setWire(suitableWire);
                generated.add(packet);
                packetCount--;
            }
        }
        allPackets.addAll(generated);
        return generated;
    }

    private Wire findSuitableWireForPacket(Packet packet) {
        for (Port out : getOutputPorts()) {
            if (out.getType().equals(packet.getType())) {
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
