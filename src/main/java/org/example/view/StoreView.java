package org.example.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class StoreView extends JPanel {
    private JLabel coinsLabel;
    private JButton atarBuy, airyamanBuy, anahitaBuy, backBtn;

    // NEW:
    private JButton aergiaBtn, eliphasBtn, sisyphusBtn, curvePointBtn;
    private JLabel  curvePointLabel;

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

        // ===== NEW: Scrolls & Curve Point =====
        aergiaBtn = new JButton("Scroll of Aergia (10)");
        aergiaBtn.setFont(f);
        gbc.gridy = 4;
        add(createPlainPanel(aergiaBtn, "Freeze accel at a point for 20s"), gbc);

        eliphasBtn = new JButton("Scroll of Eliphas (20)");
        eliphasBtn.setFont(f);
        gbc.gridy = 5;
        add(createPlainPanel(eliphasBtn, "Re-center CoM near a point for 30s"), gbc);

        sisyphusBtn = new JButton("Scroll of Sisyphus (15)");
        sisyphusBtn.setFont(f);
        gbc.gridy = 6;
        add(createPlainPanel(sisyphusBtn, "Move one non-source system within radius"), gbc);

        JPanel curvePanel = new JPanel(new BorderLayout(10,0));
        curvePanel.setOpaque(false);
        curvePointBtn = new JButton("Buy Curve Point (1)");
        curvePointBtn.setFont(f);
        curvePointLabel = new JLabel("Curve points: 0");
        curvePointLabel.setForeground(Color.WHITE);
        curvePointLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        curvePanel.add(curvePointLabel, BorderLayout.WEST);
        curvePanel.add(curvePointBtn, BorderLayout.EAST);
        gbc.gridy = 7;
        add(curvePanel, gbc);


        JTextArea help = new JTextArea(
                """
                روش استفادهٔ ابزارها:
                • Shift + کلیک نزدیک سیم → افزودن نقطهٔ کرو (از Curve Points مصرف می‌شود)
                • Ctrl  + کلیک روی سیم   → Scroll of Aergia (شتاب صفر؛ 20s؛ کول‌داون دارد؛ 10 کوین)
                • Alt   + کلیک روی سیم   → Scroll of Eliphas (بازگردانی مرکز ثقل؛ 30s؛ 20 کوین)
                • Shift نگه‌دار + کلیک روی سیستم غیرمرجع و درگ → Sisyphus (15 کوین؛ شعاع محدود و بدون نقض قیود)
                """
        );
        help.setFont(new Font("Vazirmatn", Font.PLAIN, 13));
        help.setForeground(Color.WHITE);
        help.setOpaque(false);
        help.setEditable(false);
        help.setLineWrap(true);
        help.setWrapStyleWord(true);
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0; gbc1.gridy = 8; gbc1.insets = new Insets(10,10,10,10);
        gbc1.fill = GridBagConstraints.HORIZONTAL; gbc1.weightx = 1.0;
        add(help, gbc1);

        backBtn = new JButton("Back");
        backBtn.setFont(f);
        gbc1.gridy = 8;
        add(backBtn, gbc1);
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

    private JPanel createPlainPanel(JButton btn, String hint) {
        JPanel p = new JPanel(new BorderLayout(10,0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(hint);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(new Color(220,220,220));
        p.add(lbl, BorderLayout.WEST);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    // قبلی
    public void setAtarBuyListener(ActionListener listener) { atarBuy.addActionListener(listener); }
    public void setAiryamanBuyListener(ActionListener listener) { airyamanBuy.addActionListener(listener); }
    public void setAnahitaBuyListener(ActionListener listener) { anahitaBuy.addActionListener(listener); }
    public void setBackListener(ActionListener listener) { backBtn.addActionListener(listener); }

    // NEW: لیسنرهای اسکرول‌ها و نقطه‌ی کرو
    public void setAergiaListener(ActionListener l) { aergiaBtn.addActionListener(l); }
    public void setEliphasListener(ActionListener l) { eliphasBtn.addActionListener(l); }
    public void setSisyphusListener(ActionListener l) { sisyphusBtn.addActionListener(l); }
    public void setCurvePointListener(ActionListener l) { curvePointBtn.addActionListener(l); }

    // قبلی (برای سه قابلیت قدیمی)
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

    // NEW: به‌روزرسانی وضعیت اسکرول‌ها + ظرفیت کرو
    public void updateScrollsState(int coins, double aergiaCooldown, int curvePoints) {
        coinsLabel.setText("Coins: " + coins);
        aergiaBtn.setEnabled(coins >= 10 && aergiaCooldown <= 0.0);
        eliphasBtn.setEnabled(coins >= 20);
        sisyphusBtn.setEnabled(coins >= 15);
        curvePointBtn.setEnabled(coins >= 1);
        curvePointLabel.setText("Curve points: " + curvePoints);

        if (aergiaCooldown > 0) {
            aergiaBtn.setText(String.format("Scroll of Aergia (CD %.0fs)", Math.ceil(aergiaCooldown)));
        } else {
            aergiaBtn.setText("Scroll of Aergia (10)");
        }
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
