package org.example.model.Systems;

import org.example.model.Packet.*;
import org.example.util.Debug;

import java.util.HashSet;
import java.util.Set;

public class VPNSystem extends NetworkSystem {

    private final Set<ProtectedPacket> provided = new HashSet<>();

    public VPNSystem(double x, double y) {
        super(x, y, 1);
    }

    @Override
    public void addPacket(Packet packet) {
        if (getPacketStorage().size() >= getStorageCapacity()) {
            Debug.log("[VPN]", "full drop incoming " + Debug.p(packet));
            getEnv().markAsLost(packet, "vpn_full");
            return;
        }
        // محرمانه۱ → محرمانه۲ ؛ پیام‌رسان/تروجان → محافظت‌شده
        if (packet instanceof SecretPacket2 || (packet instanceof ProtectedPacket pp && pp.isProtectionActive())) {
            Debug.log("[VPN]", "pass-through " + Debug.p(packet));
            super.addPacket(packet);
            return;
        }

        Packet toStore;
        if (packet instanceof SecretPacket1) {
            toStore = new SecretPacket2(packet.getX(), packet.getY());
            toStore.setId(packet.getId());
            Debug.log("[VPN]", "S1→S2 " + Debug.p(packet) + " => " + Debug.p(toStore));
        } else {
            // اگر Troj بود، اصلش را محافظت کن
            Packet base = (packet instanceof TrojanPacket t) ? t.getOriginal() : packet;
            ProtectedPacket prot = new ProtectedPacket(base, this);
            prot.setId(packet.getId());
            provided.add(prot);
            toStore = prot;
            Debug.log("[VPN]", "protected " + Debug.p(prot) + " from " + Debug.p(base));

        }
        super.addPacket(toStore);
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean was = isEnabled();
        super.setEnabled(enabled);
        if (was && !enabled) {
            // خاموش شد → همه‌ی پکت‌های محافظت‌شده‌ای که این تولید کرده drop شوند
            for (ProtectedPacket p : provided) {
                Debug.log("[VPN]", "dropProtection " + Debug.p(p));
                p.dropProtection() ;
            }
            provided.clear();
        }
    }
}
