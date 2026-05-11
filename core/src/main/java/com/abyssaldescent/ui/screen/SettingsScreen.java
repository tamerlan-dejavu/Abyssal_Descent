package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Settings screen (GDD FR11 §2.2).
 *
 * <p>Controls:
 * <ul>
 *   <li>Master Volume  — slider 0–100 %</li>
 *   <li>Music Volume   — slider 0–100 %</li>
 *   <li>SFX Volume     — slider 0–100 %</li>
 *   <li>Fullscreen     — toggle button</li>
 *   <li>FPS Cap        — cycle through 30 / 60 / 120</li>
 * </ul>
 * All values are auto-saved to {@code Preferences} on every change.
 */
public class SettingsScreen implements Screen {

    private static final String PREFS_NAME  = "abyssal_descent_settings";
    private static final float  SLIDER_W    = 500f;
    private static final float  SLIDER_H    = 12f;
    private static final float  THUMB_R     = 12f;
    private static final float  ROW_H       = 70f;
    private static final float  LABEL_W     = 240f;
    private static final int[]  FPS_OPTIONS = {30, 60, 120};

    private OrthographicCamera camera;
    private SpriteBatch        batch;
    private ShapeRenderer      shapes;
    private Texture            background;
    private BitmapFont         font;
    private BitmapFont         titleFont;

    private float   masterVolume;
    private float   musicVolume;
    private float   sfxVolume;
    private boolean fullscreen;
    private int     fpsCapIndex;

    private MenuButton backBtn;
    private MenuButton fullscreenBtn;
    private MenuButton fpsCycleBtn;

    /** Index of the slider currently being dragged (-1 = none). */
    private int draggingSlider = -1;

    // Slider layout — rebuilt in buildLayout()
    private float   sliderX;
    private float[] sliderY;
    private float   labelX;
    private float   toggleLabelY1;
    private float   toggleLabelY2;

    @Override
    public void show() {
        camera    = new OrthographicCamera();
        batch     = new SpriteBatch();
        shapes    = new ShapeRenderer();
        font      = new BitmapFont();
        font.getData().setScale(2f);
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        background = ScreenAssets.loadBackground("ui/backgrounds/settings_bg.png",
                0x04, 0x08, 0x06);

        loadSettings();
        buildLayout();

        // InputAdapter reads all positions from fields — safe after resize()
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float wy = Gdx.graphics.getHeight() - screenY;
                if (backBtn.handleClick(screenX, wy))       return true;
                if (fullscreenBtn.handleClick(screenX, wy)) return true;
                if (fpsCycleBtn.handleClick(screenX, wy))   return true;
                for (int i = 0; i < sliderY.length; i++) {
                    if (isOnSliderTrack(screenX, wy, i)) {
                        draggingSlider = i;
                        updateSliderValue(screenX, i);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (draggingSlider >= 0) {
                    updateSliderValue(screenX, draggingSlider);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                draggingSlider = -1;
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();
        backBtn.update(mx, my);
        fullscreenBtn.update(mx, my);
        fpsCycleBtn.update(mx, my);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawSliders();
        backBtn.renderBackground(shapes);
        fullscreenBtn.renderBackground(shapes);
        fpsCycleBtn.renderBackground(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        drawTitle(sw, sh);
        drawRowLabels();
        backBtn.renderLabel(batch, font);
        fullscreenBtn.renderLabel(batch, font);
        fpsCycleBtn.renderLabel(batch, font);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        buildLayout();
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        titleFont.dispose();
        background.dispose();
    }

    // ── layout ────────────────────────────────────────────────────────────────

    private void buildLayout() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        camera.setToOrtho(false, sw, sh);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        float contentStartY = sh * 0.65f;
        float totalBlockW   = LABEL_W + 40f + SLIDER_W;
        labelX  = (sw - totalBlockW) / 2f;
        sliderX = labelX + LABEL_W + 40f;

        sliderY = new float[]{
            contentStartY,
            contentStartY - ROW_H,
            contentStartY - 2 * ROW_H
        };

        float toggleRowY = contentStartY - 3 * ROW_H;
        float fpsRowY    = contentStartY - 4 * ROW_H;

        toggleLabelY1 = toggleRowY + 14f;
        toggleLabelY2 = fpsRowY    + 14f;

        fullscreenBtn = new MenuButton(
                fullscreen ? "ON" : "OFF",
                sliderX, toggleRowY - 8f, 100f, 50f,
                this::toggleFullscreen);

        fpsCycleBtn = new MenuButton(
                String.valueOf(FPS_OPTIONS[fpsCapIndex]),
                sliderX, fpsRowY - 8f, 100f, 50f,
                this::cycleFps);

        backBtn = new MenuButton(
                "Back",
                30f, 30f, 150f, 50f,
                () -> UiManager.getInstance().showMainMenu());
    }

    // ── rendering ─────────────────────────────────────────────────────────────

    private void drawTitle(int sw, int sh) {
        String text = "SETTINGS";
        GlyphLayout layout = new GlyphLayout(titleFont, text);
        titleFont.setColor(0.85f, 0.75f, 1f, 1f);
        titleFont.draw(batch, text, (sw - layout.width) / 2f, sh * 0.88f);
    }

    private void drawRowLabels() {
        String[] sliderLabels = {"Master Volume", "Music Volume", "SFX Volume"};
        float[]  values       = {masterVolume, musicVolume, sfxVolume};

        font.setColor(1f, 1f, 1f, 1f);
        for (int i = 0; i < sliderLabels.length; i++) {
            float rowMidY = sliderY[i] + SLIDER_H / 2f + 10f;
            font.draw(batch, sliderLabels[i], labelX, rowMidY);
            font.setColor(0.8f, 0.8f, 0.8f, 1f);
            font.draw(batch, Math.round(values[i]) + "%", sliderX + SLIDER_W + 20f, rowMidY);
            font.setColor(1f, 1f, 1f, 1f);
        }

        font.draw(batch, "Fullscreen", labelX, toggleLabelY1);
        font.draw(batch, "FPS Cap",    labelX, toggleLabelY2);
    }

    private void drawSliders() {
        float[] values = {masterVolume, musicVolume, sfxVolume};
        for (int i = 0; i < values.length; i++) {
            float ty   = sliderY[i];
            float fill = (values[i] / 100f) * SLIDER_W;

            // Track background
            shapes.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            shapes.rect(sliderX, ty, SLIDER_W, SLIDER_H);

            // Filled portion
            shapes.setColor(0.4f, 0.3f, 0.8f, 1f);
            shapes.rect(sliderX, ty, fill, SLIDER_H);

            // Thumb
            boolean active = (i == draggingSlider);
            shapes.setColor(active ? 1f : 0.85f, active ? 1f : 0.85f, active ? 0f : 0.85f, 1f);
            shapes.circle(sliderX + fill, ty + SLIDER_H / 2f, THUMB_R, 16);
        }
    }

    // ── slider interaction ────────────────────────────────────────────────────

    private boolean isOnSliderTrack(float wx, float wy, int index) {
        float ty = sliderY[index];
        return wx >= sliderX - THUMB_R
            && wx <= sliderX + SLIDER_W + THUMB_R
            && wy >= ty - THUMB_R
            && wy <= ty + SLIDER_H + THUMB_R;
    }

    private void updateSliderValue(float wx, int index) {
        float clamped = Math.max(0f, Math.min(100f, (wx - sliderX) / SLIDER_W * 100f));
        switch (index) {
            case 0: masterVolume = clamped; break;
            case 1: musicVolume  = clamped; break;
            case 2: sfxVolume    = clamped; break;
        }
        saveSettings();
    }

    // ── toggle / cycle actions ────────────────────────────────────────────────

    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
        buildLayout(); // rebuilds button label and positions after mode change
        saveSettings();
    }

    private void cycleFps() {
        fpsCapIndex = (fpsCapIndex + 1) % FPS_OPTIONS.length;
        Gdx.graphics.setForegroundFPS(FPS_OPTIONS[fpsCapIndex]);
        buildLayout(); // updates button label
        saveSettings();
    }

    // ── Preferences ──────────────────────────────────────────────────────────

    private void loadSettings() {
        Preferences p    = Gdx.app.getPreferences(PREFS_NAME);
        masterVolume     = p.getFloat("master_volume", 100f);
        musicVolume      = p.getFloat("music_volume",   80f);
        sfxVolume        = p.getFloat("sfx_volume",     80f);
        fullscreen       = p.getBoolean("fullscreen",  false);
        fpsCapIndex      = fpsIndexOf(p.getInteger("fps_cap", 60));
    }

    private void saveSettings() {
        Preferences p = Gdx.app.getPreferences(PREFS_NAME);
        p.putFloat("master_volume", masterVolume);
        p.putFloat("music_volume",  musicVolume);
        p.putFloat("sfx_volume",    sfxVolume);
        p.putBoolean("fullscreen",  fullscreen);
        p.putInteger("fps_cap",     FPS_OPTIONS[fpsCapIndex]);
        p.flush();
    }

    private int fpsIndexOf(int fps) {
        for (int i = 0; i < FPS_OPTIONS.length; i++) {
            if (FPS_OPTIONS[i] == fps) return i;
        }
        return 1;
    }
}
