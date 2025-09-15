package org.example.model.Packet;

import org.example.model.Wire;

public class TrojanPacket extends Packet {
    private final Packet original; // پیام‌رسان اصلی را نگه می‌داریم تا آنتی‌تروجان برگرداند

    public TrojanPacket(Packet original,double x,double y) {
        super(x,y);
        this.original = original;
        setSpeed(original.getInstantSpeed());
    }

    public Packet getOriginal(){ return original; }

    @Override public String getCompatibilityKey() { return original.getCompatibilityKey(); }

    @Override protected void onEnterWire(Wire w) {
//        // عین اصل حرکت می‌کند، ولی می‌تونه رفتارهای خاص Troj داشته باشه
//        original.setWire(w);
    }
//
//    @Override public void updatePosition(double delta) {
//        // Mirror حرکت اصلی
//        original.wire = this.wire;
//        original.progress = this.progress;
//        original.direction = this.direction;
//        original.offsetX = this.offsetX;
//        original.offsetY = this.offsetY;
//        original.speed = this.speed;
//        original.accel = this.accel;
//        original.updatePosition(delta);
//        // sync back
//        this.wire = original.wire;
//        this.progress = original.progress;
//        this.direction = original.direction;
//        this.offsetX = original.offsetX;
//        this.offsetY = original.offsetY;
//        this.speed = original.speed;
//        this.accel = original.accel;
//        this.wireX = original.wireX;
//        this.wireY = original.wireY;
//    }
@Override
public void updatePosition(double delta) {
    // تروجان را حرکت بده (تحویل/برگشت توسط همین شیء مدیریت می‌شود)
    super.updatePosition(delta);

    // فقط mirror برای آنتی‌تروجان/رندر
    original.wire     = this.wire;
    original.progress = this.progress;
    original.direction= this.direction;
    original.offsetX  = this.offsetX;
    original.offsetY  = this.offsetY;
    original.speed    = this.speed;
    original.accel    = this.accel;
    original.wireX    = this.wireX;
    original.wireY    = this.wireY;
}


    @Override public int getSize() { return original.getSize(); }
    @Override public int getCoinValue() { return original.getCoinValue(); }
}
