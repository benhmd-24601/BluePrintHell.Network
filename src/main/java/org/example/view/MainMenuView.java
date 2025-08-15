package org.example.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuView extends JPanel {
    private final JButton levelsButton, settingsButton, exitButton;
    private final Image backgroundImage;

    public MainMenuView() {
        backgroundImage = new ImageIcon("assets/backgrounds/menu_background.jpg").getImage();

        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 0, 20, 0);
        gbc.gridx = 0;

        Font customFont = new Font("Arial", Font.BOLD, 26);

        levelsButton = createStyledButton(" Levels", customFont, Color.BLACK);
        settingsButton = createStyledButton(" Settings", customFont, Color.BLACK);
        exitButton = createStyledButton(" Exit", customFont, Color.BLACK);

        addComponent(gbc, levelsButton, 0);
        addComponent(gbc, settingsButton, 1);
        addComponent(gbc, exitButton, 2);
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
        this.add(button, gbc);
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

    public void addLevelsButtonListener(ActionListener l) { levelsButton.addActionListener(l); }
    public void addSettingsButtonListener(ActionListener l) { settingsButton.addActionListener(l); }
    public void addExitButtonListener(ActionListener l) { exitButton.addActionListener(l); }
}
