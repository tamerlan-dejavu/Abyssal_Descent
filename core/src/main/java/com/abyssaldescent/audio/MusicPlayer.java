package com.abyssaldescent.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;

public final class MusicPlayer {

    private Music   music;
    private String  currentPath;
    private float   volume = 0.5f;

    /** Play a specific asset-relative path (e.g. "music/upper_ruins/battle_arena.mp3").
     *  Falls back to the first .mp3 found in assets/ if the path does not exist. */
    public void playTrack(String path) {
        if (path != null && path.equals(currentPath)) return;

        stopCurrent();

        FileHandle handle = resolveTrack(path);
        if (handle == null) {
            Gdx.app.log("MusicPlayer", "No track found for: " + path);
            return;
        }

        try {
            music       = Gdx.audio.newMusic(handle);
            currentPath = path;
            music.setLooping(true);
            music.setVolume(volume);
            music.play();
            Gdx.app.log("MusicPlayer", "Playing: " + handle.path());
        } catch (RuntimeException e) {
            Gdx.app.error("MusicPlayer", "Failed to play: " + handle.path(), e);
            music       = null;
            currentPath = null;
        }
    }

    /** Convenience: start with the first .mp3 found (legacy behaviour). */
    public void start() {
        playTrack(null);
    }

    public void setVolume(float v) {
        volume = v;
        if (music != null) music.setVolume(v);
    }

    public void dispose() {
        stopCurrent();
    }

    // ── internals ─────────────────────────────────────────────────────────────

    private void stopCurrent() {
        if (music != null) {
            music.stop();
            music.dispose();
            music       = null;
            currentPath = null;
        }
    }

    private FileHandle resolveTrack(String path) {
        if (path != null) {
            FileHandle h = Gdx.files.internal(path);
            if (h.exists()) return h;
            Gdx.app.log("MusicPlayer", "Track not found at: " + path + " — falling back");
        }
        return findFirstMp3();
    }

    private FileHandle findFirstMp3() {
        String hit = scanDir(new File("."));
        if (hit != null) {
            FileHandle h = Gdx.files.internal(hit);
            if (h.exists()) return h;
        }
        hit = scanDir(new File("assets"));
        if (hit != null) {
            FileHandle h = Gdx.files.internal("assets/" + hit);
            if (h.exists()) return h;
            h = Gdx.files.internal(hit);
            if (h.exists()) return h;
        }
        FileHandle cp = Gdx.files.classpath("11. Dangerous Cave.mp3");
        if (cp.exists()) return cp;
        return null;
    }

    private String scanDir(File dir) {
        if (!dir.exists() || !dir.isDirectory()) return null;
        File[] files = dir.listFiles();
        if (files == null) return null;
        for (File f : files) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(".mp3")) {
                return f.getName();
            }
        }
        return null;
    }
}
