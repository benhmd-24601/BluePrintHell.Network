package org.example.model.Systems;

import org.example.model.Packet.*;

import java.util.HashSet;
import java.util.Set;

public class VPNSystem extends NetworkSystem {

    private final Set<ProtectedPacket> provided = new HashSet<>();

    public VPNSystem(double x, double y) { super(x, y, 1); }

    @Override
    public void addPacket(Packet packet) {
        // محرمانه۱ → محرمانه۲ ؛ پیام‌رسان/تروجان → محافظت‌شده
        if (packet instanceof SecretPacket2 || (packet instanceof ProtectedPacket pp && pp.isProtectionActive())) {
            super.addPacket(packet);
            return;
        }

        Packet toStore;
        if (packet instanceof SecretPacket1) {
            toStore = new SecretPacket2(packet.getX() , packet.getY());
        } else {
            // اگر Troj بود، اصلش را محافظت کن
            Packet base = (packet instanceof TrojanPacket t) ? t.getOriginal() : packet;
            ProtectedPacket prot = new ProtectedPacket(base, this);
            provided.add(prot);
            toStore = prot;
        }
        super.addPacket(toStore);
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean was = isEnabled();
        super.setEnabled(enabled);
        if (was && !enabled) {
            // خاموش شد → همه‌ی پکت‌های محافظت‌شده‌ای که این تولید کرده drop شوند
            for (ProtectedPacket p : provided) p.dropProtection();
            provided.clear();
        }
    }
}
