package com.abyssaldescent.audio;

import java.util.EnumMap;
import java.util.Map;

public class AudioMixer {

    private final Map<AudioCategory, AudioChannel> channels = new EnumMap<>(AudioCategory.class);

    // ─────────────────────────────────────────────────────────────────────────

    public AudioMixer() {
        // Создать каналы с дефолтными значениями
        channels.put(AudioCategory.MASTER,  new AudioChannel(AudioCategory.MASTER,  1.0f));
        channels.put(AudioCategory.MUSIC,   new AudioChannel(AudioCategory.MUSIC,   0.6f));
        channels.put(AudioCategory.SFX,     new AudioChannel(AudioCategory.SFX,     0.8f));
        channels.put(AudioCategory.UI,      new AudioChannel(AudioCategory.UI,      0.7f));
        channels.put(AudioCategory.AMBIENT, new AudioChannel(AudioCategory.AMBIENT, 0.4f));
    }

   
    public void update(float delta) {
        for (AudioChannel ch : channels.values()) ch.update(delta);
    }

   
    public float getFinalVolume(AudioCategory category) {
        float master = channels.get(AudioCategory.MASTER).getVolume();
        float cat    = channels.get(category).getVolume();
        return master * cat;
    }

   
    public void setVolume(AudioCategory category, float volume) {
        channels.get(category).setVolume(volume);
    }

    public float getVolume(AudioCategory category) {
        return channels.get(category).getRawVolume();
    }

    public void setMuted(AudioCategory category, boolean muted) {
        channels.get(category).setMuted(muted);
    }

    public boolean isMuted(AudioCategory category) {
        return channels.get(category).isMuted();
    }

    public void toggleMute(AudioCategory category) {
        channels.get(category).toggleMute();
    }

    /** Заглушить всё. */
    public void muteAll() {
        channels.get(AudioCategory.MASTER).setMuted(true);
    }

    /** Включить всё. */
    public void unmuteAll() {
        channels.get(AudioCategory.MASTER).setMuted(false);
    }

    // ── Fade ─────────────────────────────────────────────────────────────────

    public void fadeTo(AudioCategory category, float target, float duration) {
        channels.get(category).fadeTo(target, duration);
    }

    public void fadeOut(AudioCategory category, float duration) {
        channels.get(category).fadeOut(duration);
    }

    public void fadeIn(AudioCategory category, float targetVolume, float duration) {
        channels.get(category).fadeIn(targetVolume, duration);
    }

    /**
     * Crossfade музыки: плавно убрать текущую и поднять новую.
     * Вызывать при смене яруса.
     *
     * @param duration секунд перехода
     */
    public void crossfadeMusic(float duration) {
        // MusicPlayer сам управляет crossfade треков,
        // здесь только fade громкости канала MUSIC
        channels.get(AudioCategory.MUSIC).fadeTo(
                channels.get(AudioCategory.MUSIC).getRawVolume(), duration);
    }

    /** Fade-out всего аудио (например, при переходе экрана). */
    public void fadeOutAll(float duration) {
        channels.get(AudioCategory.MASTER).fadeOut(duration);
    }

    /** Fade-in мастера после перехода. */
    public void fadeInMaster(float duration) {
        channels.get(AudioCategory.MASTER).fadeIn(1.0f, duration);
    }

    // ── Пресеты ───────────────────────────────────────────────────────────────

    /** Громкость при паузе — приглушить музыку до 20%. */
    public void applyPausePreset() {
        channels.get(AudioCategory.MUSIC).fadeTo(0.2f, 0.3f);
        channels.get(AudioCategory.AMBIENT).fadeTo(0.1f, 0.3f);
    }

    /** Вернуть нормальную громкость после паузы. */
    public void applyResumePreset() {
        channels.get(AudioCategory.MUSIC).fadeTo(0.6f, 0.3f);
        channels.get(AudioCategory.AMBIENT).fadeTo(0.4f, 0.3f);
    }

    /** Напряжённый пресет при входе в комнату босса. */
    public void applyBossPreset() {
        channels.get(AudioCategory.AMBIENT).fadeTo(0.0f, 1.0f);
    }

    /** Сброс к дефолтным значениям. */
    public void resetToDefaults() {
        setVolume(AudioCategory.MASTER,  1.0f);
        setVolume(AudioCategory.MUSIC,   0.6f);
        setVolume(AudioCategory.SFX,     0.8f);
        setVolume(AudioCategory.UI,      0.7f);
        setVolume(AudioCategory.AMBIENT, 0.4f);
        for (AudioChannel ch : channels.values()) ch.setMuted(false);
    }

    public AudioChannel getChannel(AudioCategory category) {
        return channels.get(category);
    }
}