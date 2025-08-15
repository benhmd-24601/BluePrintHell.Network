package org.example.controller;

import org.example.model.*;
import org.example.util.SoundManager;
import org.example.view.GameOverScreen;
import org.example.view.GameView;
import org.example.view.StoreView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameController {

    private final AppController app;
    private final int levelNumber;
    private final GameEnv env;
    private final GameView gameView;
    private final GameLoop gameLoop;

    // فیلدهای جدید برای مدیریت دکمه‌های قدرت
    private JToggleButton atarToggle;
    private JToggleButton airyToggle;
    private JToggleButton anaToggle;
    private JPanel powerBar;


    public GameController(AppController app, int levelNumber) {
        this.app = app;
        this.levelNumber = levelNumber;

        // Model
        env = new GameEnv(levelNumber);
        Stage stage = switch (levelNumber) {
            case 1 -> StageFactory.createStage1();
            case 2 -> StageFactory.createStage2();
            default -> throw new IllegalArgumentException("Unknown level: " + levelNumber);
        };
        env.applyStage(stage);

        // View
        gameView = new GameView(env);

        // Controller: mouse handling & loop
        MouseHandler mouseHandler = new MouseHandler(env, gameView::repaint);
        gameView.addMouseListener(mouseHandler);
        gameView.addMouseMotionListener(mouseHandler);

        // Back
        gameView.setOnBackToMenu(() -> {
            SoundManager.stopBackgroundMusic();
            app.returnFromGameToMenu();
        });
        createBackButton();

        createStoreButton();
        // ساخت دکمه‌های قدرت
        createPowerButtons();

        createSourceButtons();

        // Key binding: toggle mute (Controller owns input)
        JComponent content = app.getFrame().getRootPane();
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(SoundManager.InputSettings.getMuteKey(), 0), "toggleMute");
        content.getActionMap().put("toggleMute", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SoundManager.toggleMute();
            }
        });

        // Game loop
        gameLoop = new GameLoop(env, gameView, this::refreshPowerButtons);
        gameView.setGameLoop(gameLoop);

        // Game over callback
        env.setOnGameOver(this::showGameOver);

        // start
        gameLoop.start();
    }


    private void createBackButton() {
        JButton backButton = new JButton("⏎ Menu");
        backButton.setBounds(1350, 65, 100, 30);
        styleHudButton(backButton);
        backButton.addActionListener(e -> {
            SoundManager.stopBackgroundMusic();
            app.returnFromGameToMenu();
        });
        gameView.addCustomComponent(backButton);
    }

    private void createSourceButtons() {
        for (NetworkSystem system : env.getSystems()) {
            if (system instanceof SourceSystem) {
                SourceSystem src = (SourceSystem) system;
                JButton btn = new JButton("◀");
                btn.setBounds((int) src.getX(), (int) src.getY() - 25, 60, 20);
                styleHudButton(btn);

                // مدیریت رویداد کلیک
                btn.addActionListener(e -> handleSourceButtonClick(src, btn));

                gameView.addCustomComponent(btn);
            }
        }
    }

    private void handleSourceButtonClick(SourceSystem source, JButton button) {
        // 1. بررسی وضعیت اتصال سیستم‌ها
        boolean allConnected = true;
        for (NetworkSystem system : env.getSystems()) {
            if (!system.isSourceSystem() && !system.isIndicatorOn()) {
                allConnected = false;
                break;
            }
        }

        // 2. تغییر وضعیت فقط اگر همه سیستم‌ها متصل باشند
        if (allConnected) {
            source.setGenerating(!source.isGenerating());
            button.setText(source.isGenerating() ? "❚❚" : "◀");

        }
    }

    private void createPowerButtons() {
        // ساخت پنل نوار قدرت
        powerBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 5));
        powerBar.setOpaque(false);
        powerBar.setBounds(0, gameView.getHeight() - 50, gameView.getWidth(), 50);

        // ساخت دکمه‌های قدرت
        atarToggle = new JToggleButton("Atar");
        airyToggle = new JToggleButton("Airyaman");
        anaToggle = new JToggleButton("Anahita");

        styleHudButton(atarToggle);
        styleHudButton(airyToggle);
        styleHudButton(anaToggle);

        refreshPowerButtons();

        atarToggle.addActionListener(ev -> {
            if (atarToggle.isSelected()) env.activateAtar();
            else env.setActiveAtar(false);
            refreshPowerButtons();
        });

        airyToggle.addActionListener(ev -> {
            if (airyToggle.isSelected()) env.activateAiryaman();
            else env.setActiveAiryaman(false);
            refreshPowerButtons();
        });

        anaToggle.addActionListener(ev -> {
            env.setActiveAnahita(anaToggle.isSelected());
            if (env.isActiveAnahita()) env.applyEffect("Anahita");
        });

        atarToggle.setPreferredSize(new Dimension(80, 30));
        airyToggle.setPreferredSize(new Dimension(100, 30));
        anaToggle.setPreferredSize(new Dimension(90, 30));

        powerBar.add(atarToggle);
        powerBar.add(airyToggle);
        powerBar.add(anaToggle);

        gameView.addCustomComponent(powerBar);

        // افزودن لیستنر برای تغییر سایز
        gameView.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                powerBar.setBounds(0, gameView.getHeight() - 50, gameView.getWidth(), 50);
            }
        });
    }

    private void styleHudButton(AbstractButton b) {
        b.setBackground(Color.DARK_GRAY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
    }


    public void refreshPowerButtons() {
        atarToggle.setEnabled(env.hasAtar());
        airyToggle.setEnabled(env.hasAiryaman());
        anaToggle.setEnabled(env.hasAnahita());

        atarToggle.setSelected(env.isActiveAtar());
        airyToggle.setSelected(env.isActiveAiryaman());
        anaToggle.setSelected(env.isActiveAnahita());
    }


    private void showGameOver() {
        double lossPct = env.getTotalPacketLossPercent();
        GameOverScreen over = new GameOverScreen(lossPct);
        over.addReturnButtonListener(e -> {
            SoundManager.stopBackgroundMusic();
            app.returnFromGameToMenu();
        });
        app.getFrame().setContentPane(over);
        app.getFrame().revalidate();
        app.getFrame().repaint();
    }

    public JComponent getView() {
        return gameView;
    }

    public void dispose() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
    }

    private void createStoreButton() {
        JButton storeButton = new JButton("Store");
        storeButton.setBounds(30, 750, 100, 30);
        styleHudButton(storeButton);

        storeButton.addActionListener(e -> openStore());
        gameView.addCustomComponent(storeButton);
    }

    private void openStore() {
        // ایجاد کنترلر استور
        StoreController storeController = new StoreController(
                env,
                app.getFrame(),
                this::refreshUI // callback برای پس از بسته شدن استور
        );

        storeController.show();
    }
    private void refreshUI() {
        // به‌روزرسانی UI بازی پس از بسته شدن استور
        refreshPowerButtons();
        gameView.refresh();
    }
}
