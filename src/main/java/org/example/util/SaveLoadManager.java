package org.example.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.GameEnv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SaveLoadManager {
    private static final String SAVE_FILE = "game_save.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveGame(GameEnv env) {
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(env, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static GameEnv loadGame() {
        try (FileReader reader = new FileReader(SAVE_FILE)) {
            return gson.fromJson(reader, GameEnv.class);
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }
}
