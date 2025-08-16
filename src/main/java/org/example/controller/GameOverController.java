// src/main/java/org/example/controller/GameOverController.java
package org.example.controller;

import org.example.model.GameOverModel;
import org.example.view.GameOverView;

import java.awt.event.ActionListener;

public class GameOverController {
    private final GameOverModel model;
    private final GameOverView view;
    private final Runnable onReturnToMenu; // این callback را از بیرون تزریق کنید

    public GameOverController(GameOverModel model, GameOverView view, Runnable onReturnToMenu) {
        this.model = model;
        this.view = view;
        this.onReturnToMenu = onReturnToMenu;
    }

    /** اتصال مدل↔ویو و رویدادها */
    public void init() {
        // همگام‌سازی مقدار اولیه مدل با ویو
        view.setLossPercent(model.getLossPercent());

        // رویداد دکمه بازگشت
        ActionListener goBack = e -> {
            if (onReturnToMenu != null) onReturnToMenu.run();
        };
        view.getReturnButton().addActionListener(goBack);
    }

    /** اگر جایی lossPercent مدل عوض شد، این را صدا بزنید تا ویو به‌روز شود */
    public void refresh() {
        view.setLossPercent(model.getLossPercent());
    }
}
