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
        // 4) روی Protectedِ فعال اثری ندارد → پاس‌ترو
        if (packet instanceof ProtectedPacket pp && pp.isProtectionActive()) {
            super.addPacket(packet);
            return;
        }

        // 3) اگر نویز صفر است، یک واحد تزریق
        if (packet.getNoiseLevel() <= 0) {
            packet.setNoiseLevel(1);
        }

        // 2) با احتمال 1/3 به Trojan تبدیل شود
        // 2) با احتمال 1/3 (هر بار مستقل) به Trojan تبدیل شود
        if (!(packet instanceof TrojanPacket)) {
            int roll = 1 + rnd.nextInt(3); // 1..3
            if (roll == 1) {               // فقط وقتی 1 شد
                TrojanPacket troj = new TrojanPacket(packet, 0, 0);
                troj.setId(packet.getId());
                super.addPacket(troj);
                return;
            }
        }

        // بقیهٔ مواقع: همان پکت را ذخیره کن
        super.addPacket(packet);
    }

    @Override
    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        // 1) همهٔ پکت‌های غیرمحافظت‌شده باید از سیم «ناسازگار» بروند.
        //    Protectedِ فعال را دست نمی‌زنیم و اجازه می‌دهیم اگر سازگار بود عبور کند.

        ArrayList<Packet> store = super.getPacketStorage();
        if (store.isEmpty()) return null;

        // اولویت: اگر پکت Protectedِ فعال و «سازگار» هست، همان را بده (اثر نداشتن روی Protected)
        for (Packet p : store) {
            if (p instanceof ProtectedPacket pp && pp.isProtectionActive()
                    && p.canEnterWireWithStartType(startPortType)) {
                removePacket(p);
                return p;
            }
        }

        // سپس: یک پکت «ناسازگار» (غیر Protected فعال) پیدا کن و بده
        for (Packet p : store) {
            boolean isProtectedActive = (p instanceof ProtectedPacket pp) && pp.isProtectionActive();
            if (!isProtectedActive && !p.canEnterWireWithStartType(startPortType)) {
                removePacket(p);
                return p;
            }
        }

        // اگر هیچ ناسازگاری موجود نبود، چیزی رو این سیم نفرست
        // (می‌ذاریم دفعهٔ بعد که سیم/پکت ناسازگار شد حرکت کنند)
        return null;
    }
}

