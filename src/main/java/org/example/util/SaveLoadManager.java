package org.example.util;

import java.io.*;

public class SaveLoadManager {
    private static final String SAVE_FILE = "save.dat";
    private static boolean[] passedLevels;

    public static void init(int numLevels) {
        passedLevels = new boolean[numLevels];
        File f = new File(SAVE_FILE);
        if (f.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
                boolean[] loaded = (boolean[]) in.readObject();
                if (loaded.length == numLevels) passedLevels = loaded;
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(passedLevels);
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    public static void markPassed(int levelIndex) {
        if (levelIndex >= 0 && levelIndex < passedLevels.length) {
            passedLevels[levelIndex] = true;
            save();
        }
    }

    public static boolean isUnlocked(int levelIndex) {
        if (levelIndex == 0) return true;
        if (levelIndex > 0 && levelIndex < passedLevels.length)
            return passedLevels[levelIndex - 1];
        return false;
    }
}
