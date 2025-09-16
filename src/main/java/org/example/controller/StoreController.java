package org.example.controller;

import org.example.model.GameEnv;
import org.example.view.StoreView;

import javax.swing.*;

public class StoreController {
    private final GameEnv env;
    private final StoreView view;
    private final JFrame frame;
    private final Runnable onCloseCallback;

    public StoreController(GameEnv env, JFrame parentFrame, Runnable onCloseCallback) {
        this.env = env;
        this.onCloseCallback = onCloseCallback;

        frame = new JFrame("Store");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(480, 520);
        frame.setLocationRelativeTo(parentFrame);

        view = new StoreView(
                env.getCoins(),
                env.hasAtar(),
                env.hasAiryaman(),
                env.hasAnahita()
        );

        setupListeners();
        frame.setContentPane(view);

        // وضعیت اولیه دکمه‌های جدید
        view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
    }

    private void setupListeners() {
        // قبلی‌ها
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
        view.setBackListener(e -> closeStore());

        // NEW: Scrolls & Curve Point
        view.setAergiaListener(e -> {
            if (env.tryStartAergiaPlacement()) {
                // حالا کاربر باید روی سیم کلیک کند تا فیلد کاشته شود
            }
            view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
        });

        view.setEliphasListener(e -> {
            if (env.tryStartEliphasPlacement()) {
                // منتظر کلیک روی سیم
            }
            view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
        });

        view.setSisyphusListener(e -> {
            if (env.tryStartSisyphus()) {
                // کاربر باید یک سیستم غیرمرجع را انتخاب و درگ کند
            }
            view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
        });

        view.setCurvePointListener(e -> {
            env.buyCurvePoint();
            view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
        });
    }

    private void closeStore() {
        frame.dispose();
        if (onCloseCallback != null) onCloseCallback.run();
    }

    public void show() {
        frame.setVisible(true);
    }
}
