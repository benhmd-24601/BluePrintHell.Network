package org.example.util;

import javax.sound.sampled.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    private static Clip bgmClip;
    private static float musicVolume = 1.0f;
    private static boolean muted = false;

    public static class InputSettings {
        private static int muteKey = KeyEvent.VK_M;
        public static int getMuteKey() { return muteKey; }
        public static void setMuteKey(int code) { muteKey = code; }
    }

    private static Clip loadClip(String resourcePath) {
        URL url = SoundManager.class.getResource(resourcePath);
        if (url == null) {
            System.err.println("⚠️ Sound resource not found: " + resourcePath);
            return null;
        }
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(url)) {
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void init() { bgmClip = loadClip("/khakestari.wav"); }

    public static void toggleMute() {
        muted = !muted;
        if (muted) stopBackgroundMusic();
        else playBackgroundMusic();
    }

    public static boolean isMuted() { return muted; }

    public static void playBackgroundMusic() {
        if (bgmClip == null) return;
        if (bgmClip.isRunning()) return;
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        setMusicVolume(musicVolume);
        bgmClip.start();
    }

    public static void stopBackgroundMusic() { if (bgmClip != null) bgmClip.stop(); }

    public static void setMusicVolume(float v) {
        musicVolume = Math.max(0f, Math.min(1f, v));
        if (bgmClip == null) return;
        FloatControl gain = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = 20f * (float) Math.log10(Math.max(musicVolume, 0.0001f));
        gain.setValue(dB);
    }

    public static float getMusicVolume() { return musicVolume; }
}
