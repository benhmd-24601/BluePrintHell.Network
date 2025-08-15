package org.example.controller;

import org.example.view.LevelSelectionView;

public class LevelSelectionController {
    private final AppController app;
    private final LevelSelectionView view;

    public LevelSelectionController(AppController app, LevelSelectionView view) {
        this.app = app;
        this.view = view;
        bind();
    }

    private void bind() {
        view.addLevel1Listener(e -> app.startLevel(1));
        view.addLevel2Listener(e -> app.startLevel(2));
        view.addBackListener(e -> app.showMainMenu());
    }
}
