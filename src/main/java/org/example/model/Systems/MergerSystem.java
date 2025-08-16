package org.example.model.Systems;

import org.example.model.Packet.BitPacket;
import org.example.model.Packet.HeavyPacket10;
import org.example.model.Packet.HeavyPacket8;
import org.example.model.Packet.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergerSystem extends NetworkSystem {

    private final Map<String, List<BitPacket>> buckets = new HashMap<>();

    public MergerSystem(double x, double y) { super(x, y, 1); }

    @Override
    public void addPacket(Packet packet) {
        if (packet instanceof BitPacket bp) {
            String gid = bp.getGroupId();
            buckets.computeIfAbsent(gid, k -> new ArrayList<>()).add(bp);

            // اگر به تعداد لازم رسید، ادغام
            List<BitPacket> list = buckets.get(gid);
            if (list.size() >= bp.getBulkSize()) {
                int N = bp.getBulkSize();
                Packet heavy = (N >= 10) ? new HeavyPacket10(0 , 0) : new HeavyPacket8(0 , 0);
                super.addPacket(heavy);
                list.clear();
            }
            return;
        }
        super.addPacket(packet);
    }
}