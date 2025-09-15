package org.example.model.Systems;

import org.example.model.GameEnv;
import org.example.model.Packet.Packet;
import org.example.model.Packet.TrojanPacket;
import org.example.model.Wire;

import static org.example.model.ModelConfig.*;

public class AntiTrojanSystem extends NetworkSystem {
    private double cooldown = 0.0;

    public AntiTrojanSystem(double x, double y) { super(x, y, 1); }

////
//@Override
//public void update() {
//    // کول‌داون
//    if (!isEnabled()) {
//        if (cooldown > 0) cooldown -= 1.0/60.0;
//        if (cooldown <= 0) setEnabled(true);
//        return;
//    }
//
//    GameEnv env = getEnv();
//
//    // مرکز مؤثر سیستم (همان که در Renderer استفاده کردیم)
//    double cx = getX() + 60.0;
//    double cy = getY() + 22.0;
//
//    // اول فقط هدف را پیدا کن (بدون دستکاری لیست)
//    TrojanPacket target = null;
//    for (Packet p : env.getPackets()) {
//        if (p instanceof TrojanPacket troj) {
//            double dx = p.getX() - cx, dy = p.getY() - cy;
//            if (Math.hypot(dx, dy) <= ANTITROJAN_RADIUS) {
//                target = troj;
//                break;
//            }
//        }
//    }
//    if (target == null) return;
//
//    // جایگزینی امن خارج از foreach
//    Packet orig = target.getOriginal();
//    orig.setId(target.getId());                // حفظ ID برای سازگاری UI
//
//    // وضعیت فعلی سیم
//    Wire w = target.getWire();
//    double prog = target.getProgress();
//    boolean forward = target.isGoingForward();
//    double v = Math.max(SPEED_MIN, target.getInstantSpeed());
//
//    // اگر تروجان روی سیم است، همان سیم را به اصل بدهیم
//    if (w != null && w.getCurrentPacket() == target) {
//        // setCurrentPacket(orig) خودش setWire(orig) را هم صدا می‌زند
//        w.setCurrentPacket(orig);
//        // progress پس از setWire ریست شده؛ دوباره تنظیم کن
//        orig.setDirection(forward ? +1 : -1);
//        orig.setProgress(prog);
//    } else if (w != null) {
//        // احتیاطاً اگر روی سیم بود ولی currentPacket نبود
//        orig.setDirection(forward ? +1 : -1);
//        orig.setWire(w);
//        orig.setProgress(prog);
//    }
//
//    // سرعت/شتاب معقول
//    orig.setSpeed(v);
//    orig.setAccel(0);
//
//    // تعویض در لیست پکت‌ها (خارج از foreach)
//    env.getPackets().remove(target);
//    if (!env.getPackets().contains(orig)) {
//        env.getPackets().add(orig);
//    }
//
//    // ورود به کول‌داون
//    setEnabled(false);
//    cooldown = ANTITROJAN_COOLDOWN;
//}
@Override
public void update() {
    super.update();                 // مهم: کول‌داون پایه همین‌جا کم می‌شود
    if (!isEnabled()) return;       // در حالت کول‌داون، کاری نکن

    GameEnv env = getEnv();
    double cx = getX() + 60.0, cy = getY() + 22.0; // مرکز همان که در رندر استفاده می‌کنی

    TrojanPacket target = null;
    for (Packet p : env.getPackets()) {
        if (p instanceof TrojanPacket troj) {
            double dx = p.getX() - cx, dy = p.getY() - cy;
            if (Math.hypot(dx, dy) <= ANTITROJAN_RADIUS) { target = troj; break; }
        }
    }
    if (target == null) return;

    // --- تبدیل تروجان به اصل (مثل قبل؛ خلاصه شده)
    Packet orig = target.getOriginal();
    orig.setId(target.getId());
    Wire w = target.getWire();
    double prog = target.getProgress();
    boolean fwd = target.isGoingForward();
    double v = Math.max(SPEED_MIN, target.getInstantSpeed());

    if (w != null && w.getCurrentPacket() == target) {
        w.setCurrentPacket(orig);
        orig.setDirection(fwd ? +1 : -1);
        orig.setProgress(prog);
    } else if (w != null) {
        orig.setDirection(fwd ? +1 : -1);
        orig.setWire(w);
        orig.setProgress(prog);
    }
    orig.setSpeed(v);
    orig.setAccel(0);

    env.getPackets().remove(target);
    if (!env.getPackets().contains(orig)) env.getPackets().add(orig);

    // به‌جای ست‌کردن فیلد محلی:
    disableFor(org.example.model.ModelConfig.ANTITROJAN_COOLDOWN);
}
}
