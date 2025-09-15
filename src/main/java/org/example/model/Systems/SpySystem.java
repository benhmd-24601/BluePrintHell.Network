package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.Packet;
import org.example.model.Packet.ProtectedPacket;
import org.example.model.Packet.SecretPacket1;
import org.example.model.Packet.SecretPacket2;
import org.example.model.Port;
import org.example.model.Wire;
import org.example.util.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpySystem extends NetworkSystem {

    public SpySystem(double x, double y) {
        super(x, y, 1); // پورت‌ها مثل معمول
    }

//    @Override
//    public void addPacket(Packet packet) {
//        GameEnv env = getEnv();
//        // محرمانه‌ها نابود می‌شوند
//        if (packet instanceof SecretPacket1 || packet instanceof SecretPacket2) {
//            Debug.log("[SPY]", "destroy secret " + Debug.p(packet));
//            env.markAsLost(packet, "spy_secret");
//            return;
//        }
//
//        // روی محافظت‌شده اثر ندارد → رفتار عادی
//        if (packet instanceof ProtectedPacket pp && pp.isProtectionActive()) {
//            super.addPacket(packet);
//            return;
//        }
//
//        // تله‌پورت: یکی از SpySystemهای دیگر را پیدا کن و از آن‌جا خارج شو
//        List<SpySystem> spies = new ArrayList<>();
//        for (NetworkSystem s : env.getSystems())
//            if (s instanceof SpySystem spy && s != this) spies.add(spy);
//
//        if (spies.isEmpty()) { super.addPacket(packet); return; }
//
//        SpySystem exit = spies.get(new Random().nextInt(spies.size()));
//        List<Wire> candidates = new ArrayList<>();
//        for (Port out : exit.getOutputPorts()) {
//            Wire w = env.findWireByStartPort(out);
//            if (w == null) continue;
//            if (w.isBusy()) continue;
//            if (w.getEndSystem() != null && !w.getEndSystem().isEnabled()) continue;
//
//            // فقط اگر سازگار است اجازه بده
//            if (packet.canEnterWireWithStartType(w.getStartPortType())) {
//                candidates.add(w);
//            }
//        }
//
//        if (!candidates.isEmpty()) {
//            Wire w = candidates.get(new Random().nextInt(candidates.size())); // اگر چندتا بود، یکی تصادفی
//            w.setCurrentPacket(packet);
//            packet.setDirectionForward();
//            Debug.log("[SPY]", "teleport " + Debug.p(packet) + " to " + Debug.sys(exit) +
//                    " via " + Debug.wire(w));
//            return;
//        }
//        // اگر نشد، عادی نگه دار
//        super.addPacket(packet);
//    }
//@Override
//public void addPacket(Packet packet) {
//    GameEnv env = getEnv();
//
//    // اسرار را نابود کن (مثل قبل)
//    if (packet instanceof SecretPacket1 || packet instanceof SecretPacket2) {
//        Debug.log("[SPY]", "destroy secret " + Debug.p(packet));
//        env.markAsLost(packet, "spy_secret");
//        return;
//    }
//
//    // اگر Protected و هنوز محافظت فعاله → تلاش برای خروج از همین Spy روی وایر «سازگار»
//    if (packet instanceof ProtectedPacket pp && pp.isProtectionActive()) {
//        for (Port out : getOutputPorts()) {
//            Wire w = env.findWireByStartPort(out);
//            if (w == null || w.isBusy()) continue;
//            if (w.getEndSystem() != null && !w.getEndSystem().isEnabled()) continue;
////            if (!packet.canEnterWireWithStartType(w.getStartPortType())) continue; // سازگار
//
//            w.setCurrentPacket(packet);
//            packet.setDirectionForward();
//            Debug.log("[SPY]", "emit protected " + Debug.p(packet) + " via " + Debug.wire(w));
//            return;
//        }
//        // اگر وایر سازگار نبود، نگه‌دار
//        super.addPacket(packet);
//        return;
//    }
//
//    // سایر انواع پکت → تلپورت به یک Spy دیگر و فقط ذخیره شوند
//    List<SpySystem> spies = new ArrayList<>();
//    for (NetworkSystem s : env.getSystems())
//        if (s instanceof SpySystem spy && s != this) spies.add(spy);
//
//    if (!spies.isEmpty()) {
//        SpySystem exit = spies.get(new Random().nextInt(spies.size()));
//        exit.acceptTeleport(packet);// فقط ذخیره در مقصد
//        this.removePacket(packet);
//        Debug.log("[SPY]", "teleport store " + Debug.p(packet) + " -> " + Debug.sys(exit));
//        return;
//    }
//
//    // اگر اسپای دیگری نبود، همین‌جا ذخیره کن
//    super.addPacket(packet);
//}

@Override
public void addPacket(Packet packet) {
    GameEnv env = getEnv();

    // 1) اسرار را نابود کن
    if (packet instanceof SecretPacket1 || packet instanceof SecretPacket2) {
        Debug.log("[SPY]", "destroy secret " + Debug.p(packet));
        env.markAsLost(packet, "spy_secret");
        return;
    }

    // 2) اگر Protected و محافظت فعال → خروج یا استوریج
    if (packet instanceof ProtectedPacket pp && pp.isProtectionActive()) {
        if (!tryEgressElseStore(packet)) {
            Debug.log("[SPY]", "store protected (no free compatible wire) " + Debug.p(packet));
        }
        return;
    }

    // 3) اگر قبلاً تلپورت شده → دیگر تلپورت نکن؛ خروج یا استوریج
    if (packet.isTeleported()) {
        if (!tryEgressElseStore(packet)) {
            Debug.log("[SPY]", "store teleported (no free compatible wire) " + Debug.p(packet));
        }
        return;
    }

    // 4) اولین بار تلپورت
    List<SpySystem> others = new ArrayList<>();
    for (NetworkSystem s : env.getSystems())
        if (s instanceof SpySystem spy && spy != this) others.add(spy);

    if (!others.isEmpty()) {
        SpySystem exit = others.get(new Random().nextInt(others.size()));
        packet.markTeleported();          // ← فقط همین یک بار!
        exit.acceptTeleport(packet);      // مقصد خودش تصمیم می‌گیرد خروج کند یا ذخیره
        Debug.log("[SPY]", "teleport once " + Debug.p(packet) + " -> " + Debug.sys(exit));
        return;
    }

    // اگر Spy دیگری نبود، نگه‌داری
    super.addPacket(packet);
}
//    @Override
//    public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
//        // فقط Protectedِ محافظت‌شده و سازگار اجازه خروج دارند
//        ArrayList<Packet> store = super.getPacketStorage(); // کپی
//        for (Packet p : store) {
//            if (p instanceof ProtectedPacket pp && pp.isProtectionActive()
//                    && p.canEnterWireWithStartType(startPortType)) {
//                super.removePacket(p); // حذف از استوریج واقعی
//                return p;
//            }
//        }
//        return null; // بقیه را بیرون نده
//    }
@Override
public Packet getNextPacketForWire(String startPortType, List<Wire> allWires) {
    ArrayList<Packet> store = super.getPacketStorage();
    for (Packet p : store) {
        boolean canGo =
                (p instanceof ProtectedPacket pp && pp.isProtectionActive())
                        || p.isTeleported(); // ← اجازهٔ خروج برای تلپورت‌شده‌ها

        if (canGo && p.canEnterWireWithStartType(startPortType)) {
            super.removePacket(p);
            return p;
        }
    }
    return null;
}

    public void acceptTeleport(Packet packet) {
        if (!packet.isTeleported()) packet.markTeleported(); // safety
        if (!tryEgressElseStore(packet)) {
            Debug.log("[SPY]", "accept & store (no egress wire) " + Debug.p(packet));
        }
    }

    private boolean tryEgressElseStore(Packet packet) {
        GameEnv env = getEnv();
        for (Port out : getOutputPorts()) {
            Wire w = env.findWireByStartPort(out);
            if (w == null || w.isBusy()) continue;
            if (w.getEndSystem() != null && !w.getEndSystem().isEnabled()) continue;
            if (!packet.canEnterWireWithStartType(w.getStartPortType())) continue;

            w.setCurrentPacket(packet);
            packet.setDirectionForward();
            Debug.log("[SPY]", "egress " + Debug.p(packet) + " via " + Debug.wire(w));
            return true;
        }
        super.addPacket(packet);
        return false;
    }

}
