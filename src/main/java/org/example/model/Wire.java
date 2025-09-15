package org.example.model;

import org.example.model.Packet.HeavyPacket;
import org.example.model.Packet.Packet;
import org.example.model.Systems.NetworkSystem;
import org.example.util.Debug;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Wire {
    private final NetworkSystem startSystem;
    private final NetworkSystem endSystem;
    private final String startPortType;
    private final String endPortType;
    private final double length; // طول اولیه‌ی خط مستقیم (برای ساخت)
    private Packet currentPacket;
    private final double Sx, Sy, Ex, Ey; // مختصات اولیه‌ی نقاط اتصال (اگر پورت null باشد)
    private final Port startPort;
    private final Port endPort;
    private final List<Point> points; // فعلاً استفاده نمی‌شود
    private final GameEnv env;

    // نقطه‌ی قابل‌درگ روی «خود مسیر» (anchor-on-curve) — نه کنترل‌پوینت حقیقی Bezier
    private Point2D.Double bendAnchor = null;

    // فلگ قدیمی
    private final boolean curved;

    // شمارنده‌ی عبور پکت حجیم
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
        this.Sx = Sx;
        this.Sy = Sy;
        this.Ex = Ex;
        this.Ey = Ey;
        this.points = points;
        this.env = env;
        this.curved = curved;
    }
    /** آیا مسیر این سیم از روی بدنه‌ی این سیستم عبور می‌کند؟ (مبدا/مقصد نادیده گرفته می‌شوند) */
    public boolean crossesSystem(NetworkSystem sys) {
        if (sys == null) return false;
        if (sys == startSystem || sys == endSystem) return false; // پورت‌های اتصال اجازه دارند کنار بدنه باشند

        // کادر بدنه: همانی که در MouseHandler برای hit-test استفاده می‌کنی (120×160)
        Rectangle2D.Double rect = new Rectangle2D.Double(sys.getX(), sys.getY(), 120, 160);

        double sx = getStartx(), sy = getStarty();
        double ex = getEndX(),  ey = getEndY();

        // اگر سیم مستقیم است
        if (!hasControlPoint()) {
            // اگر هر نقطه‌ی مسیر داخل مستطیل بود (به‌ندرت برای خط مستقیم) یا خط با کادر برخورد داشت
            if (rect.contains(sx, sy) || rect.contains(ex, ey)) return true;
            return rect.intersectsLine(sx, sy, ex, ey);
        }

        // اگر سیم خم‌شده است: نمونه‌گیری
        final int N = 40;
        Point2D.Double prev = getPointAt(0.0);
        if (rect.contains(prev)) return true;

        for (int i = 1; i <= N; i++) {
            double t = i / (double) N;
            Point2D.Double pt = getPointAt(t);
            if (rect.contains(pt)) return true;
            if (rect.intersectsLine(prev.x, prev.y, pt.x, pt.y)) return true;
            prev = pt;
        }
        return false;
    }

    /** آیا این سیم از روی بدنه‌ی حداقل یک سیستم عبور می‌کند؟ */
    public boolean crossesAnySystem(GameEnv env) {
        for (NetworkSystem s : env.getSystems()) {
            if (crossesSystem(s)) return true;
        }
        return false;
    }

    // ---------- API خم‌کردن: Anchor روی خود مسیر ----------
    /** آیا نقطه‌ی خم (anchor-on-curve) داریم؟ */
    public boolean hasControlPoint() { return bendAnchor != null; }

    /** گرفتن anchor (روی مسیر) — برای رندر دکمه‌ی سفید */
    public Point2D.Double getControlPoint() { return bendAnchor; }

    /** ست‌کردن anchor (روی مسیر) — دایره‌ی قابل‌درگ */
    public void setControlPoint(double x, double y) {
        if (bendAnchor == null) bendAnchor = new Point2D.Double(x, y);
        else { bendAnchor.x = x; bendAnchor.y = y; }
    }

    /** حذف خم */
    public void clearControlPoint() { bendAnchor = null; }

    /** کنترل‌پوینت حقیقی Bezier درجه‌۲ را از روی anchor در t=0.5 محاسبه می‌کند. */
    public Point2D.Double getQuadraticControlFromAnchor() {
        if (!hasControlPoint()) return null;
        double sx = getStartx(), sy = getStarty();
        double ex = getEndX(),  ey = getEndY();

        // برای t=0.5: C = 2*A - 0.5*(S + E)
        double cx = 2.0 * bendAnchor.x - 0.5 * (sx + ex);
        double cy = 2.0 * bendAnchor.y - 0.5 * (sy + ey);
        return new Point2D.Double(cx, cy);
    }

    /** نقطه روی مسیر برای t∈[0,1] (خط مستقیم یا منحنی درجه‌۲) */
    public Point2D.Double getPointAt(double t) {
        double sx = getStartx(), sy = getStarty();
        double ex = getEndX(),  ey = getEndY();

        if (!hasControlPoint()) {
            // خط مستقیم
            double x = sx + (ex - sx) * t;
            double y = sy + (ey - sy) * t;
            return new Point2D.Double(x, y);
        } else {
            // منحنی درجه‌۲: P(t) = (1-t)^2*S + 2(1-t)t*C + t^2*E
            Point2D.Double C = getQuadraticControlFromAnchor();
            double u = 1.0 - t;
            double x = u*u*sx + 2*u*t*C.x + t*t*ex;
            double y = u*u*sy + 2*u*t*C.y + t*t*ey;
            return new Point2D.Double(x, y);
        }
    }

    /** طول لحظه‌ای مسیر: مستقیم دقیق؛ منحنی با نمونه‌گیری تقریب زده می‌شود. */
    public double getLength() {
        if (!hasControlPoint()) {
            double dx = getEndX() - getStartx();
            double dy = getEndY() - getStarty();
            return Math.hypot(dx, dy);
        } else {
            double len = 0.0;
            Point2D.Double prev = getPointAt(0.0);
            final int N = 40; // دقت بیشتر → طول دقیق‌تر
            for (int i = 1; i <= N; i++) {
                double t = i / (double) N;
                Point2D.Double pt = getPointAt(t);
                len += pt.distance(prev);
                prev = pt;
            }
            return len;
        }
    }

    public boolean isCurved(){ return curved; }
    public GameEnv getEnv(){ return env; }

    // ---------- حلقه‌ی سیم ----------
    public void update() {
        if (currentPacket == null && startSystem != null) {
            // مقصد باید فعال باشد
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

                // کُند کردن پیام‌رسان‌های ناسازگار (منطق قدیمی)
                if (packet.getCompatibilityKey() != null &&
                        !Objects.equals(packet.getCompatibilityKey(), startPortType)) {
                    Debug.log("[WIRE]", "mismatch slow " + Debug.p(packet) +
                            " v=" + Debug.fmt(packet.getInstantSpeed()));
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
    }

    public boolean isBusy() { return currentPacket != null; }
    public Packet getCurrentPacket() { return currentPacket; }

    public NetworkSystem getStartSystem() { return startSystem; }
    public NetworkSystem getEndSystem() { return endSystem; }

    public String getStartPortType() { return startPortType; }
    public String getEndPortType() { return endPortType; }
    public List<Point>  getPoints() { return points; }

    public Port getStartPort() { return startPort; }
    public Port getEndPort() { return endPort; }

    public int getHeavyPasses() { return heavyPasses; }
    public void setHeavyPasses(int heavyPasses) { this.heavyPasses = heavyPasses; }

    public double getStartx() { return (startPort != null) ? startPort.getCenterX() : Sx; }
    public double getStarty() { return (startPort != null) ? startPort.getCenterY() : Sy; }
    public double getEndX()   { return (endPort   != null) ? endPort.getCenterX()   : Ex; }
    public double getEndY()   { return (endPort   != null) ? endPort.getCenterY()   : Ey; }
}
