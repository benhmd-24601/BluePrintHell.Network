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
        view.addLevel1Listener(e -> {
            if (org.example.util.SaveLoadManager.isUnlocked(0)) app.startLevel(1);
            else javax.swing.JOptionPane.showMessageDialog(null, "مرحله هنوز آنلاک نشده است.");
        });

        view.addLevel2Listener(e -> {
            if (org.example.util.SaveLoadManager.isUnlocked(1)) app.startLevel(2);
            else javax.swing.JOptionPane.showMessageDialog(null, "ابتدا مرحلهٔ قبلی را ببرید.");
        });

        view.addLevel3Listener(e -> {
            if (org.example.util.SaveLoadManager.isUnlocked(2)) app.startLevel(3);
            else javax.swing.JOptionPane.showMessageDialog(null, "ابتدا مرحلهٔ قبلی را ببرید.");
        });

        view.addLevel4Listener(e -> {
            if (org.example.util.SaveLoadManager.isUnlocked(3)) app.startLevel(4);
            else javax.swing.JOptionPane.showMessageDialog(null, "ابتدا مرحلهٔ قبلی را ببرید.");
        });

        view.addLevel5Listener(e -> {
            if (org.example.util.SaveLoadManager.isUnlocked(4)) app.startLevel(5);
            else javax.swing.JOptionPane.showMessageDialog(null, "ابتدا مرحلهٔ قبلی را ببرید.");
        });

        view.addBackListener(e -> app.showMainMenu());
    }
}
