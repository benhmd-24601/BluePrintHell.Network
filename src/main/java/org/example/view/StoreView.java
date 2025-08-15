//package org.example.view;
//
//import org.example.model.GameEnv;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class Store extends JPanel {
//    private final GameEnv env;
//    private final Runnable backToGame;
//    private JLabel coinsLabel;
//    private JButton atarBuy, airyamanBuy, anahitaBuy, backBtn;
//    private static final int SQUARE = 40;
//    private final Runnable onPurchaseCallback;
//
//    public Store(GameEnv env, Runnable backToGame , Runnable onPurchaseCallback) {
//        this.env = env;
//        this.backToGame = backToGame;
//        this.onPurchaseCallback = onPurchaseCallback;
//
//        setOpaque(false);
//        setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(10,10,10,10);
//        gbc.gridx = 0;
//
//        coinsLabel = new JLabel("Coins: " + env.getCoins());
//        coinsLabel.setFont(new Font("Consolas", Font.BOLD, 24));
//        coinsLabel.setForeground(Color.WHITE);
//        gbc.gridy = 0; gbc.gridwidth = 2;
//        add(coinsLabel, gbc);
//        gbc.gridwidth = 1;
//
//        Font f = new Font("Arial", Font.BOLD, 20);
//
//        atarBuy = new JButton(env.hasAtar() ? "Purchased" : "Buy (3)");
//        atarBuy.setEnabled(env.canBuyAtar());
//        atarBuy.setFont(f);
//        atarBuy.addActionListener(e -> { env.buyAtar(); refreshButtons(); });
//        gbc.gridy = 1;
//        add(createAbilityPanel("O' Atar", 3, atarBuy, env.hasAtar()), gbc);
//
//        airyamanBuy = new JButton(env.hasAiryaman() ? "Purchased" : "Buy (4)");
//        airyamanBuy.setEnabled(env.canBuyAiryaman());
//        airyamanBuy.setFont(f);
//        airyamanBuy.addActionListener(e -> { env.buyAiryaman(); refreshButtons(); });
//        gbc.gridy = 2;
//        add(createAbilityPanel("O' Airyaman", 4, airyamanBuy, env.hasAiryaman()), gbc);
//
//        anahitaBuy = new JButton(env.hasAnahita() ? "Purchased" : "Buy (5)");
//        anahitaBuy.setEnabled(env.canBuyAnahita());
//        anahitaBuy.setFont(f);
//        anahitaBuy.addActionListener(e -> { env.buyAnahita(); refreshButtons(); });
//        gbc.gridy = 3;
//        add(createAbilityPanel("O' Anahita", 5, anahitaBuy, env.hasAnahita()), gbc);
//
//        backBtn = new JButton("Back");
//        backBtn.setFont(f);
//        backBtn.addActionListener(e -> backToGame.run());
//        gbc.gridy = 4;
//        add(backBtn, gbc);
//    }
//
//    private JPanel createAbilityPanel(String name, int cost, JButton buyBtn, boolean owned) {
//        JPanel p = new JPanel(new BorderLayout(10,0));
//        p.setOpaque(false);
//        JLabel lbl = new JLabel(name);
//        lbl.setFont(new Font("Arial", Font.BOLD, 20));
//        lbl.setForeground(owned ? Color.WHITE : Color.GRAY);
//        p.add(lbl, BorderLayout.WEST);
//        p.add(buyBtn, BorderLayout.EAST);
//        return p;
//    }
//
//    private void refreshButtons() {
//        coinsLabel.setText("Coins: " + env.getCoins());
//
//        atarBuy.setText(env.hasAtar() ? "Purchased" : "Buy (3)");
//        atarBuy.setEnabled(env.canBuyAtar());
//        ((JLabel)((JPanel)getComponent(1)).getComponent(0))
//                .setForeground(env.hasAtar() ? Color.WHITE : Color.GRAY);
//
//        airyamanBuy.setText(env.hasAiryaman() ? "Purchased" : "Buy (4)");
//        airyamanBuy.setEnabled(env.canBuyAiryaman());
//        ((JLabel)((JPanel)getComponent(2)).getComponent(0))
//                .setForeground(env.hasAiryaman() ? Color.WHITE : Color.GRAY);
//
//        anahitaBuy.setText(env.hasAnahita() ? "Purchased" : "Buy (5)");
//        anahitaBuy.setEnabled(env.canBuyAnahita());
//        ((JLabel)((JPanel)getComponent(3)).getComponent(0))
//                .setForeground(env.hasAnahita() ? Color.WHITE : Color.GRAY);
//
//        if (onPurchaseCallback != null) {
//            onPurchaseCallback.run();
//        }
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g.create();
//        for (int y = 0; y < getHeight(); y += SQUARE) {
//            for (int x = 0; x < getWidth(); x += SQUARE) {
//                boolean dark = ((x / SQUARE) + (y / SQUARE)) % 2 == 0;
//                g2.setColor(dark? new Color(40,40,40) : new Color(60,60,60));
//                g2.fillRect(x, y, SQUARE, SQUARE);
//            }
//        }
//        g2.dispose();
//        super.paintComponent(g);
//    }
//}
package org.example.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class StoreView extends JPanel {
    private JLabel coinsLabel;
    private JButton atarBuy, airyamanBuy, anahitaBuy, backBtn;
    private static final int SQUARE = 40;

    public StoreView(int coins, boolean hasAtar, boolean hasAiryaman, boolean hasAnahita) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0;

        coinsLabel = new JLabel("Coins: " + coins);
        coinsLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        coinsLabel.setForeground(Color.WHITE);
        gbc.gridy = 0; gbc.gridwidth = 2;
        add(coinsLabel, gbc);
        gbc.gridwidth = 1;

        Font f = new Font("Arial", Font.BOLD, 20);

        atarBuy = new JButton(hasAtar ? "Purchased" : "Buy (3)");
        atarBuy.setEnabled(!hasAtar && coins >= 3);
        atarBuy.setFont(f);
        gbc.gridy = 1;
        add(createAbilityPanel("O' Atar", 3, atarBuy, hasAtar), gbc);

        airyamanBuy = new JButton(hasAiryaman ? "Purchased" : "Buy (4)");
        airyamanBuy.setEnabled(!hasAiryaman && coins >= 4);
        airyamanBuy.setFont(f);
        gbc.gridy = 2;
        add(createAbilityPanel("O' Airyaman", 4, airyamanBuy, hasAiryaman), gbc);

        anahitaBuy = new JButton(hasAnahita ? "Purchased" : "Buy (5)");
        anahitaBuy.setEnabled(!hasAnahita && coins >= 5);
        anahitaBuy.setFont(f);
        gbc.gridy = 3;
        add(createAbilityPanel("O' Anahita", 5, anahitaBuy, hasAnahita), gbc);

        backBtn = new JButton("Back");
        backBtn.setFont(f);
        gbc.gridy = 4;
        add(backBtn, gbc);
    }

    public void updateState(int coins, boolean hasAtar, boolean hasAiryaman, boolean hasAnahita) {
        coinsLabel.setText("Coins: " + coins);

        atarBuy.setText(hasAtar ? "Purchased" : "Buy (3)");
        atarBuy.setEnabled(!hasAtar && coins >= 3);
        ((JLabel)((JPanel)getComponent(1)).getComponent(0))
                .setForeground(hasAtar ? Color.WHITE : Color.GRAY);

        airyamanBuy.setText(hasAiryaman ? "Purchased" : "Buy (4)");
        airyamanBuy.setEnabled(!hasAiryaman && coins >= 4);
        ((JLabel)((JPanel)getComponent(2)).getComponent(0))
                .setForeground(hasAiryaman ? Color.WHITE : Color.GRAY);

        anahitaBuy.setText(hasAnahita ? "Purchased" : "Buy (5)");
        anahitaBuy.setEnabled(!hasAnahita && coins >= 5);
        ((JLabel)((JPanel)getComponent(3)).getComponent(0))
                .setForeground(hasAnahita ? Color.WHITE : Color.GRAY);
    }

    private JPanel createAbilityPanel(String name, int cost, JButton buyBtn, boolean owned) {
        JPanel p = new JPanel(new BorderLayout(10,0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(name);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setForeground(owned ? Color.WHITE : Color.GRAY);
        p.add(lbl, BorderLayout.WEST);
        p.add(buyBtn, BorderLayout.EAST);
        return p;
    }

    public void setAtarBuyListener(ActionListener listener) {
        atarBuy.addActionListener(listener);
    }

    public void setAiryamanBuyListener(ActionListener listener) {
        airyamanBuy.addActionListener(listener);
    }

    public void setAnahitaBuyListener(ActionListener listener) {
        anahitaBuy.addActionListener(listener);
    }

    public void setBackListener(ActionListener listener) {
        backBtn.addActionListener(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        for (int y = 0; y < getHeight(); y += SQUARE) {
            for (int x = 0; x < getWidth(); x += SQUARE) {
                boolean dark = ((x / SQUARE) + (y / SQUARE)) % 2 == 0;
                g2.setColor(dark? new Color(40,40,40) : new Color(60,60,60));
                g2.fillRect(x, y, SQUARE, SQUARE);
            }
        }
        g2.dispose();
        super.paintComponent(g);
    }
}