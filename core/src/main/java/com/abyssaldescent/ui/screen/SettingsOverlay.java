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
 * the interactive layer (sliders, labels, Back button).
 *
 * <h3>Layout</h3>
 * <ul>
 *   <li>Three volume sliders (Master / Music / SFX) with texture labels — centred
 *       horizontally and placed at ~58 % of screen height.</li>
 *   <li>Back button — bottom-left corner, 500×150 px, 40 px margin.</li>
 * </ul>
 *
 * <h3>Slider colours</h3>
 * Track and thumb are rendered in greyscale so they don't fight the background.
 */
class SettingsOverlay extends MenuOverlay {

    static final String PREFS = "abyssal_descent_settings";

    // ── sizing constants ──────────────────────────────────────────────────────
    // EDIT HERE: Adjust slider and label sizes
    private static final float SLIDER_W   = 700f;   // slider width
    private static final float SLIDER_H   = 18f;    // slider height
    private static final float THUMB_R    = 12f;    // slider thumb radius
    private static final float LABEL_TEX_W = 450f;  // label texture width
    private static final float LABEL_TEX_H = 100f;   // label texture height
    private static final float LABEL_GAP  = 50f;    // gap between label and slider
    private static final float VALUE_GAP  = 27f;    // gap between slider and value
    private static final float SLIDER_SPACING = 175f; // EDIT HERE: vertical spacing between sliders (40px)
    private static final float CENTER_Y    = 900f;  // EDIT HERE: center Y position for middle slider (1440x900)
    private static final float BTN_W      = 500f;   // back button width
    private static final float BTN_H      = 150f;   // back button height
    private static final float MARGIN     = 40f;    // margin from edges

    // ── state ─────────────────────────────────────────────────────────────────
    float   masterVolume, musicVolume, sfxVolume;

    // ── layout fields (rebuilt on resize) ────────────────────────────────────
    private float   labelX, sliderX, valueX;
    private float[] sliderY;            // Y baselines for 3 sliders
    private int     draggingSlider = -1;

    private Texture masterVolumeTex, musicVolumeTex, sfxVolumeTex;
    private Texture backOffTex, backOnTex;
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
        float totalW = LABEL_TEX_W + LABEL_GAP + SLIDER_W + VALUE_GAP + 60f /*value digits*/;
        labelX  = px + (pw - totalW) / 2f;
        sliderX = labelX + LABEL_TEX_W + LABEL_GAP;
        valueX  = sliderX + SLIDER_W + VALUE_GAP;

        // Vertical positioning: middle slider at CENTER_Y, others at ±SLIDER_SPACING
        sliderY = new float[]{
                CENTER_Y + SLIDER_SPACING,      // top slider
                CENTER_Y,                        // middle slider (center)
                CENTER_Y - SLIDER_SPACING       // bottom slider
        };

        // Back — bottom-left corner
        backBtn = new MenuButton(
                "Back",
                px + MARGIN,
                py + MARGIN,
                BTN_W, BTN_H,
                onClose);
        if (backOffTex == null) {
            backOffTex = ScreenAssets.loadTexture("ui/buttons/back_off.png");
            backOnTex  = ScreenAssets.loadTexture("ui/buttons/back_on.png");
        }
        if (backOffTex != null) backBtn.setTextures(backOffTex, backOnTex);
    }

    // ── input ─────────────────────────────────────────────────────────────────

    @Override
    void update(float mx, float my) {
        if (backBtn == null) return;
        backBtn.update(mx, my);
    }

    @Override
    boolean handleClick(float wx, float wy) {
        if (backBtn      == null)            return false;
        if (backBtn.handleClick(wx, wy))     return true;
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

        backBtn.renderBackground(shapes);
    }

    /**
     * Phase 5 — batch.
     * Labels, percentage values and button labels.
     */
    @Override
    void renderLabels(SpriteBatch batch, BitmapFont font) {
        if (sliderY == null) return;

        Texture[] labelTextures = {masterVolumeTex, musicVolumeTex, sfxVolumeTex};
        float[]  vals   = {masterVolume, musicVolume, sfxVolume};

        font.getData().setScale(2f);
        for (int i = 0; i < labelTextures.length; i++) {
            float sliderBaseY = sliderY[i];
            // Center label texture vertically on slider
            float labelY = sliderBaseY - LABEL_TEX_H / 2f;
            if (labelTextures[i] != null) {
                batch.draw(labelTextures[i], labelX, labelY, LABEL_TEX_W, LABEL_TEX_H);
            }
            // Center percentage text vertically on slider
            font.setColor(0.75f, 0.75f, 0.75f, 1f);
            font.draw(batch, Math.round(vals[i]) + "%", valueX, sliderBaseY + SLIDER_H / 2f + 14f);
        }

        // Buttons
        font.getData().setScale(3f);
        backBtn.renderTexture(batch);
        backBtn.renderLabel(batch, font);

        font.getData().setScale(3f); // reset for caller
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
        loadLabelTextures();
    }

    private void saveSettings() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        p.putFloat("master_volume", masterVolume);
        p.putFloat("music_volume",  musicVolume);
        p.putFloat("sfx_volume",    sfxVolume);
        p.flush();
    }

    private void loadLabelTextures() {
        masterVolumeTex = ScreenAssets.loadTexture("ui/buttons/master_volume.png");
        musicVolumeTex  = ScreenAssets.loadTexture("ui/buttons/music_volume.png");
        sfxVolumeTex    = ScreenAssets.loadTexture("ui/buttons/sfx.png");
    }

    void dispose() {
        if (masterVolumeTex != null) masterVolumeTex.dispose();
        if (musicVolumeTex != null) musicVolumeTex.dispose();
        if (sfxVolumeTex != null) sfxVolumeTex.dispose();
        if (backOffTex != null) backOffTex.dispose();
        if (backOnTex != null) backOnTex.dispose();
    }
}
