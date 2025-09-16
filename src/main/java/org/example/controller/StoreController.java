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
        // قدیمی‌ها
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

        // NEW: Scrolls → فقط راهنما
        view.setAergiaListener(e ->
                JOptionPane.showMessageDialog(frame,
                        "Aergia: Ctrl + کلیک روی سیم.\nهزینه: 10 کوین، مدت: 20s، کول‌داون دارد.",
                        "Scroll of Aergia", JOptionPane.INFORMATION_MESSAGE)
        );
        view.setEliphasListener(e ->
                JOptionPane.showMessageDialog(frame,
                        "Eliphas: Alt + کلیک روی سیم.\nهزینه: 20 کوین، مدت: 30s.",
                        "Scroll of Eliphas", JOptionPane.INFORMATION_MESSAGE)
        );
        view.setSisyphusListener(e ->
                JOptionPane.showMessageDialog(frame,
                        "Sisyphus: Shift را نگه دارید، روی سیستم غیرمرجع کلیک کرده و درگ کنید.\nهزینه: 15 کوین، شعاع جابه‌جایی محدود.",
                        "Scroll of Sisyphus", JOptionPane.INFORMATION_MESSAGE)
        );

        // خرید ظرفیت کرو
        view.setCurvePointListener(e -> {
            if (env.buyCurvePoint()) {
                view.updateScrollsState(env.getCoins(), env.getAergiaCooldown(), env.getCurvePoints());
            }
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
