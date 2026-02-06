package com.union.brainrush.service;

import org.springframework.stereotype.Service;
import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class SoundService {
    private Clip bgClip;
    private final Map<String, Clip> sfxClips = new HashMap<>();
    private boolean isMuted = false;

    // Load sounds once when the service starts
    public SoundService() {
        loadSfx("click", "sound/click.wav");
        loadSfx("correct", "sound/correct.wav");
        loadSfx("wrong", "sound/wrong.wav");
    }

    private void loadSfx(String name, String path) {
        try {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                sfxClips.put(name, clip);
            }
        } catch (Exception e) {
            System.err.println("Could not load sound: " + path);
        }
    }

    public void playBgSound() {
        try {
            if (bgClip != null && bgClip.isRunning()) return;

            URL url = getClass().getClassLoader().getResource("sound/bg.wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
            bgClip = AudioSystem.getClip();
            bgClip.open(audioStream);

            // Set lower volume for background music
            FloatControl gainControl = (FloatControl) bgClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-15.0f);

            bgClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playSfx(String name) {
        if (isMuted) return;

        Clip clip = sfxClips.get(name);
        if (clip != null) {
            if (clip.isRunning()) clip.stop(); // Stop if already playing
            clip.setFramePosition(0);         // Rewind to start
            clip.start();
        }
    }

    public void stopBgSound() {
        if (bgClip != null) bgClip.stop();
    }

    public void toggleMute() {
        this.isMuted = !isMuted;
        if (isMuted) stopBgSound();
        else playBgSound();
    }
}