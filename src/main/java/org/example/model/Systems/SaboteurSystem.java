package org.example.model.Systems;

import org.example.model.Packet.Packet;
import org.example.model.Packet.ProtectedPacket;
import org.example.model.Packet.TrojanPacket;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Wire;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.example.model.ModelConfig.*;

public class SaboteurSystem extends NetworkSystem {
    private final Random rnd = new Random();

    public SaboteurSystem(double x, double y) { super(x, y, 1); }

    @Override
    public void addPacket(Packet packet) {
        // روی محافظت‌شده اثر ندارد
        if (packet instanceof ProtectedPacket pp && pp.isProtectionActive()) {
            super.addPacket(packet);
            return;
        }

        // اگر نویز ندارد، یک واحد تزریق
        if (packet.getNoiseLevel() <= 0) packet.setNoiseLevel(1);

        // با احتمال خاص به Troj تبدیل
        if (!(packet instanceof TrojanPacket) && rnd.nextDouble() < SABOTEUR_TROJAN_PROB) {
            TrojanPacket troj = new TrojanPacket(packet , 0 , 0);
            super.addPacket(troj);
            return;
        }
        super.addPacket(packet);
    }

    @Override
    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        // برعکس معمول: «عمداً» پکت ناسازگار بفرست
        ArrayList<Packet> store = super.getPacketStorage();
        if (store.isEmpty()) return null;

        for (Packet p : store) {
            if (!p.canEnterWireWithStartType(startPortType)) {
                // بردار و برگردان
                removePacket(p);
                return p;
            }
        }
        // اگر همه سازگار بودند، یکی را الکی بفرست
        Packet p = store.get(0);
        removePacket(p);
        return p;
    }
}
