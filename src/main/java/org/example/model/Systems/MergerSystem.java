package org.example.model.Systems;

import org.example.model.Packet.BitPacket;
import org.example.model.Packet.HeavyPacket10;
import org.example.model.Packet.HeavyPacket8;
import org.example.model.Packet.Packet;
import org.example.model.Wire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergerSystem extends NetworkSystem {

    private final Map<String, List<BitPacket>> buckets = new HashMap<>();

    public MergerSystem(double x, double y) { super(x, y, 1);
        setStorageCapacity(Integer.MAX_VALUE); // یا مثلاً 1000
    }
    @Override
    public boolean canStorePacket() {
        return this.isEnabled(); // فقط وضعیت enable مهم است
    }

    @Override
    public void addPacket(Packet packet) {
        if (packet instanceof BitPacket bp) {
            // 1) بفرست داخل استوریج تا UI نشانش بدهد
            super.addPacket(bp);

            // 2) داخل سطل گروهی هم نگه دار
            String gid = bp.getGroupId();
            List<BitPacket> list = buckets.computeIfAbsent(gid, k -> new ArrayList<>());
            list.add(bp);

            // 3) به حد نصاب رسید → ادغام
            if (list.size() >= bp.getBulkSize()) {
                int N = bp.getBulkSize();

                // بیت‌های این گروه را از استوریج حذف کن تا فقط هِوی نمایش داده شود
                for (BitPacket b : list) removePacket(b);

                // هِویِ خروجی
                Packet heavy = (N >= 10) ? new HeavyPacket10(0, 0) : new HeavyPacket8(0, 0);
                super.addPacket(heavy);

                list.clear();
            }
            return;
        }
        // سایر پکت‌ها مثل قبل
        super.addPacket(packet);
    }

    // جلوی خروج «بیت‌ها» از مرجر را بگیر؛ فقط هِوی/سایر پکت‌ها اجازه خروج داشته باشند
    @Override
    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        for (Packet p : getPacketStorage()) { // کپی برمی‌گرداند
            if (p instanceof BitPacket) continue; // بیت‌ها نباید خارج شوند
            if (p.canEnterWireWithStartType(startPortType)) {
                removePacket(p);
                return p;
            }
        }
        return null;
    }
}