package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.Packet;
import org.example.model.PacketQueue;
import org.example.model.Port;
import org.example.model.Wire;
import org.example.util.Debug;

import java.util.*;

public class NetworkSystem {
    private final List<Port> inputPorts = new ArrayList<>();
    private final List<Port> outputPorts = new ArrayList<>();
    private final ArrayList<Packet> packetStorage = new ArrayList<>();
    private final Map<String, PacketQueue> inputQueues = new HashMap<>();
    private final Map<String, List<Wire>> outputWires = new HashMap<>();

    private boolean indicatorOn;
    private int storageCapacity = 5;

    private double x, y;
    private boolean isSourceSystem = false;
    private int type;
    private double packetsDropped;

    private GameEnv env;

    public NetworkSystem(double x, double y, int type) {
        this.type = type;
        double spacing = 120 / 4.0;

        if (type == 1) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + spacing, true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing, true));
            inputPorts.add(new Port("circle" , -1 , this , x - 15 , y + 3 * spacing  ,true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
            outputPorts.add(new Port("circle" , 1 , this , x  +120 , y + 3 * spacing  ,true));



        } else if (type == 2) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + spacing - 20, true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing - 20, true));
            inputPorts.add(new Port("square", -1, this, x - 15, y + 3 * spacing - 20, true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 4 * spacing - 20, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 3) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + spacing, true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing, true));
            inputPorts.add(new Port("circle" , -1 , this , x - 15 , y + 3 * spacing  ,true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
            outputPorts.add(new Port("circle" , 1 , this , x  +120 , y + 3 * spacing  ,true));
//            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing - 20, true));
//            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 4) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + spacing, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 5) {
            inputPorts.add(new Port("square", -1, this, x - 15,y + spacing , true));
            inputPorts.add(new Port("triangle", -1, this, x - 15, y + 2 * spacing, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
            outputPorts.add(new Port("triangle", 1, this, x + 120, y + 2 * spacing, true));
        } else if (type == 6) {
            inputPorts.add(new Port("square", -1, this, x - 15, y + 2 * spacing, true));
            outputPorts.add(new Port("square", 1, this, x + 120, y + spacing, true));
        }
        else if (type == 7){
            inputPorts.add(new Port("circle" , -1 , this , x - 15 , y + 3 * spacing  ,true));
            outputPorts.add(new Port("circle" , 1 , this , x  +120 , y + 3 * spacing  ,true));

        } else if (type == 8){
            inputPorts.add(new Port("square" , -1 , this , x  +120 , y + 3 * spacing  ,true));
            outputPorts.add(new Port("square" , 1 , this , x  +120 , y + 3 * spacing  ,true));
            inputPorts.add(new Port("circle" , -1 , this , x - 15 , y + 3 * spacing  ,true));

        }

        this.x = x - 6;
        this.y = y - 30;

        this.indicatorOn = false;

        outputWires.put("square", new ArrayList<>());
        outputWires.put("triangle", new ArrayList<>());
        outputWires.put("circle", new ArrayList<>());
    }

    public void setEnv(GameEnv env) {
        this.env = env;
    }

    public GameEnv getEnv() {
        return env;
    }

    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        if (packetStorage.isEmpty()) return null;

        // اول پکت‌های سازگار با این startPortType
        for (Packet packet : new ArrayList<>(packetStorage)) {
            if (packet.canEnterWireWithStartType(startPortType)) {
                packetStorage.remove(packet);
                return packet;
            }
        }
        // اگر نبود، هر پکت موجود (برخی پکت‌ها مثل محرمانه/حجیم سازگاری ندارند → قبول)
        Packet any = packetStorage.get(0);
        packetStorage.remove(0);
        return any;
    }

    public List<Wire> getAllConnectedWires() {
        List<Wire> all = new ArrayList<>();
        for (List<Wire> ws : outputWires.values()) all.addAll(ws);
        return all;
    }

    public void addOutputWire(Wire wire) {
        outputWires.get(wire.getStartPortType()).add(wire);
    }

    public void addInputWire(Wire wire) {
        // no-op, override in SinkSystem if needed
    }

    public void removeOutputWire(Wire wire) {
        List<Wire> list = outputWires.get(wire.getStartPortType());
        if (list != null) list.remove(wire);
        checkIndicator();
    }

    public void removeInputWire(Wire wire) {
        checkIndicator();
    }

    public boolean canStorePacket() {
        return (packetStorage.size() < storageCapacity && this.isEnabled());
    }

    public void addPacket(Packet packet) {

        if (canStorePacket()) {
            packetStorage.add(packet);
            Debug.log("[STORE]", Debug.p(packet) + " in " + Debug.sys(this) +
                    " size=" + packetStorage.size() + "/" + storageCapacity);
            String key = packet.getPortKey(); // به‌جای getType()
            if (key != null) {
                PacketQueue q = inputQueues.get(key);
                if (q != null) q.addPacket(packet);
            }
        } else {
            packetsDropped++;
            Debug.log("[DROP]", Debug.p(packet) + " at " + Debug.sys(this) +
                    " reason=storage_full dropped=" + packetsDropped);
        }
    }

    public double getPacketsDropped() {
        return packetsDropped;
    }

    public void removePacket(Packet packet) {
        packetStorage.remove(packet);
    }

    public void checkIndicator() {
        boolean allConnected = true;
        for (Port port : inputPorts) {
            if (port.getIsEmpty()) {
                allConnected = false;
                break;
            }
        }
        this.indicatorOn = allConnected;
    }

    public List<Port> getInputPorts() {
        return inputPorts;
    }

    public List<Port> getOutputPorts() {
        return outputPorts;
    }

    public boolean isIndicatorOn() {
        return indicatorOn;
    }

    public void setIndicatorOn(boolean indicatorOn) {
        this.indicatorOn = indicatorOn;
    }

    public int getPacketCount() {
        return packetStorage.size();
    }



    public boolean isSourceSystem() {
        return isSourceSystem;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public ArrayList<Packet> getPacketStorage() {
        return new ArrayList<>(packetStorage);
    }

    //***********************************************************************
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }



    private double reenableTimerSec = 0;

    public void disableFor(double sec) {
        setEnabled(false);
        reenableTimerSec = Math.max(reenableTimerSec, sec);
    }

    public void update() {
        // با فرض ~60fps (مثل AntiTrojan)
        if (!isEnabled() && reenableTimerSec > 0) {
            reenableTimerSec -= 1.0 / 60.0;
            if (reenableTimerSec <= 0) {
                setEnabled(true);
                reenableTimerSec = 0;
            }
        }
    }
    public double getReenableTimerSec() {
        return Math.max(0, reenableTimerSec);
    }
    public void setStorageCapacity(int capacity) {
        this.storageCapacity = Math.max(0, capacity);
    }
    public boolean isGenerating() { return false; }   // پیش‌فرض: تولید ندارد
    public void setGenerating(boolean generating) { /* no-op */ }
}
