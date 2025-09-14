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

    @Override
    public void addPacket(Packet packet) {
        GameEnv env = getEnv();
        // محرمانه‌ها نابود می‌شوند
        if (packet instanceof SecretPacket1 || packet instanceof SecretPacket2) {
            Debug.log("[SPY]", "destroy secret " + Debug.p(packet));
            env.markAsLost(packet, "spy_secret");
            return;
        }

        // روی محافظت‌شده اثر ندارد → رفتار عادی
        if (packet instanceof ProtectedPacket pp && pp.isProtectionActive()) {
            super.addPacket(packet);
            return;
        }

        // تله‌پورت: یکی از SpySystemهای دیگر را پیدا کن و از آن‌جا خارج شو
        List<SpySystem> spies = new ArrayList<>();
        for (NetworkSystem s : env.getSystems())
            if (s instanceof SpySystem spy && s != this) spies.add(spy);

        if (spies.isEmpty()) { super.addPacket(packet); return; }

        SpySystem exit = spies.get(new Random().nextInt(spies.size()));
        // یک وایر خروجی از exit پیدا کن که بیکار باشد
        for (Port out : exit.getOutputPorts()) {
            Wire w = env.findWireByStartPort(out);
            if (w != null && !w.isBusy() && w.getEndSystem().isEnabled()) {
                w.setCurrentPacket(packet);
                packet.setDirectionForward();
                Debug.log("[SPY]", "teleport " + Debug.p(packet) + " to " + Debug.sys(exit) +
                        " via " + Debug.wire(w));
                return;
            }
        }
        // اگر نشد، عادی نگه دار
        super.addPacket(packet);
    }
}
