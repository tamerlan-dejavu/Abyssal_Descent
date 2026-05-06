package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Full-screen settings overlay.
 *
 * <p>The blurred background is drawn by MainMenuScreen — this class renders only
 * the interactive layer (sliders, fullscreen toggle, Back button).
 *
 * <h3>Layout</h3>
 * <ul>
 *   <li>Three volume sliders (Master / Music / SFX) + Fullscreen toggle — centred
 *       horizontally and placed at ~58 % of screen height.</li>
 *   <li>Back button — bottom-right corner, 500×150 px, 40 px margin.</li>
 * </ul>
 *
 * <h3>Slider colours</h3>
 * Track and thumb are rendered in greyscale so they don't fight the background.
 */
class SettingsOverlay extends MenuOverlay {

    static final String PREFS = "abyssal_descent_settings";

    // ── sizing constants ──────────────────────────────────────────────────────
    private static final float SLIDER_W  = 500f;
    private static final float SLIDER_H  = 14f;
    private static final float THUMB_R   = 14f;
    private static final float LABEL_W   = 280f;
    private static final float LABEL_GAP = 50f;   // gap between label column and slider
    private static final float VALUE_GAP = 20f;   // gap between slider end and value text
    private static final float ROW_H     = 100f;  // vertical distance between rows
    private static final float BTN_W     = 500f;
    private static final float BTN_H     = 150f;
    private static final float MARGIN    = 40f;

    // ── state ─────────────────────────────────────────────────────────────────
    float   masterVolume, musicVolume, sfxVolume;
    boolean fullscreen;

    // ── layout fields (rebuilt on resize) ────────────────────────────────────
    private float   labelX, sliderX, valueX;
    private float[] sliderY;            // Y baselines for 3 sliders
    private float   toggleRowY;         // Y for fullscreen row
    private int     draggingSlider = -1;

    private MenuButton fullscreenBtn;
    private MenuButton backBtn;

    // ── constructor ───────────────────────────────────────────────────────────

    SettingsOverlay(Texture panelTex, Runnable onClose) {
        super(panelTex, onClose);
        loadSettings();
    }

    // ── background overrides — full-screen; MainMenuScreen handles the bg ─────

    @Override
    void renderFallbackBackground(ShapeRenderer shapes) {}

    @Override
    void renderPanelTexture(SpriteBatch batch) {}

    // ── layout ────────────────────────────────────────────────────────────────

    /**
     * Rebuilds all positions.  Call with {@code (0, 0, screenW, screenH)}.
     */
    @Override
    void rebuildLayout(float px, float py, float pw, float ph) {
        panelX = px; panelY = py; panelW = pw; panelH = ph;

        // Total block width = label + gap + slider + gap + value
        float totalW = LABEL_W + LABEL_GAP + SLIDER_W + VALUE_GAP + 60f /*value digits*/;
        labelX  = px + (pw - totalW) / 2f;
        sliderX = labelX + LABEL_W + LABEL_GAP;
        valueX  = sliderX + SLIDER_W + VALUE_GAP;

        // Content top: 58 % of screen height (slightly above centre)
        float contentTop = py + ph * 0.68f;
        sliderY = new float[]{
                contentTop,
                contentTop - ROW_H,
                contentTop - ROW_H * 2
        };

        // Fullscreen toggle one row below sliders
        toggleRowY = contentTop - ROW_H * 3;

        fullscreenBtn = new MenuButton(
                fullscreen ? "ON" : "OFF",
                sliderX, toggleRowY - (BTN_H - SLIDER_H) / 2f,
                BTN_W, BTN_H,
                this::toggleFullscreen);

        // Back — bottom-right corner
        backBtn = new MenuButton(
                "Back",
                px + pw - BTN_W - MARGIN,
                py + MARGIN,
                BTN_W, BTN_H,
                onClose);
    }

    // ── input ─────────────────────────────────────────────────────────────────

    @Override
    void update(float mx, float my) {
        if (backBtn == null) return;
        fullscreenBtn.update(mx, my);
        backBtn.update(mx, my);
    }

    @Override
    boolean handleClick(float wx, float wy) {
        if (backBtn      == null)            return false;
        if (backBtn.handleClick(wx, wy))     return true;
        if (fullscreenBtn.handleClick(wx, wy)) return true;
        for (int i = 0; i < sliderY.length; i++) {
            if (onSliderTrack(wx, wy, i)) {
                draggingSlider = i;
                setSliderValue(wx, i);
                return true;
            }
        }
        return false;
    }

    @Override
    boolean handleDrag(float wx) {
        if (draggingSlider < 0) return false;
        setSliderValue(wx, draggingSlider);
        return true;
    }

    @Override
    void stopDrag() { draggingSlider = -1; }

    // ── render ────────────────────────────────────────────────────────────────

    /**
     * Phase 4 — shapes.
     * Sliders use a greyscale palette; thumb brightens while dragging.
     */
    @Override
    void renderInteractiveShapes(ShapeRenderer shapes) {
        if (sliderY == null) return;
        float[] vals = {masterVolume, musicVolume, sfxVolume};
        for (int i = 0; i < vals.length; i++) {
            float fill = vals[i] / 100f * SLIDER_W;
            float ty   = sliderY[i];
            boolean dragging = (i == draggingSlider);

            // Track background — dark grey
            shapes.setColor(0.28f, 0.28f, 0.28f, 0.90f);
            shapes.rect(sliderX, ty, SLIDER_W, SLIDER_H);

            // Track fill — medium grey
            shapes.setColor(0.58f, 0.58f, 0.58f, 1f);
            shapes.rect(sliderX, ty, fill, SLIDER_H);

            // Thumb — light grey, brighter while dragging
            float g = dragging ? 1.0f : 0.82f;
            shapes.setColor(g, g, g, 1f);
            shapes.circle(sliderX + fill, ty + SLIDER_H / 2f, THUMB_R, 20);
        }

        fullscreenBtn.renderBackground(shapes);
        backBtn.renderBackground(shapes);
    }

    /**
     * Phase 5 — batch.
     * Labels, percentage values and button labels.
     */
    @Override
    void renderLabels(SpriteBatch batch, BitmapFont font) {
        if (sliderY == null) return;

        String[] labels = {"Master Volume", "Music Volume", "SFX Volume"};
        float[]  vals   = {masterVolume, musicVolume, sfxVolume};

        font.getData().setScale(2f);
        for (int i = 0; i < labels.length; i++) {
            float rowMidY = sliderY[i] + SLIDER_H / 2f + 14f;
            font.setColor(1f, 1f, 1f, 1f);
            font.draw(batch, labels[i], labelX, rowMidY);
            font.setColor(0.75f, 0.75f, 0.75f, 1f);
            font.draw(batch, Math.round(vals[i]) + "%", valueX, rowMidY);
        }

        // Fullscreen label — aligned with the button
        float toggleMidY = toggleRowY - (BTN_H - SLIDER_H) / 2f + BTN_H / 2f + 14f;
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "Fullscreen", labelX, toggleMidY);

        // Buttons
        font.getData().setScale(3f);
        fullscreenBtn.renderTexture(batch);
        fullscreenBtn.renderLabel(batch, font);
        backBtn.renderTexture(batch);
        backBtn.renderLabel(batch, font);

        font.getData().setScale(3f); // reset for caller
    }

    // ── actions ───────────────────────────────────────────────────────────────

    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        else            Gdx.graphics.setWindowedMode(1280, 720);
        rebuildLayout(panelX, panelY, panelW, panelH);
        saveSettings();
    }

    // ── slider helpers ────────────────────────────────────────────────────────

    private boolean onSliderTrack(float wx, float wy, int i) {
        float ty = sliderY[i];
        return wx >= sliderX - THUMB_R && wx <= sliderX + SLIDER_W + THUMB_R
            && wy >= ty - THUMB_R     && wy <= ty + SLIDER_H + THUMB_R;
    }

    private void setSliderValue(float wx, int i) {
        float v = Math.max(0f, Math.min(100f, (wx - sliderX) / SLIDER_W * 100f));
        switch (i) {
            case 0: masterVolume = v; break;
            case 1: musicVolume  = v; break;
            case 2: sfxVolume    = v; break;
        }
        saveSettings();
    }

    // ── preferences ──────────────────────────────────────────────────────────

    private void loadSettings() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        masterVolume = p.getFloat("master_volume", 100f);
        musicVolume  = p.getFloat("music_volume",   80f);
        sfxVolume    = p.getFloat("sfx_volume",     80f);
        fullscreen   = p.getBoolean("fullscreen",  false);
    }

    private void saveSettings() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        p.putFloat("master_volume", masterVolume);
        p.putFloat("music_volume",  musicVolume);
        p.putFloat("sfx_volume",    sfxVolume);
        p.putBoolean("fullscreen",  fullscreen);
        p.flush();
    }
}
