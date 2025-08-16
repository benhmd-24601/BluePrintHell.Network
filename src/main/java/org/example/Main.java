package org.example;

import javax.swing.SwingUtilities;
import org.example.controller.AppController;

public class  Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AppController().start(); // فقط بوت‌استرپ
        });
    }
}
