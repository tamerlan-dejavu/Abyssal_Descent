package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

import java.util.Arrays;
import java.util.List;

/**
 * Main menu screen — first screen shown on launch (GDD FR11).
 *
 * <h3>Asset slots</h3>
 * <pre>
 *   ui/backgrounds/main_menu_bg.png      — full-screen background
 *
 *   ui/buttons/newgame_off.png           — New Game idle    (500×150 px)
 *   ui/buttons/newgame_on.png            — New Game hovered
 *   ui/buttons/continue_off.png          — Continue idle
 *   ui/buttons/continue_on.png           — Continue hovered
 *   ui/buttons/setting_off.png           — Settings idle
 *   ui/buttons/setting_on.png            — Settings hovered
 *   ui/buttons/exit_off.png              — Exit idle
 *   ui/buttons/exit_on.png               — Exit hovered
 *
 *   ui/credits/credits.png               — project-info card (500×300, bottom-left)
 *
 *   ui/overlays/settings_panel.png       — settings overlay panel  (1800×900)
 *   ui/overlays/difficulty_panel.png     — difficulty overlay panel (1800×900)
 *   ui/overlays/easy_off.png  / easy_on.png
 *   ui/overlays/normal_off.png / normal_on.png
 *   ui/overlays/hard_off.png  / hard_on.png
 * </pre>
 */
public class MainMenuScreen implements Screen {

    // ── constants ─────────────────────────────────────────────────────────────

    private static final float BTN_W          = 500f;
    private static final float BTN_H          = 150f;
    private static final float BTN_GAP        = 20f;
    private static final float CREDITS_W      = 700f;
    private static final float CREDITS_H      = 500f;
    private static final float CREDITS_MARGIN = 20f;
    private static final float PANEL_W        = 1800f;
    private static final float PANEL_H        = 900f;

    private enum Overlay { NONE, SETTINGS, DIFFICULTY }

    // ── fields ────────────────────────────────────────────────────────────────

    private OrthographicCamera camera;
    private SpriteBatch        batch;
    private ShapeRenderer      shapes;
    private BitmapFont         font;

    // textures — main menu
    private Texture background;
    private Texture newGameOff, newGameOn;
    private Texture continueOff, continueOn;
    private Texture settingOff, settingOn;
    private Texture exitOff, exitOn;
    private Texture creditsTex;

    // music
    private Music bgMusic;

    // textures — settings overlay
    private Texture settingsBgTex;    // full-screen background (ui/backgrounds/settings.png)
    private Texture settingsPanelTex; // optional centred panel (unused if settingsBgTex present)

    // textures — difficulty overlay (full-screen)
    private Texture difficultyBgTex;           // full-screen background
    private Texture easyOff,   easyOn;         // Easy   idle / glow
    private Texture normalOff, normalOn;       // Normal idle / glow
    private Texture hardOff,   hardOn;         // Hard   idle / glow
    private Texture diffBackOff, diffBackOn;   // Back   idle / glow

    // main menu buttons
    private List<MenuButton> buttons;

    // overlay system
    private Overlay         activeOverlay = Overlay.NONE;
    private SettingsOverlay settingsOverlay;
    private DifficultyOverlay difficultyOverlay;

    // blur FBO
    private FrameBuffer   blurFbo;
    private TextureRegion blurRegion;

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        camera = new OrthographicCamera();
        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();
        font   = new BitmapFont();
        font.getData().setScale(3f);

        loadAllTextures();
        startMusic();

        settingsOverlay   = new SettingsOverlay(settingsPanelTex, this::closeOverlay);
        difficultyOverlay = new DifficultyOverlay(difficultyBgTex,
                easyOff,    easyOn,
                normalOff,  normalOn,
                hardOff,    hardOn,
                diffBackOff, diffBackOn,
                this::closeOverlay);

        buildLayout();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float wy = Gdx.graphics.getHeight() - screenY;
                if (activeOverlay == Overlay.SETTINGS)
                    return settingsOverlay.handleClick(screenX, wy);
                if (activeOverlay == Overlay.DIFFICULTY)
                    return difficultyOverlay.handleClick(screenX, wy);
                for (MenuButton btn : buttons) {
                    if (btn.handleClick(screenX, wy)) return true;
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (activeOverlay == Overlay.SETTINGS)
                    return settingsOverlay.handleDrag(screenX);
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                settingsOverlay.stopDrag();
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

        if (activeOverlay == Overlay.NONE) {
            renderMainMenu(sw, sh, mx, my);
        } else {
            renderOverlay(sw, sh, mx, my);
        }
    }

    @Override
    public void resize(int width, int height) {
        buildLayout();
        if (blurFbo != null) {
            blurFbo.dispose();
            blurFbo = null;
        }
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        if (bgMusic != null) bgMusic.pause();
    }

    @Override
    public void dispose() {
        if (bgMusic != null) { bgMusic.stop(); bgMusic.dispose(); }
        batch.dispose();
        shapes.dispose();
        font.dispose();
        disposeTextures();
        if (blurFbo != null) blurFbo.dispose();
    }

    // ── main-menu render ──────────────────────────────────────────────────────

    private void renderMainMenu(int sw, int sh, float mx, float my) {
        for (MenuButton btn : buttons) btn.update(mx, my);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        // Button shapes (fallback bg + hover border)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (MenuButton btn : buttons) btn.renderBackground(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Textures + labels + credits
        batch.begin();
        for (MenuButton btn : buttons) btn.renderTexture(batch);
        font.getData().setScale(3f);
        for (MenuButton btn : buttons) btn.renderLabel(batch, font);
        if (creditsTex != null) {
            batch.draw(creditsTex, CREDITS_MARGIN, CREDITS_MARGIN, CREDITS_W, CREDITS_H);
        }
        batch.end();
    }

    // ── overlay render ────────────────────────────────────────────────────────

    private void renderOverlay(int sw, int sh, float mx, float my) {
        if (activeOverlay == Overlay.SETTINGS) {
            renderSettingsOverlay(sw, sh, mx, my);
        } else {
            renderDifficultyOverlay(sw, sh, mx, my);
        }
    }

    /**
     * Settings overlay: custom background (settings.png) or blurred fallback → sliders → labels.
     */
    private void renderSettingsOverlay(int sw, int sh, float mx, float my) {
        settingsOverlay.update(mx, my);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (settingsBgTex != null) {
            // Custom settings background — draw full-screen, no blur needed
            batch.begin();
            batch.draw(settingsBgTex, 0, 0, sw, sh);
            batch.end();
        } else {
            // Fallback: blurred main menu + dark dim
            if (blurRegion == null) captureBlur(sw, sh);
            batch.begin();
            batch.draw(blurRegion, 0, 0, sw, sh);
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.60f);
            shapes.rect(0, 0, sw, sh);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        settingsOverlay.renderInteractiveShapes(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.getData().setScale(3f);
        settingsOverlay.renderLabels(batch, font);
        batch.end();
    }

    /**
     * Difficulty overlay: full-screen background → buttons.
     * No blur — the menu logo above remains visible.
     */
    private void renderDifficultyOverlay(int sw, int sh, float mx, float my) {
        difficultyOverlay.update(mx, my);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Full-screen background: custom texture → fallback to dark main-menu bg
        batch.begin();
        if (difficultyBgTex != null) {
            batch.draw(difficultyBgTex, 0, 0, sw, sh);
        } else {
            batch.draw(background, 0, 0, sw, sh);
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0.08f, 0.82f);
            shapes.rect(0, 0, sw, sh);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
        }
        batch.end();

        // Button hover shapes (borders)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        difficultyOverlay.renderInteractiveShapes(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Button textures + labels
        batch.begin();
        font.getData().setScale(3f);
        difficultyOverlay.renderLabels(batch, font);
        batch.end();
    }

    // ── blur capture ──────────────────────────────────────────────────────────

    /**
     * Renders the background to a 1/8-resolution FrameBuffer.
     * When drawn back at full screen size the linear filter produces a smooth blur.
     */
    private void captureBlur(int sw, int sh) {
        int fbW = Math.max(1, sw / 8);
        int fbH = Math.max(1, sh / 8);

        if (blurFbo == null) {
            try {
                blurFbo = new FrameBuffer(Pixmap.Format.RGB888, fbW, fbH, false);
                blurFbo.getColorBufferTexture().setFilter(
                        Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            } catch (Exception e) {
                Gdx.app.error("MainMenuScreen", "FrameBuffer creation failed", e);
                blurRegion = new TextureRegion(background);
                return;
            }
        }

        Matrix4 savedProj = new Matrix4(batch.getProjectionMatrix());

        blurFbo.begin();
        Gdx.gl.glViewport(0, 0, fbW, fbH);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, fbW, fbH));
        batch.begin();
        batch.draw(background, 0, 0, fbW, fbH);
        batch.end();
        blurFbo.end();

        Gdx.gl.glViewport(0, 0, sw, sh);
        batch.setProjectionMatrix(savedProj);

        blurRegion = new TextureRegion(blurFbo.getColorBufferTexture());
        blurRegion.flip(false, true); // FBO Y is inverted in libGDX
    }

    // ── overlay management ────────────────────────────────────────────────────

    private void openOverlay(Overlay ov) {
        activeOverlay = ov;
        blurRegion = null; // force recapture each time overlay opens
    }

    private void closeOverlay() {
        activeOverlay = Overlay.NONE;
        blurRegion    = null;
    }

    // ── layout ────────────────────────────────────────────────────────────────

    private void buildLayout() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        camera.setToOrtho(false, sw, sh);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // Main menu buttons — stack centred at 40 % screen height
        float totalH = 4 * BTN_H + 3 * BTN_GAP;
        float startY = sh * 0.40f - totalH / 2f;
        float bx     = (sw - BTN_W) / 2f;

        MenuButton exitBtn = new MenuButton(
                "Exit", bx, startY, BTN_W, BTN_H,
                () -> Gdx.app.exit());
        if (exitOff != null) exitBtn.setTextures(exitOff, exitOn);

        MenuButton settingsBtn = new MenuButton(
                "Settings", bx, startY + (BTN_H + BTN_GAP), BTN_W, BTN_H,
                () -> openOverlay(Overlay.SETTINGS));
        if (settingOff != null) settingsBtn.setTextures(settingOff, settingOn);

        MenuButton continueBtn = new MenuButton(
                "Continue", bx, startY + 2 * (BTN_H + BTN_GAP), BTN_W, BTN_H,
                () -> UiManager.getInstance().continueGame());
        if (continueOff != null) continueBtn.setTextures(continueOff, continueOn);

        if (!hasSaveGame()) {
            continueBtn.setEnabled(false);
            continueBtn.setAlpha(0.5f);
        }

        MenuButton newGameBtn = new MenuButton(
                "New Game", bx, startY + 3 * (BTN_H + BTN_GAP), BTN_W, BTN_H,
                () -> openOverlay(Overlay.DIFFICULTY));
        if (newGameOff != null) newGameBtn.setTextures(newGameOff, newGameOn);

        buttons = Arrays.asList(newGameBtn, continueBtn, settingsBtn, exitBtn);

        // Both overlays are full-screen; each positions its own content internally
        settingsOverlay.rebuildLayout(0, 0, sw, sh);
        difficultyOverlay.rebuildLayout(0, 0, sw, sh);
    }

    // ── asset loading ─────────────────────────────────────────────────────────

    private void loadAllTextures() {
        background = ScreenAssets.loadBackground(
                "ui/backgrounds/main_menu_bg.png", 0x08, 0x04, 0x14);

        newGameOff   = ScreenAssets.loadTexture("ui/buttons/newgame_off.png");
        newGameOn    = ScreenAssets.loadTexture("ui/buttons/newgame_on.png");
        continueOff  = ScreenAssets.loadTexture("ui/buttons/continue_off.png");
        continueOn   = ScreenAssets.loadTexture("ui/buttons/continue_on.png");
        settingOff   = ScreenAssets.loadTexture("ui/buttons/setting_off.png");
        settingOn    = ScreenAssets.loadTexture("ui/buttons/setting_on.png");
        exitOff      = ScreenAssets.loadTexture("ui/buttons/exit_off.png");
        exitOn       = ScreenAssets.loadTexture("ui/buttons/exit_on.png");
        creditsTex   = ScreenAssets.loadTexture("ui/credits/credits.png");

        settingsBgTex    = firstOf("ui/backgrounds/settings.png",
                                   "ui/overlays/settings_bg.png");
        settingsPanelTex = ScreenAssets.loadTexture("ui/overlays/settings_panel.png");

        // Difficulty: full-screen background — check all known locations
        difficultyBgTex = firstOf("ui/backgrounds/difficulty.png",
                                  "ui/overlays/difficulty_bg.png",
                                  "ui/overlays/difficulty_panel.png");

        // Difficulty buttons 500×150 — _idle/_glow preferred, _off/_on as fallback
        easyOff    = firstOf("ui/overlays/easy_idle.png",   "ui/overlays/easy_off.png");
        easyOn     = firstOf("ui/overlays/easy_glow.png",   "ui/overlays/easy_on.png");
        normalOff  = firstOf("ui/overlays/normal_idle.png", "ui/overlays/normal_off.png");
        normalOn   = firstOf("ui/overlays/normal_glow.png", "ui/overlays/normal_on.png");
        hardOff    = firstOf("ui/overlays/hard_idle.png",   "ui/overlays/hard_off.png");
        hardOn     = firstOf("ui/overlays/hard_glow.png",   "ui/overlays/hard_on.png");

        // Back button 500×150 — bottom-right corner of difficulty screen
        diffBackOff = firstOf("ui/overlays/back_idle.png", "ui/overlays/back_off.png");
        diffBackOn  = firstOf("ui/overlays/back_glow.png", "ui/overlays/back_on.png");
    }

    private void disposeTextures() {
        if (background      != null) background.dispose();
        if (newGameOff      != null) newGameOff.dispose();
        if (newGameOn       != null) newGameOn.dispose();
        if (continueOff     != null) continueOff.dispose();
        if (continueOn      != null) continueOn.dispose();
        if (settingOff      != null) settingOff.dispose();
        if (settingOn       != null) settingOn.dispose();
        if (exitOff         != null) exitOff.dispose();
        if (exitOn          != null) exitOn.dispose();
        if (creditsTex      != null) creditsTex.dispose();
        if (settingsBgTex    != null) settingsBgTex.dispose();
        if (settingsPanelTex != null) settingsPanelTex.dispose();
        if (difficultyBgTex  != null) difficultyBgTex.dispose();
        if (easyOff      != null) easyOff.dispose();
        if (easyOn       != null) easyOn.dispose();
        if (normalOff    != null) normalOff.dispose();
        if (normalOn     != null) normalOn.dispose();
        if (hardOff      != null) hardOff.dispose();
        if (hardOn       != null) hardOn.dispose();
        if (diffBackOff  != null) diffBackOff.dispose();
        if (diffBackOn   != null) diffBackOn.dispose();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void startMusic() {
        String path = "ui/sounds/d093f364c701d48.mp3";
        if (!Gdx.files.internal(path).exists()) return;
        try {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.6f);
            bgMusic.play();
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Failed to load bg music", e);
        }
    }

    /** Returns the first path that exists as a loadable texture, or {@code null}. */
    private static Texture firstOf(String... paths) {
        for (String p : paths) {
            Texture t = ScreenAssets.loadTexture(p);
            if (t != null) return t;
        }
        return null;
    }

    private boolean hasSaveGame() {
        Preferences prefs = Gdx.app.getPreferences("abyssal_descent_save");
        return prefs.getBoolean("has_save", false);
    }
}
