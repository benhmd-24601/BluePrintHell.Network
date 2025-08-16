package org.example.model;

public final class ModelConfig {
    private ModelConfig(){}

    // --- سرعت/شتاب پایه
    public static final double BASE_SPEED_MSG = 2.0;     // پیام‌رسان‌ها
    public static final double BASE_SPEED_BIT = 1.0;     // بیت‌پکت
    public static final double SPEED_MIN      = 0.2;

    // مثلث روی پورت ناسازگار
    public static final double TRIANGLE_ACCEL_INCOMPAT = 0.4;

    // دایره
    public static final double CIRCLE_ACCEL_COMPAT     = 0.4;
    public static final double CIRCLE_ACCEL_INCOMPAT_START = 0.4; // نزولی تا 0

    // پکت‌های حجیم
    public static final int HEAVY_WIRE_MAX_PASSES = 3;
    public static final double HEAVY1_ACCEL_ON_CURVE = 0.5; // فقط اگر wire.curved == true
    public static final double HEAVY_DRIFT_STEP_DIST  = 120.0; // هر X واحد فاصله
    public static final double HEAVY_DRIFT_OFFSET     = 2.0;   // مقدار انحراف

    // محرمانه‌ها
    public static final double SECRET_APPROACH_HOLD_DIST = 10.0; // فاصله‌ای که قبل از ورودی نگه می‌دارد
    public static final double SECRET_SLOW_IF_BUSY       = 0.35; // کندشدن وقتی مقصد شلوغ است
    public static final double SECRET2_DESIRED_GAP       = 60.0; // فاصله مدّنظر روی سیم

    // آنتی‌تروجان
    public static final double ANTITROJAN_RADIUS   = 120.0;
    public static final double ANTITROJAN_COOLDOWN = 4.0;  // ثانیه

    // خرابکار
    public static final double SABOTEUR_TROJAN_PROB = 0.25; // 25%

    // Timeout روی سیم
    public static final double PACKET_WIRE_TIMEOUT = 30.0; // ثانیه

    // سکه‌ها
    public static final int COIN_SQUARE   = 2;
    public static final int COIN_TRIANGLE = 3;
    public static final int COIN_CIRCLE   = 1;
    public static final int COIN_BIT      = 1;
    public static final int COIN_PROTECTED= 5;
    public static final int COIN_SECRET1  = 3;
    public static final int COIN_SECRET2  = 4;
    public static final int COIN_HEAVY8   = 8;
    public static final int COIN_HEAVY10  = 10;
}
