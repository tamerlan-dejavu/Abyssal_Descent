package com.abyssaldescent.audio;

public class AudioChannel {

    private final AudioCategory category;
    private float  volume     = 1.0f;   // 0.0 – 1.0
    private boolean muted     = false;

    // Fade
    private float fadeFrom    = 1.0f;
    private float fadeTo      = 1.0f;
    private float fadeDuration = 0f;
    private float fadeTimer   = 0f;
    private boolean fading    = false;

    // ─────────────────────────────────────────────────────────────────────────

    public AudioChannel(AudioCategory category, float defaultVolume) {
        this.category = category;
        this.volume   = defaultVolume;
    }

    // ── Обновление (fade) ─────────────────────────────────────────────────────

    public void update(float delta) {
        if (!fading) return;

        fadeTimer += delta;
        float t = Math.min(1f, fadeTimer / fadeDuration);

        // Плавная интерполяция
        volume = fadeFrom + (fadeTo - fadeFrom) * t;

        if (t >= 1f) {
            volume = fadeTo;
            fading = false;
        }
    }

    // ── Fade ─────────────────────────────────────────────────────────────────

    /**
     * Плавно изменить громкость за durationSec секунд.
     */
    public void fadeTo(float targetVolume, float durationSec) {
        if (durationSec <= 0f) {
            volume = targetVolume;
            fading = false;
            return;
        }
        fadeFrom    = volume;
        fadeTo      = Math.max(0f, Math.min(1f, targetVolume));
        fadeDuration = durationSec;
        fadeTimer   = 0f;
        fading      = true;
    }

    /** Fade-out до 0. */
    public void fadeOut(float durationSec) { fadeTo(0f, durationSec); }

    /** Fade-in до targetVolume. */
    public void fadeIn(float targetVolume, float durationSec) { fadeTo(targetVolume, durationSec); }

    // ── Громкость ─────────────────────────────────────────────────────────────

    public void  setVolume(float v) { this.volume = Math.max(0f, Math.min(1f, v)); }
    public float getVolume()        { return muted ? 0f : volume; }
    public float getRawVolume()     { return volume; }

    public void    setMuted(boolean m) { this.muted = m; }
    public boolean isMuted()           { return muted; }
    public void    toggleMute()        { muted = !muted; }

    public AudioCategory getCategory()  { return category; }
    public boolean       isFading()     { return fading; }
}