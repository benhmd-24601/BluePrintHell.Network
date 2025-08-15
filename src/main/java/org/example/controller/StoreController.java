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
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(parentFrame);

        view = new StoreView(
                env.getCoins(),
                env.hasAtar(),
                env.hasAiryaman(),
                env.hasAnahita()
        );

        setupListeners();
        frame.setContentPane(view);
    }

    private void setupListeners() {
        view.setAtarBuyListener(e -> buyAtar());
        view.setAiryamanBuyListener(e -> buyAiryaman());
        view.setAnahitaBuyListener(e -> buyAnahita());
        view.setBackListener(e -> closeStore());
    }

    private void buyAtar() {
        if (env.canBuyAtar()) {
            env.buyAtar();
            view.updateState(
                    env.getCoins(),
                    env.hasAtar(),
                    env.hasAiryaman(),
                    env.hasAnahita()
            );
        }
    }

    private void buyAiryaman() {
        if (env.canBuyAiryaman()) {
            env.buyAiryaman();
            view.updateState(
                    env.getCoins(),
                    env.hasAtar(),
                    env.hasAiryaman(),
                    env.hasAnahita()
            );
        }
    }

    private void buyAnahita() {
        if (env.canBuyAnahita()) {
            env.buyAnahita();
            view.updateState(
                    env.getCoins(),
                    env.hasAtar(),
                    env.hasAiryaman(),
                    env.hasAnahita()
            );
        }
    }

    private void closeStore() {
        frame.dispose();
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    public void show() {
        frame.setVisible(true);
    }
}