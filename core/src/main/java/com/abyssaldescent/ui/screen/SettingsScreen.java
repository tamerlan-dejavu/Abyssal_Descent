package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Preferences;

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

    private static final String PREFS_NAME   = "abyssal_descent_settings";
    private static final float  SLIDER_W     = 500f;
    private static final float  SLIDER_H     = 12f;
    private static final float  THUMB_R      = 12f;
    private static final float  ROW_H        = 70f;
    private static final float  LABEL_W      = 240f;
    private static final int[]  FPS_OPTIONS  = {30, 60, 120};

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

    // Slider track left-edge X, calculated in buildLayout()
    private float sliderX;
    // Y positions for the 3 slider rows (world-space, Y-up)
    private float[] sliderY;

    @Override
    public void show() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, sw, sh);

        batch  = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);

        shapes = new ShapeRenderer();
        shapes.setProjectionMatrix(camera.combined);

        font = new BitmapFont();
        font.getData().setScale(2f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        background = ScreenAssets.loadBackground("ui/backgrounds/settings_bg.png",
                0x04, 0x08, 0x06);

        loadSettings();
        buildLayout(sw, sh);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float wy = sh - screenY;
                if (backBtn.handleClick(screenX, wy))       return true;
                if (fullscreenBtn.handleClick(screenX, wy)) return true;
                if (fpsCycleBtn.handleClick(screenX, wy))   return true;
                // Check if touch starts on a slider thumb or track
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
        drawLabels(sw, sh);
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
        buildLayout(width, height);
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

    private void buildLayout(int sw, int sh) {
        // Content block starts at 65 % screen height, rows descend
        float contentStartY = sh * 0.65f;
        sliderX = (sw - LABEL_W - 40f - SLIDER_W) / 2f + LABEL_W + 40f;
        sliderY = new float[]{
            contentStartY,
            contentStartY - ROW_H,
            contentStartY - 2 * ROW_H
        };

        float toggleRowY  = contentStartY - 3 * ROW_H;
        float fpsRowY     = contentStartY - 4 * ROW_H;
        float toggleX     = sliderX;

        fullscreenBtn = new MenuButton(
                fullscreen ? "ON" : "OFF",
                toggleX, toggleRowY - 10f, 100f, 50f,
                this::toggleFullscreen);

        fpsCycleBtn = new MenuButton(
                String.valueOf(FPS_OPTIONS[fpsCapIndex]),
                toggleX, fpsRowY - 10f, 100f, 50f,
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

    private void drawLabels(int sw, int sh) {
        String[] sliderLabels   = {"Master Volume", "Music Volume", "SFX Volume"};
        float[]  sliderValues   = {masterVolume, musicVolume, sfxVolume};
        String[] toggleLabels   = {"Fullscreen", "FPS Cap"};
        float    contentStartY  = sh * 0.65f;

        font.setColor(1f, 1f, 1f, 1f);
        float labelX = (sw - LABEL_W - 40f - SLIDER_W) / 2f;

        for (int i = 0; i < sliderLabels.length; i++) {
            float y = sliderY[i] + SLIDER_H / 2f + 10f;
            font.draw(batch, sliderLabels[i], labelX, y);
            // value percentage next to thumb
            String pct = Math.round(sliderValues[i]) + "%";
            font.setColor(0.8f, 0.8f, 0.8f, 1f);
            font.draw(batch, pct, sliderX + SLIDER_W + 20f, y);
            font.setColor(1f, 1f, 1f, 1f);
        }

        // Toggle row labels
        float labelY1 = contentStartY - 3 * ROW_H + 12f;
        float labelY2 = contentStartY - 4 * ROW_H + 12f;
        float labelX2 = (sw - LABEL_W - 40f - SLIDER_W) / 2f;
        font.draw(batch, "Fullscreen",  labelX2, labelY1);
        font.draw(batch, "FPS Cap",     labelX2, labelY2);
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
            float thumbX = sliderX + fill;
            float thumbY = ty + SLIDER_H / 2f;
            shapes.setColor(i == draggingSlider ? 1f : 0.85f,
                            i == draggingSlider ? 1f : 0.85f,
                            i == draggingSlider ? 0f : 0.85f,
                            1f);
            shapes.circle(thumbX, thumbY, THUMB_R, 16);
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
        float raw = (wx - sliderX) / SLIDER_W * 100f;
        float clamped = Math.max(0f, Math.min(100f, raw));
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
        fullscreenBtn = rebuildToggle(fullscreenBtn, fullscreen ? "ON" : "OFF",
                this::toggleFullscreen);
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
        saveSettings();
    }

    private void cycleFps() {
        fpsCapIndex = (fpsCapIndex + 1) % FPS_OPTIONS.length;
        fpsCycleBtn = rebuildToggle(fpsCycleBtn,
                String.valueOf(FPS_OPTIONS[fpsCapIndex]), this::cycleFps);
        Gdx.graphics.setForegroundFPS(FPS_OPTIONS[fpsCapIndex]);
        saveSettings();
    }

    /** Returns a new MenuButton with the same position/size but an updated label. */
    private MenuButton rebuildToggle(MenuButton old, String newLabel, Runnable action) {
        return new MenuButton(newLabel,
                old.getX(), old.getY(), old.getWidth(), old.getHeight(), action);
    }

    // ── Preferences ──────────────────────────────────────────────────────────

    private void loadSettings() {
        Preferences p = Gdx.app.getPreferences(PREFS_NAME);
        masterVolume = p.getFloat("master_volume", 100f);
        musicVolume  = p.getFloat("music_volume",   80f);
        sfxVolume    = p.getFloat("sfx_volume",     80f);
        fullscreen   = p.getBoolean("fullscreen",  false);
        int savedFps = p.getInteger("fps_cap",       60);
        fpsCapIndex  = fpsIndexOf(savedFps);
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
        return 1; // default to 60
    }
}
