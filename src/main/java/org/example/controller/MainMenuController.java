package org.example.controller;

import org.example.view.MainMenuView;

public class MainMenuController {
    private final AppController app;
    private final MainMenuView view;

    public MainMenuController(AppController app, MainMenuView view) {
        this.app = app;
        this.view = view;
        bind();
    }

    private void bind() {
        view.addLevelsButtonListener(e -> app.showLevelSelection());
        view.addSettingsButtonListener(e -> app.showSettings());
        view.addExitButtonListener(e -> System.exit(0));
    }
}
