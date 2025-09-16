package org.example.controller;

import org.example.model.GameOverModel;
import org.example.util.SaveLoadManager;
import org.example.util.SoundManager;
import org.example.view.GameOverView;
import org.example.view.LevelSelectionView;
import org.example.view.MainMenuView;
import org.example.view.SettingsMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        SaveLoadManager.init(5);


        frame = new JFrame("Blueprint Hell Swing - MVC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cards = new CardLayout();
        root = new JPanel(cards);

        mainMenuView = new MainMenuView();
        levelSelectionView = new LevelSelectionView();
        settingsMenu = new SettingsMenu();

        root.add(mainMenuView, "menu");
        root.add(levelSelectionView, "levels");
        root.add(settingsMenu, "settings");

        frame.setContentPane(root);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(true);
        frame.setResizable(false);

        // سیو مطمئن هنگام بستن پنجره
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                try {
                    if (gameController != null && gameController.getEnv() != null) {
                        SaveLoadManager.saveLevelNow(gameController.getEnv());
                    }
                } catch (Throwable ignored) {}
            }
        });
    }

    public void start() {
        SoundManager.playBackgroundMusic();

        mainMenuController = new MainMenuController(this, mainMenuView);
        levelSelectionController = new LevelSelectionController(this, levelSelectionView);
        settingsController = new SettingsController(this, settingsMenu);

        showMainMenu();
        frame.setVisible(true);
    }

    public JFrame getFrame() { return frame; }

    public void showMainMenu() {
        cards.show(root, "menu");
        frame.revalidate();
        frame.repaint();
    }

    public void showLevelSelection() {
        cards.show(root, "levels");
        levelSelectionView.refreshLocks();
        frame.revalidate();
        frame.repaint();
    }

    public void showSettings() {
        cards.show(root, "settings");
        frame.revalidate();
        frame.repaint();
    }

    public void startLevel(int levelNumber) {
        if (gameController != null) {
            gameController.dispose();
            gameController = null;
        }
        gameController = new GameController(this, levelNumber);
        JComponent gameView = gameController.getView();

        // تلاش برای لودِ سیو موجود
        SaveLoadManager.loadLevelIfExists(gameController.getEnv(), levelNumber);

        root.add(gameView, "game");
        cards.show(root, "game");
        frame.revalidate();
        frame.repaint();
    }

    public void returnFromGameToMenu() {
        // قبل از خروج از بازی، سیو کن تا ادامهٔ حرکت پکت‌ها حفظ شود
        if (gameController != null && gameController.getEnv() != null) {
            SaveLoadManager.saveLevelNow(gameController.getEnv());
        }
        if (gameController != null) {
            gameController.dispose();
            gameController = null;
        }
        showMainMenu();
    }

    public void showGameOver(double lossPct) {
        GameOverModel model = new GameOverModel(lossPct);
        GameOverView view = new GameOverView();
        GameOverController controller = new GameOverController(
                model, view,
                this::returnFromGameToMenu
        );
        controller.init();

        root.add(view, "gameover");
        cards.show(root, "gameover");
        frame.revalidate();
        frame.repaint();
    }
}
