package org.example.model.Systems;

import org.example.model.Packet.BitPacket;
import org.example.model.Packet.HeavyPacket10;
import org.example.model.Packet.HeavyPacket8;
import org.example.model.Packet.Packet;
import org.example.model.Wire;

import java.util.*;

public class MergerSystem extends NetworkSystem {

    // هر گروه: لیست بیت‌ها + زمان ورود اولین بیت
    private static final long TIMEOUT_MS = 60_000L;

    private static final class GroupState {
        final List<BitPacket> bits = new ArrayList<>();
        long firstArriveMs = 0L;
    }

    private final Map<String, GroupState> buckets = new HashMap<>();
    private final Deque<Packet> flushQueue = new ArrayDeque<>();

    public MergerSystem(double x, double y) {
        super(x, y, 1);
    }

    /** مرجر ظرفیت نامحدود (تا وقتی enabled است) */
    @Override
    public boolean canStorePacket() {
        return this.isEnabled();
    }

    @Override
    public void addPacket(Packet packet) {
        if (packet instanceof BitPacket bp) {
            // 1) برای نمایش در استورج
            super.addPacket(bp);

            // 2) گروه‌بندی
            String gid = bp.getGroupId();
            GroupState g = buckets.computeIfAbsent(gid, k -> new GroupState());
            if (g.bits.isEmpty()) g.firstArriveMs = System.currentTimeMillis();
            g.bits.add(bp);

            // 3) تکمیل گروه → ادغام
            if (g.bits.size() >= bp.getBulkSize()) {
                int N = bp.getBulkSize();

                // حذف بیت‌ها از استورج
                for (BitPacket b : g.bits) removePacket(b);
                g.bits.clear();
                buckets.remove(gid);

                // افزودن هِوی
                Packet heavy = (N >= 10) ? new HeavyPacket10(0, 0) : new HeavyPacket8(0, 0);
                super.addPacket(heavy);
            }
            return;
        }

        // سایر پکت‌ها مثل قبل
        super.addPacket(packet);
    }

    /** هر فریم چک کن: اگر گروهی ۶۰ ثانیه موند، بیت‌هاش رو برای خروج تدریجی صف کن */
    @Override
    public void update() {
        super.update();

        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, GroupState>> it = buckets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, GroupState> e = it.next();
            GroupState g = e.getValue();

            if (!g.bits.isEmpty() && now - g.firstArriveMs >= TIMEOUT_MS) {
                // زمان گروه تمام شده → برای خروج تدریجی صف کن
                for (BitPacket b : g.bits) {
                    // در استورج هستند؛ فقط در صف خروج هم قرارشان می‌دهیم
                    flushQueue.addLast(b);
                }
                g.bits.clear();
                it.remove(); // گروه را حذف کن
            }
        }
    }

    /**
     * خروج از مرجر:
     * 1) اگر فلش‌کیو خالی نیست → بیت‌ها را (بدون سخت‌گیری سازگاری) اولویت بده.
     * 2) در غیر این صورت، پکت غیر بیت که با startPortType سازگار است را بده.
     */
    @Override
    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
        // 1) اولویت با بیت‌های در حال فلش
        if (!flushQueue.isEmpty()) {
            Iterator<Packet> it = flushQueue.iterator();
            while (it.hasNext()) {
                Packet p = it.next();
                // اگر در استورج هنوز موجود است (ممکن است جایی حذف شده باشد)
                // و اجازه ورود به این startPortType را دارد:
                if (getPacketStorage().contains(p) && p.canEnterWireWithStartType(startPortType)) {
                    it.remove();
                    removePacket(p);   // از استورج خارج کن
                    return p;
                }
            }
            // اگر هیچ‌کدام به این پورت نخوردند، اجازه بده سیم‌های بعدی سراغشان بیایند
        }

        // 2) پکت‌های غیر بیتِ سازگار
        for (Packet p : getPacketStorage()) {
            if (p instanceof BitPacket) continue; // بیت‌ها فقط از طریق فلش‌کیو خارج می‌شوند
            if (p.canEnterWireWithStartType(startPortType)) {
                removePacket(p);
                return p;
            }
        }

        return null;
    }
}
