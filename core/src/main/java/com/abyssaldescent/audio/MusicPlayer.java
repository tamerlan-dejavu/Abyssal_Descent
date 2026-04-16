package com.abyssaldescent.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import java.io.File;

public final class MusicPlayer {

    private Music music;

    public void start() {
        FileHandle track = findFirstMp3();
        if (track == null) {
            Gdx.app.log("MusicPlayer", "No .mp3 file found in assets folder.");
            return;
        }

        try {
            Gdx.app.log("MusicPlayer", "Playing: " + track.path());
            music = Gdx.audio.newMusic(track);
            music.setLooping(true);
            music.setVolume(0.5f);
            music.play();
        } catch (RuntimeException e) {
            Gdx.app.error("MusicPlayer", "Failed to play: " + track.path(), e);
        }
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

    public void dispose() {
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
        }
    }
}
