package org.example.controller;

import org.example.util.SaveManager;
import org.example.util.SoundManager;
import org.example.util.UiUtil;
import org.example.view.LevelSelectionView;
import org.example.view.MainMenuView;
import org.example.view.SettingsMenu;

import javax.swing.*;
import java.awt.*;

public class AppController {

    private final JFrame frame;
    private final CardLayout cards;
    private final JPanel root;

    // Views
    private final MainMenuView mainMenuView;
    private final LevelSelectionView levelSelectionView;
    private final SettingsMenu settingsMenu;

    // Sub-controllers
    private MainMenuController mainMenuController;
    private LevelSelectionController levelSelectionController;
    private SettingsController settingsController;
    private GameController gameController;

    public AppController() {
        SoundManager.init();
        SaveManager.init(2);

        frame = new JFrame("Blueprint Hell Swing - MVC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1000, 700);
//        frame.setLocationRelativeTo(null);

        cards = new CardLayout();
        root = new JPanel(cards);

        mainMenuView = new MainMenuView();
        levelSelectionView = new LevelSelectionView();
        settingsMenu = new SettingsMenu();

        root.add(mainMenuView, "menu");
        root.add(levelSelectionView, "levels");
        root.add(settingsMenu, "settings");

        frame.setContentPane(root);
        UiUtil.fullscreen(frame);

    }

    public void start() {
        // play music
        SoundManager.playBackgroundMusic();

        // wire controllers
        mainMenuController = new MainMenuController(this, mainMenuView);
        levelSelectionController = new LevelSelectionController(this, levelSelectionView);
        settingsController = new SettingsController(this, settingsMenu);

        // initial screen
        showMainMenu();

        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    // Navigation (centralized)
    public void showMainMenu() {
        cards.show(root, "menu");
        frame.revalidate();
        frame.repaint();
    }

    public void showLevelSelection() {
        cards.show(root, "levels");
        frame.revalidate();
        frame.repaint();
    }

    public void showSettings() {
        cards.show(root, "settings");
        frame.revalidate();
        frame.repaint();
    }

    public void startLevel(int levelNumber) {
        // ساخت کنترلر بازی + جایگزینی UI بازی در همین فریم
        if (gameController != null) {
            gameController.dispose();
            gameController = null;
        }
        gameController = new GameController(this, levelNumber);
        JComponent gameView = gameController.getView();
        root.add(gameView, "game");
        cards.show(root, "game");
        frame.revalidate();
        frame.repaint();
    }

    public void returnFromGameToMenu() {
        // توقف لوپ و بازگشت
        if (gameController != null) {
            gameController.dispose();
            gameController = null;
        }
        showMainMenu();
    }
}
