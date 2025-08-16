// src/main/java/org/example/view/GameOverView.java
package org.example.view;

import javax.swing.*;
import java.awt.*;

public class GameOverView extends JPanel {
    private final JButton returnButton = new JButton("Return to Menu");
    private final JLabel lossLabel = new JLabel("", SwingConstants.CENTER);

    public GameOverView() {
        setBackground(new Color(25, 25, 25));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Game Over!");
        title.setFont(new Font("Consolas", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(20, 20, 10, 20);
        add(title, gbc);

        lossLabel.setFont(new Font("Consolas", Font.PLAIN, 24));
        lossLabel.setForeground(Color.WHITE);
        gbc.gridy = 1; gbc.insets = new Insets(0, 20, 30, 20);
        add(lossLabel, gbc);

        styleButton(returnButton);
        returnButton.setPreferredSize(new Dimension(250, 50));
        gbc.gridy = 2; gbc.insets = new Insets(0, 20, 20, 20);
        add(returnButton, gbc);
    }

    private void styleButton(JButton b) {
        b.setBackground(Color.DARK_GRAY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        b.setFont(new Font("Consolas", Font.PLAIN, 18));
    }

    /** فقط UI را آپدیت می‌کند؛ لاجیک ندارد */
    public void setLossPercent(double lossPercent) {
        lossLabel.setText(String.format("Packet Loss: %.1f%%", lossPercent));
    }

    /** کنترلر خودش لیسنر می‌چسباند؛ ویو فقط accessor می‌دهد */
    public JButton getReturnButton() {
        return returnButton;
    }
}
