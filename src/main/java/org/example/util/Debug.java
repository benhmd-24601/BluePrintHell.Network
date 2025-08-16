package org.example.util;

import org.example.model.*;
import org.example.model.Systems.NetworkSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Debug {
    private Debug() {}
    /** روشن/خاموش کردن لاگ‌ها */
    public static volatile boolean ENABLED = true;

    /** throttle برای رویدادهای پرتکرار مثل mouse move */
    private static final Map<String, Long> last = new ConcurrentHashMap<>();

    public static void log(String tag, String msg) {
        if (ENABLED) System.out.println(tag + " " + msg);
    }

    /** هر key را حداکثر هر ms میلی‌ثانیه یک بار اجازه چاپ می‌دهد */
    public static boolean throttle(String key, long ms) {
        long now = System.currentTimeMillis();
        Long prev = last.get(key);
        if (prev == null || now - prev >= ms) {
            last.put(key, now);
            return true;
        }
        return false;
    }

    /** مختصات تمام پورت‌های تمام سیستم‌ها را چاپ می‌کند (یک‌بار در شروع) */
    public static void logAllPorts(GameEnv env) {
        if (!ENABLED) return;
        int idxSys = 0;
        for (NetworkSystem sys : env.getSystems()) {
            log("[SYS]", "#" + (idxSys++) + " @ (" + (int)sys.getX() + "," + (int)sys.getY() + ") " +
                    "inputs=" + sys.getInputPorts().size() + ", outputs=" + sys.getOutputPorts().size());
            int i = 0;
            for (Port p : sys.getInputPorts()) {
                log("[PORT]", "IN  [" + (i++) + "] " + p.getType() + " port@(" +
                        (int)p.getX() + "," + (int)p.getY() + ")");
            }
            int o = 0;
            for (Port p : sys.getOutputPorts()) {
                log("[PORT]", "OUT [" + (o++) + "] " + p.getType() + " port@(" +
                        (int)p.getX() + "," + (int)p.getY() + ")");
            }
        }
    }
}
