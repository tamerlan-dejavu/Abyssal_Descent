package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Arrays;
import java.util.List;

/**
 * Main menu screen — first screen shown on launch (GDD FR11).
 *
 * <p>Asset slots (place files in project {@code assets/} folder):
 * <pre>
 *   ui/backgrounds/main_menu_bg.png   — full-screen background
 *   ui/buttons/button_normal.png      — button idle state  (500×150 px)
 *   ui/buttons/button_hover.png       — button hover state (500×150 px, optional)
 *   ui/credits/project_info.png       — project info card  (500×300 px, bottom-left)
 * </pre>
 *
 * <p>Buttons (centre of stack ≈ 40 % from bottom):
 * <ul>
 *   <li>New Game  → DifficultyScreen</li>
 *   <li>Continue  → GameScreen (disabled when no save)</li>
 *   <li>Settings  → SettingsScreen</li>
 *   <li>Exit      → Gdx.app.exit()</li>
 * </ul>
 */
public class MainMenuScreen implements Screen {

    private static final float BTN_W          = 500f;
    private static final float BTN_H          = 150f;
    private static final float BTN_GAP        = 20f;
    private static final float CREDITS_W      = 500f;
    private static final float CREDITS_H      = 300f;
    private static final float CREDITS_MARGIN = 20f;

    private OrthographicCamera camera;
    private SpriteBatch        batch;
    private ShapeRenderer      shapes;

    private Texture background;
    private Texture btnNormalTex;
    private Texture btnHoverTex;
    private Texture creditsTex;

    private BitmapFont font;

    private List<MenuButton> buttons;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(3f);

        background  = ScreenAssets.loadBackground("ui/backgrounds/main_menu_bg.png", 0x08, 0x04, 0x14);
        btnNormalTex = loadTexture("ui/buttons/button_normal.png");
        btnHoverTex  = loadTexture("ui/buttons/button_hover.png");
        creditsTex   = loadTexture("ui/credits/project_info.png");

        buildButtons();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float wy = Gdx.graphics.getHeight() - screenY;
                for (MenuButton btn : buttons) {
                    if (btn.handleClick(screenX, wy)) return true;
                }
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
        for (MenuButton btn : buttons) btn.update(mx, my);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── 1. Background ────────────────────────────────────────────────────
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        // ── 2. Button shapes (fallback when no textures, + hover border always) ──
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (MenuButton btn : buttons) btn.renderBackground(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── 3. Button textures + labels + credits panel ──────────────────────
        batch.begin();
        for (MenuButton btn : buttons) btn.renderTexture(batch);
        for (MenuButton btn : buttons) btn.renderLabel(batch, font);
        drawCreditsPanel();
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        buildButtons();
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
        background.dispose();
        if (btnNormalTex != null) btnNormalTex.dispose();
        if (btnHoverTex  != null) btnHoverTex.dispose();
        if (creditsTex   != null) creditsTex.dispose();
    }

    // ── layout ────────────────────────────────────────────────────────────────

    private void buildButtons() {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        camera.setToOrtho(false, sw, sh);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // Centre of button stack: ~40 % from bottom (slightly below screen centre)
        float totalH = 4 * BTN_H + 3 * BTN_GAP;
        float stackCY = sh * 0.40f;
        float startY  = stackCY - totalH / 2f;
        float bx      = (sw - BTN_W) / 2f;

        MenuButton exitBtn = new MenuButton(
                "Exit",
                bx, startY, BTN_W, BTN_H,
                () -> Gdx.app.exit());

        MenuButton settingsBtn = new MenuButton(
                "Settings",
                bx, startY + (BTN_H + BTN_GAP), BTN_W, BTN_H,
                () -> UiManager.getInstance().showSettings());

        MenuButton continueBtn = new MenuButton(
                "Continue",
                bx, startY + 2 * (BTN_H + BTN_GAP), BTN_W, BTN_H,
                () -> UiManager.getInstance().continueGame());

        MenuButton newGameBtn = new MenuButton(
                "New Game",
                bx, startY + 3 * (BTN_H + BTN_GAP), BTN_W, BTN_H,
                () -> UiManager.getInstance().showDifficulty());

        if (!hasSaveGame()) {
            continueBtn.setEnabled(false);
            continueBtn.setAlpha(0.5f);
        }

        buttons = Arrays.asList(newGameBtn, continueBtn, settingsBtn, exitBtn);

        if (btnNormalTex != null) {
            for (MenuButton btn : buttons) {
                btn.setTextures(btnNormalTex, btnHoverTex);
            }
        }
    }

    // ── rendering helpers ─────────────────────────────────────────────────────

    private void drawCreditsPanel() {
        if (creditsTex == null) return;
        batch.draw(creditsTex, CREDITS_MARGIN, CREDITS_MARGIN, CREDITS_W, CREDITS_H);
    }

    // ── asset helpers ─────────────────────────────────────────────────────────

    /**
     * Loads a texture from the internal assets folder.
     * Returns {@code null} if the file does not exist.
     */
    private static Texture loadTexture(String path) {
        if (!Gdx.files.internal(path).exists()) return null;
        try {
            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Failed to load: " + path, e);
            return null;
        }
    }

    private boolean hasSaveGame() {
        Preferences prefs = Gdx.app.getPreferences("abyssal_descent_save");
        return prefs.getBoolean("has_save", false);
    }
}
