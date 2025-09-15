package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.*;

import java.util.*;

public class DistributorSystem extends NetworkSystem {
    private final Random rnd = new Random();

    public DistributorSystem(double x, double y) { super(x, y, 1);
        setStorageCapacity(Integer.MAX_VALUE); // یا مثلاً 1000
    }

    @Override
    public void addPacket(Packet packet) {
        GameEnv env = getEnv();
        if (packet instanceof HeavyPacket hp) {
            // تقسیم
            int N = hp.getSize(); // 8 یا 10
            String groupId = "grp-" + System.nanoTime() + "-" + rnd.nextInt(100000);
            // N بیت پکت بساز
            for (int i = 0; i < N; i++) {
                BitPacket bp = new BitPacket(groupId, N , 0 , 0);
                super.addPacket(bp);
            }
            // پکت حجیم مصرف شد
            return;
        }
        super.addPacket(packet);
    }
}

