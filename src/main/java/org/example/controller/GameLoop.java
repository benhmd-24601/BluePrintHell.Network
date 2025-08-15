package org.example.controller;

import javax.swing.*;
import org.example.model.GameEnv;
import org.example.view.GameView;

public class GameLoop {
    private static final int TARGET_FPS = 60;
    private static final int TIMER_DELAY = 1000 / TARGET_FPS;
    private static final double OPTIMAL_TIME = 1_000_000_000.0 / TARGET_FPS;

    private final GameEnv gameEnv;
    private final GameView gameView;
    private final Timer timer;
    private long lastLoopTime;

    private final Runnable onUpdateCallback;


    public GameLoop(GameEnv gameEnv, GameView gameView , Runnable onUpdateCallback) {
        this.gameEnv = gameEnv;
        this.gameView = gameView;

        this.onUpdateCallback = onUpdateCallback;


        this.timer = new Timer(TIMER_DELAY, e -> tick());
        this.timer.setCoalesce(true);
    }

    public void start() {
        lastLoopTime = System.nanoTime();
        timer.start();
    }

    public void stop() { timer.stop(); }

    private void tick() {
        long now = System.nanoTime();
        long updateLength = now - lastLoopTime;
        lastLoopTime = now;

        double delta = updateLength / OPTIMAL_TIME;
        gameEnv.update(delta);
        gameView.refresh();

        if (onUpdateCallback != null) {
            onUpdateCallback.run();
        }
    }

    public boolean isRunning() { return timer.isRunning(); }
}
