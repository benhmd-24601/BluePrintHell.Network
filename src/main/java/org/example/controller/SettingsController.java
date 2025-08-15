package org.example.controller;

import org.example.view.SettingsMenu;

public class SettingsController {
    private final AppController app;
    private final SettingsMenu view;

    public SettingsController(AppController app, SettingsMenu view) {
        this.app = app;
        this.view = view;
        bind();
    }

    private void bind() {
        view.addBackButtonListener(e -> app.showMainMenu());
    }
}
