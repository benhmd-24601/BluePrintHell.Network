// Wire.java
package org.example.model;

import org.example.model.Packet.HeavyPacket;
import org.example.model.Packet.Packet;
import org.example.model.Systems.NetworkSystem;
import org.example.util.Debug;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Wire {
    private final NetworkSystem startSystem;
    private final NetworkSystem endSystem;
    private final String startPortType;
    private final String endPortType;
    private final double length;                 // طول اولیه (فقط برای ساخت)
    private Packet currentPacket;
    private final double Sx, Sy, Ex, Ey;         // مختصات fallback اگر پورت null شود
    private final Port startPort;
    private final Port endPort;
    private final List<Point> points;            // استفاده نشده
    private final GameEnv env;

    // حداکثر 3 انکر روی مسیر
    private final ArrayList<Point2D.Double> anchors = new ArrayList<>(3);

    private final boolean curved;                // فلگ قدیمی، بی‌استفاده‌ی عملی
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

    //================== عبور از روی سیستم‌ها ==================
    public boolean crossesSystem(NetworkSystem sys) {
        if (sys == null) return false;
        if (sys == startSystem || sys == endSystem) return false;

        Rectangle2D.Double rect = new Rectangle2D.Double(sys.getX(), sys.getY(), 120, 160);

        final int N = 64;
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
    public boolean crossesAnySystem(GameEnv env) {
        for (NetworkSystem s : env.getSystems()) if (crossesSystem(s)) return true;
        return false;
    }

    //================== نقاط کمکی ==================
    /** لیست گره‌ها: Start, anchors..., End */
    private List<Point2D.Double> knots() {
        ArrayList<Point2D.Double> ks = new ArrayList<>(anchors.size() + 2);
        ks.add(new Point2D.Double(getStartx(), getStarty()));
        ks.addAll(anchors);
        ks.add(new Point2D.Double(getEndX(), getEndY()));
        return ks;
    }
    private Point2D.Double knotAt(List<Point2D.Double> ks, int i) {
        if (i < 0) {
            // برون‌یابی نقطهٔ قبل: P_{-1} = 2P0 - P1
            Point2D.Double P0 = ks.get(0), P1 = ks.get(1);
            return new Point2D.Double(2*P0.x - P1.x, 2*P0.y - P1.y);
        } else if (i >= ks.size()) {
            // برون‌یابی نقطهٔ بعد: P_{m+1} = 2Pm - P_{m-1}
            Point2D.Double Pm = ks.get(ks.size()-1), Pm_1 = ks.get(ks.size()-2);
            return new Point2D.Double(2*Pm.x - Pm_1.x, 2*Pm.y - Pm_1.y);
        } else return ks.get(i);
    }

    //================== API چند-انکری ==================
    public int getAnchorCount() { return anchors.size(); }
    public Point2D.Double getAnchor(int idx) { return anchors.get(idx); }
    public void setAnchor(int idx, double x, double y) {
        if (idx < 0 || idx >= anchors.size()) return;
        anchors.get(idx).x = x;
        anchors.get(idx).y = y;
        invalidateArcCache();
    }
    /** افزودن انکر نزدیک‌ترین نقطه از مسیر (سقف 3) */
    public int addAnchorAtNearest(double x, double y) {
        if (anchors.size() >= 3) return -1;

        final int N = 120;
        double bestT = 0.0, bestD = Double.MAX_VALUE;
        for (int i = 0; i <= N; i++) {
            double t = i / (double) N;
            Point2D.Double pt = getPointAt(t);
            double d = pt.distance(x, y);
            if (d < bestD) { bestD = d; bestT = t; }
        }
        int segs = getSegmentCount();
        int segIdx = Math.min(segs - 1, (int) Math.floor(bestT * segs));
        int insertIdx = Math.min(anchors.size(), segIdx);
        anchors.add(insertIdx, new Point2D.Double(x, y));
        invalidateArcCache();
        return insertIdx;
    }
    public void removeAnchor(int idx) {
        if (idx < 0 || idx >= anchors.size()) return;
        anchors.remove(idx);
        invalidateArcCache();
    }
    public void clearAnchors() { anchors.clear(); invalidateArcCache(); }

    //================== سگمنت‌ها ==================
    public int getSegmentCount() { return anchors.size() + 1; }
    public Point2D.Double getSegmentStart(int seg) {
        if (seg == 0) return new Point2D.Double(getStartx(), getStarty());
        Point2D.Double a = anchors.get(seg-1);
        return new Point2D.Double(a.x, a.y);
    }
    public Point2D.Double getSegmentEnd(int seg) {
        if (seg == anchors.size()) return new Point2D.Double(getEndX(), getEndY());
        Point2D.Double a = anchors.get(seg);
        return new Point2D.Double(a.x, a.y);
    }

    //================== Catmull–Rom → Cubic Bezier ==================
    /** یک بازهٔ Catmull–Rom بین Pi و Pi+1 را به کنترل‌های بزیه تبدیل می‌کند */
    public BezierSegment getBezierForSegment(int seg) {
        List<Point2D.Double> ks = knots();
        int i = seg; // بین Pi و Pi+1
        Point2D.Double Pm1 = knotAt(ks, i-1);
        Point2D.Double P0  = knotAt(ks, i);
        Point2D.Double P1  = knotAt(ks, i+1);
        Point2D.Double P2  = knotAt(ks, i+2);

        // فرمول استاندارد Catmull-Rom (tension = 1)
        Point2D.Double C1 = new Point2D.Double(
                P0.x + (P1.x - Pm1.x) / 6.0,
                P0.y + (P1.y - Pm1.y) / 6.0
        );
        Point2D.Double C2 = new Point2D.Double(
                P1.x - (P2.x - P0.x) / 6.0,
                P1.y - (P2.y - P0.y) / 6.0
        );
        return new BezierSegment(P0, C1, C2, P1);
    }

    /** نقطهٔ روی بازهٔ seg با پارامتر tLocal ∈ [0..1] (ارزیابی کوبیک بزیه) */
    public Point2D.Double getPointOnSegment(int seg, double tLocal) {
        BezierSegment bz = getBezierForSegment(seg);
        return cubicPoint(bz.p0, bz.c1, bz.c2, bz.p1, tLocal);
    }

    /** نقطه روی کل مسیر برای tGlobal ∈ [0..1] */
    public Point2D.Double getPointAt(double tGlobal) {
        int segs = getSegmentCount();
        if (segs <= 0) return new Point2D.Double(getStartx(), getStarty());
        double u = Math.max(0.0, Math.min(0.999999, tGlobal));
        double f = u * segs;
        int seg = Math.min(segs - 1, (int) Math.floor(f));
        double tLocal = f - seg;
        return getPointOnSegment(seg, tLocal);
    }

    /** طول مسیر با LUT قوسی */
    public double getLength() {
        rebuildArcCacheIfNeeded();
        if (arcS == null || arcS.length == 0) return 0.0;
        return arcS[arcS.length - 1];
    }

    //================== حرکت/تحویل پکت (بدون تغییر منطقی) ==================
    public boolean isCurved(){ return curved; }
    public GameEnv getEnv(){ return env; }

    public void update() {
        if (currentPacket == null && startSystem != null) {
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

    //================== Arc-Length LUT برای کل مسیر ==================
    private double[] arcS = null;   // طول تجمعی
    private double[] arcT = null;   // tGlobal متناظر
    private boolean arcDirty = true;
    private double cacheSx = Double.NaN, cacheSy = Double.NaN, cacheEx = Double.NaN, cacheEy = Double.NaN;
    private int    cacheAnchorCount = -1;

    public void invalidateArcCache() { arcDirty = true; }

    private void rebuildArcCacheIfNeeded() {
        double sx = getStartx(), sy = getStarty();
        double ex = getEndX(),  ey = getEndY();
        boolean endpointsChanged = (sx != cacheSx) || (sy != cacheSy) || (ex != cacheEx) || (ey != cacheEy);
        boolean anchorsChanged   = (cacheAnchorCount != anchors.size());

        if (!arcDirty && !endpointsChanged && !anchorsChanged && arcS != null && arcT != null) return;

        int segs = getSegmentCount();
        int M = 40; // نمونه در هر سگمنت (کیفیت طول)
        int totalSamples = Math.max(1, segs * M);

        arcS = new double[totalSamples + 1];
        arcT = new double[totalSamples + 1];

        Point2D.Double prev = getPointOnSegment(0, 0.0);
        arcS[0] = 0.0; arcT[0] = 0.0;

        int idx = 1;
        for (int seg = 0; seg < segs; seg++) {
            for (int j = 1; j <= M; j++) {
                double tLocal = j / (double) M;
                double tGlobal = (seg + tLocal) / segs;
                Point2D.Double pt = getPointOnSegment(seg, tLocal);
                arcS[idx] = arcS[idx - 1] + pt.distance(prev);
                arcT[idx] = tGlobal;
                prev = pt;
                idx++;
            }
        }

        cacheSx = sx; cacheSy = sy; cacheEx = ex; cacheEy = ey;
        cacheAnchorCount = anchors.size();
        arcDirty = false;
    }

    private double lengthToT(double s) {
        rebuildArcCacheIfNeeded();
        if (arcS == null || arcT == null || arcS.length == 0) return 0.0;

        s = Math.max(0.0, Math.min(s, arcS[arcS.length - 1]));
        int lo = 0, hi = arcS.length - 1;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (arcS[mid] < s) lo = mid + 1; else hi = mid;
        }
        int i = Math.max(1, lo);
        double s1 = arcS[i - 1], s2 = arcS[i];
        double t1 = arcT[i - 1], t2 = arcT[i];
        double denom = (s2 - s1);
        double ratio = denom <= 1e-9 ? 0.0 : (s - s1) / denom;
        return t1 + ratio * (t2 - t1);
    }

    /** نقطهٔ دقیق روی مسیر برحسب طول طی‌شده از ابتدا */
    public Point2D.Double getPointAtDistance(double s) {
        double t = lengthToT(s);
        return getPointAt(t);
    }

    //================== ابزارهای بزیه ==================
    public static class BezierSegment {
        public final Point2D.Double p0, c1, c2, p1;
        public BezierSegment(Point2D.Double p0, Point2D.Double c1,
                             Point2D.Double c2, Point2D.Double p1) {
            this.p0 = p0; this.c1 = c1; this.c2 = c2; this.p1 = p1;
        }
    }
    private static Point2D.Double cubicPoint(Point2D.Double p0, Point2D.Double p1,
                                             Point2D.Double p2, Point2D.Double p3, double t) {
        double u = 1.0 - t;
        double x = u*u*u*p0.x + 3*u*u*t*p1.x + 3*u*t*t*p2.x + t*t*t*p3.x;
        double y = u*u*u*p0.y + 3*u*u*t*p1.y + 3*u*t*t*p2.y + t*t*t*p3.y;
        return new Point2D.Double(x, y);
    }
    /** نزدیک‌ترین فاصلهٔ قوسی روی مسیر برای یک نقطهٔ دلخواه (تقریب با LUT داخلی) */
    public double approxDistanceForPoint(double px, double py) {
        rebuildArcCacheIfNeeded();
        if (arcS == null || arcS.length == 0) return 0.0;

        // نمونه‌برداری از LUT و انتخاب نزدیک‌ترین
        double bestD = Double.MAX_VALUE;
        double bestS = 0.0;

        for (int i = 0; i < arcT.length; i++) {
            double t = arcT[i];
            Point2D.Double pt = getPointAt(t);
            double d = Point2D.distance(px, py, pt.x, pt.y);
            if (d < bestD) { bestD = d; bestS = arcS[i]; }
        }
        return bestS;
    }

    /** جایگزینی دقیق انکرها مطابق سیو (بدون heuristic) */
    public void setAnchorsExact(java.util.List<Point2D.Double> list) {
        anchors.clear();
        anchors.addAll(list);
        invalidateArcCache();
    }
}
