package org.example.view;

import org.example.util.SaveLoadManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LevelSelectionView extends JPanel {
    private final JButton level1Button, level2Button, backButton;
    private final Image backgroundImage;

    public LevelSelectionView() {
        backgroundImage = new ImageIcon("assets/backgrounds/menu_background.jpg").getImage();

        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 0, 20, 0);
        gbc.gridx = 0;

        Font customFont = new Font("Arial", Font.BOLD, 26);

        level1Button = createStyledButton(" Level 1", customFont, Color.BLACK);
        level1Button.setEnabled(SaveLoadManager.isUnlocked(0));
        level2Button = createStyledButton(" Level 2", customFont, Color.BLACK);
        level2Button.setEnabled(SaveLoadManager.isUnlocked(1));
        backButton = createStyledButton(" Back to Menu", customFont, Color.DARK_GRAY);

        addComponent(gbc, level1Button, 0);
        addComponent(gbc, level2Button, 1);
        addComponent(gbc, backButton, 2);
    }

    private JButton createStyledButton(String text, Font font, Color bg) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(300, 60));
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return button;
    }

    private void addComponent(GridBagConstraints gbc, JButton button, int y) {
        gbc.gridy = y;
        add(button, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null)
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        int square = 40;
        for (int y = 0; y < getHeight(); y += square) {
            for (int x = 0; x < getWidth(); x += square) {
                g.setColor(((x/square + y/square) % 2 == 0) ? new Color(40,40,40) : new Color(60,60,60));
                g.fillRect(x, y, square, square);
            }
        }
    }

    public void addLevel1Listener(ActionListener l) { level1Button.addActionListener(l); }
    public void addLevel2Listener(ActionListener l) { level2Button.addActionListener(l); }
    public void addBackListener(ActionListener l) { backButton.addActionListener(l); }
}
