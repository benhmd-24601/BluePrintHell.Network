package org.example.controller;

import org.example.model.*;
import org.example.model.Systems.NetworkSystem;
import org.example.model.Systems.SourceSystem;
import org.example.util.SaveLoadManager;
import org.example.util.SoundManager;
import org.example.view.GameView;

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
            case 3 -> StageFactory.createStage3();
            case 4 -> StageFactory.createStage4();
            case 5 -> StageFactory.createStage5();
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
        createPowerButtons();
        createSourceButtons();

        // Key binding: toggle mute
        JComponent content = app.getFrame().getRootPane();
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(SoundManager.InputSettings.getMuteKey(), 0), "toggleMute");
        content.getActionMap().put("toggleMute", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { SoundManager.toggleMute(); }
        });

        // Game loop
        gameLoop = new GameLoop(env, gameView, this::refreshPowerButtons);
        gameView.setGameLoop(gameLoop);

        // Game over callback
        env.setOnGameOver(this::showGameOver);

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
                btn.addActionListener(e -> handleSourceButtonClick(src, btn));
                gameView.addCustomComponent(btn);
            }
        }
    }

    private void handleSourceButtonClick(SourceSystem source, JButton button) {
        // همه‌ی سیستم‌ها باید متصل باشند
        boolean allConnected = true;
        for (NetworkSystem system : env.getSystems()) {
            if (!system.isSourceSystem() && !system.isIndicatorOn()) { allConnected = false; break; }
        }
        if (!allConnected) return;

        // Play ⇄ Pause
        boolean nextRunning = !source.isGenerating();
        source.setGenerating(nextRunning);
        env.setMovementPaused(!nextRunning);

        // سیو فوری حالت (برای ادامهٔ دقیق بعد از Load)
        SaveLoadManager.saveLevelNow(env);

        button.setText(nextRunning ? "❚❚" : "◀");
    }

    private void createPowerButtons() {
        powerBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 5));
        powerBar.setOpaque(false);
        powerBar.setBounds(0, gameView.getHeight() - 50, gameView.getWidth(), 50);

        atarToggle = new JToggleButton("Atar");
        airyToggle = new JToggleButton("Airyaman");
        anaToggle = new JToggleButton("Anahita");

        styleHudButton(atarToggle);
        styleHudButton(airyToggle);
        styleHudButton(anaToggle);

        refreshPowerButtons();

        atarToggle.addActionListener(ev -> {
            if (atarToggle.isSelected()) env.activateAtar(); else env.setActiveAtar(false);
            refreshPowerButtons();
        });

        airyToggle.addActionListener(ev -> {
            if (airyToggle.isSelected()) env.activateAiryaman(); else env.setActiveAiryaman(false);
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

        gameView.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
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
        SwingUtilities.invokeLater(() -> {
            if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
            app.showGameOver(env.getTotalPacketLossPercent());
        });
    }

    public JComponent getView() { return gameView; }

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
        StoreController storeController = new StoreController(
                env,
                app.getFrame(),
                this::refreshUI
        );
        storeController.show();
    }

    private void refreshUI() {
        refreshPowerButtons();
        gameView.refresh();
    }

    public GameEnv getEnv() { return env; }
}
