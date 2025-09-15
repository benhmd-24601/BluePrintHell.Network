package org.example.model;

import org.example.model.Packet.HeavyPacket;
import org.example.model.Packet.Packet;
import org.example.model.Systems.NetworkSystem;
import org.example.util.Debug;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Wire {
    private final NetworkSystem startSystem;
    private final NetworkSystem endSystem;
    private final String startPortType;
    private final String endPortType;
    private final double length;
    private Packet currentPacket;
    private final double Sx, Sy, Ex, Ey;
    private final Port startPort;
    private final Port endPort;
    private final List<Point> points;
    private final GameEnv env;
    private Point2D.Double control;
    // NEW:
    private final boolean curved;
    private int heavyPasses = 0;

    public Wire(Port startPort, Port endPort,
                NetworkSystem startSystem, String startPortType,
                NetworkSystem endSystem, String endPortType,
                double length, double Sx, double Sy, double Ex, double Ey,
                List<Point> points, GameEnv env) {
        this(startPort, endPort, startSystem, startPortType, endSystem, endPortType,
                length, Sx, Sy, Ex, Ey, points, env, false);
    }

    public Wire(Port startPort, Port endPort,
                NetworkSystem startSystem, String startPortType,
                NetworkSystem endSystem, String endPortType,
                double length, double Sx, double Sy, double Ex, double Ey,
                List<Point> points, GameEnv env, boolean curved) {

        this.startPort = startPort;
        this.endPort = endPort;
        this.startSystem = startSystem;
        this.startPortType = startPortType;
        this.endSystem = endSystem;
        this.endPortType = endPortType;
        this.length = length;
        this.currentPacket = null;
        this.Sx = Sx ;
        this.Sy = Sy ;
        this.Ex = Ex;
        this.Ey = Ey ;
        this.points = points;
        this.env = env;
        this.curved = curved;
    }

    // --- API جدید:
    public boolean hasControlPoint() { return control != null; }
    public Point2D.Double getControlPoint() { return control; }

    public void setControlPoint(double x, double y) {
        if (control == null) control = new Point2D.Double(x, y);
        else { control.x = x; control.y = y; }
        // اگر از فلگ قدیمی curved استفاده می‌کنی، این را هم ست کن:
        // this.curved = true;
    }
    public void clearControlPoint() { control = null; /* curved=false; */ }

    /** نقطهٔ روی مسیر برای t ∈ [0..1] */
    public Point2D.Double getPointAt(double t) {
        double sx = getStartx(), sy = getStarty();
        double ex = getEndX(),  ey = getEndY();
        if (!hasControlPoint()) {
            // خط مستقیم
            return new Point2D.Double(
                    sx + (ex - sx) * t,
                    sy + (ey - sy) * t
            );
        } else {
            // منحنی کوادراتیک: B(t) = (1-t)^2 S + 2(1-t)t C + t^2 E
            double one = 1.0 - t;
            double cx = control.x, cy = control.y;
            double x = one*one*sx + 2*one*t*cx + t*t*ex;
            double y = one*one*sy + 2*one*t*cy + t*t*ey;
            return new Point2D.Double(x, y);
        }
    }

    /** طول مسیر: مستقیم دقیق؛ منحنی → تقریب نمونه‌گیری */
    public double getLength() {
        double sx = getStartx(), sy = getStarty();
        double ex = getEndX(),  ey = getEndY();

        if (!hasControlPoint()) {
            return Math.hypot(ex - sx, ey - sy);
        } else {
            // تقریب طول منحنی با نمونه‌گیری
            final int N = 24; // هر چه بیشتر، دقیق‌تر
            double prevX = sx, prevY = sy;
            double len = 0.0;
            for (int i = 1; i <= N; i++) {
                double t = i / (double) N;
                Point2D.Double pt = getPointAt(t);
                len += Math.hypot(pt.x - prevX, pt.y - prevY);
                prevX = pt.x; prevY = pt.y;
            }
            return len;
        }
    }
    public boolean isCurved(){ return curved; }
    public GameEnv getEnv(){ return env; }

    public void update() {
        if (currentPacket == null && startSystem != null) {
            // مقصد باید فعال باشد؛ و پکت با این پورت سازگار باشد
            if (!endSystem.isEnabled()) return;

            Packet packet = startSystem.getNextPacketForWire(startPortType, startSystem.getAllConnectedWires());
            if (packet != null) {
                currentPacket = packet;
                packet.setDirectionForward();
                packet.setWire(this);
                env.getPackets().add(currentPacket);

                Debug.log("[WIRE]", "place " + Debug.p(packet) + " on " + Debug.wire(this) +
                        " compat=" + (packet.getCompatibilityKey()==null? "*" : packet.getCompatibilityKey()) +
                        "/" + startPortType);

                if (packet.getCompatibilityKey() != null &&
                        !packet.getCompatibilityKey().equals(startPortType)) {
                    // همین‌جا بعد از کاهش سرعت، لاگ کن
                    Debug.log("[WIRE]", "mismatch slow " + Debug.p(packet) +
                            " v=" + Debug.fmt(packet.getInstantSpeed()));
                }


                // اگر ناسازگار بود (برای پیام‌رسان‌ها معنی دارد)، سرعتش را 0.7 کن (منطق قدیمی شما)
                if (packet.getCompatibilityKey() != null &&
                        !packet.getCompatibilityKey().equals(startPortType)) {
                    packet.setSpeed(packet.getInstantSpeed() * 0.7);
                }
            }
        }
    }

    public void setCurrentPacket(Packet p) {
        this.currentPacket = p;
        if (p != null) p.setWire(this);
    }

    public void deliverCurrentPacket() {

        if (currentPacket == null) return;
        Packet p = currentPacket;
        double v = p.getInstantSpeed();
        if (v > ModelConfig.SYS_DISABLE_SPEED_THRESH) {
            endSystem.disableFor(5.0);
            org.example.util.Debug.log("[SYS]", "disabled by overspeed v=" + org.example.util.Debug.fmt(v));
        }
        boolean forward = p.isGoingForward();
        if (forward) {
            if (!endSystem.isEnabled()) {
                Debug.log("[WIRE]", "bounce " + Debug.p(p) + " at disabled " + Debug.sys(endSystem));
                p.bounceBackFromEnd();
                return;
            }

            if (p instanceof HeavyPacket) {
                heavyPasses++;
                Debug.log("[WIRE]", "heavy pass " + Debug.p(p) + " count=" + heavyPasses);
            }

            Debug.log("[DELIVER]", Debug.p(p) + " → " + Debug.sys(endSystem));
            p.onDelivered(env, endSystem);

            boolean mism = (p.getCompatibilityKey()!=null &&
                    !Objects.equals(p.getCompatibilityKey(), this.startPortType));
            if (mism) {
                Debug.log("[WIRE]", "post-deliver boost " + Debug.p(p) +
                        " v=" + Debug.fmt(p.getInstantSpeed()));
            }

            endSystem.addPacket(p);
            startSystem.removePacket(p);
            currentPacket = null;

            if (heavyPasses >= ModelConfig.HEAVY_WIRE_MAX_PASSES) {
                Debug.log("[WIRE]", "destroy " + Debug.wire(this) + " (heavy passes=" + heavyPasses + ")");
                env.removeWire(this);
            }

            if (p instanceof HeavyPacket && endPort != null) {
                endPort.setType(new Random().nextBoolean() ? "square" : "triangle");

                Debug.log("[WIRE]", "heavy flipped end-port of " + Debug.sys(endSystem) +
                        " to " + endPort.getType());
            }

        } else {
            if (!startSystem.isEnabled()) {
                p.setDirectionForward();
                return;
            }
            Debug.log("[DELIVER]", Debug.p(p) + " → " + Debug.sys(startSystem) + " (back)");
            p.onDelivered(env, startSystem);
            startSystem.addPacket(p);
            endSystem.removePacket(p);
            currentPacket = null;
        }

//        if (currentPacket == null) return;
//        Packet p = currentPacket;
//
//        boolean forward = p.isGoingForward();
//
//        if (forward) {
//            // اگر مقصد در لحظه‌ی رسیدن غیرفعال شد → برگشت
//            if (!endSystem.isEnabled()) {
//                p.bounceBackFromEnd();
//                return;
//            }
//
//            // عبور «حجیم» را بشمار، شاید سیم نابود شود
//            if (p instanceof HeavyPacket) {
//                heavyPasses++;
//            }
//
//            // پکت تحویل مقصد
//            p.onDelivered(env, endSystem);
//
//            // «اگر» پیام‌رسان از پورت ناسازگار وارد این سیستم شده بود → سرعت خروج بعدی ×2
//            if (p.getCompatibilityKey() != null &&
//                    !Objects.equals(p.getCompatibilityKey(), this.startPortType)) {
//                p.setSpeed(p.getInstantSpeed() * 2.0);
//            }
//
//            endSystem.addPacket(p);
//            startSystem.removePacket(p);
//            currentPacket = null;
//
//            // نابودی سیم بعد از 3 عبور حجیم
//            if (heavyPasses >= ModelConfig.HEAVY_WIRE_MAX_PASSES) {
//                env.removeWire(this);
//            }
//
//            // اگر پکت حجیم بود: پورت ورودیِ سیستم مقصد را تصادفی تغییر بده
//            if (p instanceof HeavyPacket && endPort != null) {
//                endPort.setType(new Random().nextBoolean() ? "square" : "triangle");
//            }
//
//        } else {
//            // رسیدن به مبدا در حالت برگشت
//            if (!startSystem.isEnabled()) {
//                p.setDirectionForward();
//                return;
//            }
//            p.onDelivered(env, startSystem);
//            startSystem.addPacket(p);
//            endSystem.removePacket(p);
//            currentPacket = null;
//
//            // اگر حجیم بود و از این سر رد شد، شمارش عبور را هم به‌دلخواه می‌توانید اضافه کنید
//        }
    }

    public boolean isBusy() { return currentPacket != null; }
    public Packet getCurrentPacket() { return currentPacket; }

    public NetworkSystem getStartSystem() { return startSystem; }
    public NetworkSystem getEndSystem() { return endSystem; }


    public String getStartPortType() { return startPortType; }
    public String getEndPortType() { return endPortType; }
    public List<Point>  getPoints() { return points; }

    public Port getStartPort() {
        return startPort;
    }

    public Port getEndPort() {
        return endPort;
    }

    public int getHeavyPasses() {
        return heavyPasses;
    }

    public void setHeavyPasses(int heavyPasses) {
        this.heavyPasses = heavyPasses;
    }

    public double getStartx() { return (startPort != null) ? startPort.getCenterX() : Sx; }
    public double getStarty() { return (startPort != null) ? startPort.getCenterY() : Sy; }
    public double getEndX()   { return (endPort   != null) ? endPort.getCenterX()   : Ex; }
    public double getEndY()   { return (endPort   != null) ? endPort.getCenterY()   : Ey; }


}
