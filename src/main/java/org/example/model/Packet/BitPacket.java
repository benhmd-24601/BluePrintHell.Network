package org.example.model.Packet;

import org.example.model.Wire;

import static org.example.model.ModelConfig.*;

public class BitPacket extends Packet {
    private final String groupId;   // شناسه‌ی گروه
    private final int BalkSize;   // تعداد کل لازم برای ادغام
    public BitPacket(String groupId, int totalCount , double x , double y) {
        super(x, y);
        this.groupId = groupId;
        this.BalkSize = totalCount;
        setSpeed(BASE_SPEED_BIT);
        setAccel(0);
    }
    public String getGroupId(){ return groupId; }
    public int getBulkSize(){ return BalkSize; }

    @Override public String getCompatibilityKey() {
        // مثل دایره: یکی از square/triangle را تصادفی قبول می‌کند (اینجا اهمیتی ندارد)
        return null; // اجازه‌ی ورود به هر پورتی
    }

    @Override protected void onEnterWire(Wire w) { /* سرعت ثابت */ }
    @Override public int getSize() { return 1; }
    @Override public int getCoinValue() { return COIN_BIT; }
}
