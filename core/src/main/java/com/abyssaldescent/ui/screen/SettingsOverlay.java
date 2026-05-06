package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Settings panel overlay.
 * State (volumes, fullscreen, fps) persists across layout rebuilds via {@link Preferences}.
 */
class SettingsOverlay extends MenuOverlay {

    static final  String PREFS       = "abyssal_descent_settings";
    private static final int[]  FPS_OPT     = {30, 60, 120};
    private static final float  SLIDER_W    = 460f;
    private static final float  SLIDER_H    = 12f;
    private static final float  THUMB_R     = 12f;
    private static final float  ROW_H       = 80f;

    float masterVolume, musicVolume, sfxVolume;
    boolean fullscreen;
    int fpsCapIndex;

    private float   labelX, sliderX;
    private float[] sliderY;
    private float   toggleLabelY, fpsLabelY;
    private int     draggingSlider = -1;

    private MenuButton fullscreenBtn, fpsCycleBtn, backBtn;

    SettingsOverlay(Texture panelTex, Runnable onClose) {
        super(panelTex, onClose);
        loadSettings();
    }

    // ── layout ────────────────────────────────────────────────────────────────

    @Override
    void rebuildLayout(float px, float py, float pw, float ph) {
        panelX = px; panelY = py; panelW = pw; panelH = ph;

        float totalBlockW = 230f + 40f + SLIDER_W;
        labelX  = px + (pw - totalBlockW) / 2f;
        sliderX = labelX + 270f;

        float contentTop = py + ph * 0.72f;
        sliderY = new float[]{contentTop, contentTop - ROW_H, contentTop - ROW_H * 2};

        float toggleY = contentTop - ROW_H * 3;
        float fpsY    = contentTop - ROW_H * 4;
        toggleLabelY  = toggleY + 14f;
        fpsLabelY     = fpsY    + 14f;

        fullscreenBtn = new MenuButton(
                fullscreen ? "ON" : "OFF",
                sliderX, toggleY - 8f, 100f, 50f,
                this::toggleFullscreen);

        fpsCycleBtn = new MenuButton(
                String.valueOf(FPS_OPT[fpsCapIndex]),
                sliderX, fpsY - 8f, 100f, 50f,
                this::cycleFps);

        backBtn = new MenuButton(
                "Back",
                px + 30f, py + 30f, 150f, 50f,
                onClose);
    }

    // ── input ─────────────────────────────────────────────────────────────────

    @Override
    void update(float mx, float my) {
        if (backBtn == null) return;
        fullscreenBtn.update(mx, my);
        fpsCycleBtn.update(mx, my);
        backBtn.update(mx, my);
    }

    @Override
    boolean handleClick(float wx, float wy) {
        if (backBtn.handleClick(wx, wy))       return true;
        if (fullscreenBtn.handleClick(wx, wy)) return true;
        if (fpsCycleBtn.handleClick(wx, wy))   return true;
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

    @Override
    void renderInteractiveShapes(ShapeRenderer shapes) {
        if (sliderY == null) return;
        float[] vals = {masterVolume, musicVolume, sfxVolume};
        for (int i = 0; i < vals.length; i++) {
            float fill = vals[i] / 100f * SLIDER_W;
            float ty   = sliderY[i];
            shapes.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            shapes.rect(sliderX, ty, SLIDER_W, SLIDER_H);
            shapes.setColor(0.4f, 0.3f, 0.8f, 1f);
            shapes.rect(sliderX, ty, fill, SLIDER_H);
            boolean drag = (i == draggingSlider);
            shapes.setColor(drag ? 1f : 0.9f, drag ? 1f : 0.9f, drag ? 0f : 0.9f, 1f);
            shapes.circle(sliderX + fill, ty + SLIDER_H / 2f, THUMB_R, 16);
        }
        fullscreenBtn.renderBackground(shapes);
        fpsCycleBtn.renderBackground(shapes);
        backBtn.renderBackground(shapes);
    }

    @Override
    void renderLabels(SpriteBatch batch, BitmapFont font) {
        if (sliderY == null) return;
        String[] labels = {"Master Volume", "Music Volume", "SFX Volume"};
        float[]  vals   = {masterVolume, musicVolume, sfxVolume};

        font.getData().setScale(1.8f);
        for (int i = 0; i < labels.length; i++) {
            float rowY = sliderY[i] + SLIDER_H / 2f + 12f;
            font.setColor(1f, 1f, 1f, 1f);
            font.draw(batch, labels[i], labelX, rowY);
            font.setColor(0.8f, 0.8f, 0.8f, 1f);
            font.draw(batch, Math.round(vals[i]) + "%", sliderX + SLIDER_W + 18f, rowY);
        }
        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, "Fullscreen", labelX, toggleLabelY);
        font.draw(batch, "FPS Cap",    labelX, fpsLabelY);

        font.getData().setScale(2f);
        fullscreenBtn.renderLabel(batch, font);
        fpsCycleBtn.renderLabel(batch, font);
        backBtn.renderLabel(batch, font);

        if (panelTex == null) {
            font.getData().setScale(3f);
            font.setColor(0.85f, 0.75f, 1f, 1f);
            String title = "SETTINGS";
            GlyphLayout gl = new GlyphLayout(font, title);
            font.draw(batch, title, panelX + (panelW - gl.width) / 2f, panelY + panelH * 0.93f);
        }
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

    private void cycleFps() {
        fpsCapIndex = (fpsCapIndex + 1) % FPS_OPT.length;
        Gdx.graphics.setForegroundFPS(FPS_OPT[fpsCapIndex]);
        rebuildLayout(panelX, panelY, panelW, panelH);
        saveSettings();
    }

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
        fpsCapIndex  = fpsIndexOf(p.getInteger("fps_cap", 60));
    }

    private void saveSettings() {
        Preferences p = Gdx.app.getPreferences(PREFS);
        p.putFloat("master_volume", masterVolume);
        p.putFloat("music_volume",  musicVolume);
        p.putFloat("sfx_volume",    sfxVolume);
        p.putBoolean("fullscreen",  fullscreen);
        p.putInteger("fps_cap",     FPS_OPT[fpsCapIndex]);
        p.flush();
    }

    private int fpsIndexOf(int fps) {
        for (int i = 0; i < FPS_OPT.length; i++) if (FPS_OPT[i] == fps) return i;
        return 1;
    }
}
