// StoreController.java
package org.example.controller;

import org.example.model.GameEnv;
import org.example.view.StoreView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StoreController {
    private final GameEnv env;
    private final StoreView view;
    private final JDialog dialog;           // ← به‌جای JFrame
    private final Runnable onCloseCallback;
    private final JFrame parent;

    private boolean open = false;           // ← گارد
    private final Timer refreshTimer;       // ← برای آپدیت کول‌داون/سکه‌ها

    public StoreController(GameEnv env, JFrame parentFrame, Runnable onCloseCallback) {
        this.env = env;
        this.onCloseCallback = onCloseCallback;
        this.parent = parentFrame;

        view = new StoreView(
                env.getCoins(),
                env.hasAtar(),
                env.hasAiryaman(),
                env.hasAnahita()
        );

        dialog = new JDialog(parentFrame, "Store", Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE); // فقط hide شود
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setContentPane(view);

        // باز/بسته بودن را با لایف‌سایکل همگام کن
        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e)  { open = true; }
            @Override public void windowClosing(WindowEvent e) { open = false; }
            @Override public void windowClosed(WindowEvent e)  { open = false; }
        });

        setupListeners();

        // تایمر سبک برای به‌روز نگه‌داشتن نوشته‌های استور (کول‌داون، سکه، Curve Points)
        refreshTimer = new Timer(250, e ->
                view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints())
        );
        refreshTimer.setRepeats(true);

        // وضعیت اولیه
        view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
    }

    private void setupListeners() {
        // خریدهای قدیمی
        view.setAtarBuyListener(e -> {
            if (env.canBuyAtar()) env.buyAtar();
            view.updateState(env.getCoins(), env.hasAtar(), env.hasAiryaman(), env.hasAnahita());
            view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
        });
        view.setAiryamanBuyListener(e -> {
            if (env.canBuyAiryaman()) env.buyAiryaman();
            view.updateState(env.getCoins(), env.hasAtar(), env.hasAiryaman(), env.hasAnahita());
            view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
        });
        view.setAnahitaBuyListener(e -> {
            if (env.canBuyAnahita()) env.buyAnahita();
            view.updateState(env.getCoins(), env.hasAtar(), env.hasAiryaman(), env.hasAnahita());
            view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
        });

        // راهنمای اسکرول‌ها (کلیدهای میانبر)
        view.setAergiaListener(e -> JOptionPane.showMessageDialog(dialog,
                "Aergia: Ctrl + کلیک روی سیم\nهزینه: 10 کوین | مدت: 20s | کول‌داون دارد",
                "Scroll of Aergia", JOptionPane.INFORMATION_MESSAGE));
        view.setEliphasListener(e -> JOptionPane.showMessageDialog(dialog,
                "Eliphas: Alt + کلیک روی سیم\nهزینه: 20 کوین | مدت: 30s",
                "Scroll of Eliphas", JOptionPane.INFORMATION_MESSAGE));
        view.setSisyphusListener(e -> JOptionPane.showMessageDialog(dialog,
                "Sisyphus: Shift نگه‌دار + کلیک روی سیستم غیرمرجع و درگ\nهزینه: 15 کوین | شعاع محدود | بدون نقض بودجه/برخورد",
                "Scroll of Sisyphus", JOptionPane.INFORMATION_MESSAGE));

        // خرید Curve Point
        view.setCurvePointListener(e -> {
            if (env.buyCurvePoint()) {
                view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
            }
        });

        // بستن
        view.setBackListener(e -> closeStore());
    }

    public void show() {
        // اگر باز است، فقط بیا جلو
        if (dialog.isShowing() || open) {
            dialog.toFront();
            dialog.requestFocus();
            return;
        }
        open = true;
        SwingUtilities.invokeLater(() -> {
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);   // روی بازی و بدون تغییر CardLayout
            dialog.toFront();
            dialog.requestFocus();
            refreshTimer.start();      // شروع آپدیت
        });
    }

    private void closeStore() {
        if (!open) return;
        open = false;
        refreshTimer.stop();
        dialog.setVisible(false);      // فقط hide (نه dispose)
        if (onCloseCallback != null) onCloseCallback.run();
        if (parent != null) parent.toFront();
    }

    public boolean isOpen() { return open; }

    // در صورت نیاز هنگام پایان لِول:
    public void dispose() {
        refreshTimer.stop();
        dialog.dispose();
    }
}
