package org.example.util;

import javax.swing.*;
import java.awt.*;

public final class UiUtil {
    private UiUtil() {}

    public static void fullscreen(JFrame f) {
        // لازم است قبل از setUndecorated، پنجره displayable نباشد
        f.dispose();
        f.setUndecorated(true);    // بدون نوار عنوان و قاب
        f.setResizable(false);     // کاربر نتواند تغییر اندازه دهد

        GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(f);   // فول‌اسکرین واقعی
        } else {
            // اگر کارت گرافیک/سیستم پشتیبانی نکرد، حداقل ماکزیمایز کن
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setVisible(true);
        }
    }
}
