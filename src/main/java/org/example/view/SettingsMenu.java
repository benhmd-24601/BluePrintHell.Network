package org.example.view;

import org.example.util.SoundManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class SettingsMenu extends JPanel {
    private static final int SQUARE = 40;
    private final JButton backButton;
    private final JSlider volumeSlider;
    private final JButton rebindMuteBtn;

    public SettingsMenu() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        JLabel lbl = new JLabel("Music Volume:");
        lbl.setFont(new Font("Consolas", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0;
        add(lbl, gbc);

        volumeSlider = new JSlider(0, 100, (int)(SoundManager.getMusicVolume() * 100));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setOpaque(false);
        volumeSlider.setFont(new Font("Consolas", Font.PLAIN, 12));
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                float vol = volumeSlider.getValue() / 100f;
                SoundManager.setMusicVolume(vol);
            }
        });
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(volumeSlider, gbc);

        backButton = new JButton("Back to Menu");
        styleHudButton(backButton);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        add(backButton, gbc);

        rebindMuteBtn = new JButton("Mute Key: " + KeyEvent.getKeyText(SoundManager.InputSettings.getMuteKey()));
        rebindMuteBtn.setFont(new Font("Consolas", Font.PLAIN, 16));
        rebindMuteBtn.setBackground(Color.GRAY);
        rebindMuteBtn.setForeground(Color.WHITE);
        rebindMuteBtn.setFocusPainted(false);
        rebindMuteBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        gbc.gridx = 1; gbc.gridy = 1;
        add(rebindMuteBtn, gbc);

        rebindMuteBtn.addActionListener(e -> {
            rebindMuteBtn.setText("Press any key…");
            requestFocusInWindow();
        });

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent ev) {
                int code = ev.getKeyCode();
                SoundManager.InputSettings.setMuteKey(code);
                rebindMuteBtn.setText("Mute Key: " + KeyEvent.getKeyText(code));
            }
        });
        setFocusable(true);
    }

    public void addBackButtonListener(ActionListener l) { backButton.addActionListener(l); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        for (int y = 0; y < getHeight(); y += SQUARE) {
            for (int x = 0; x < getWidth(); x += SQUARE) {
                boolean dark = ((x / SQUARE) + (y / SQUARE)) % 2 == 0;
                g2.setColor(dark ? new Color(40, 40, 40) : new Color(60, 60, 60));
                g2.fillRect(x, y, SQUARE, SQUARE);
            }
        }
        g2.dispose();
        super.paintComponent(g);
    }

    private void styleHudButton(JButton b) {
        b.setBackground(Color.DARK_GRAY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        b.setFont(new Font("Consolas", Font.BOLD, 16));
    }
}
