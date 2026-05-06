package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Preferences;

import java.util.Arrays;
import java.util.List;

/**
 * Main menu screen — first screen shown on launch.
 *
 * <p>Buttons (GDD FR11):
 * <ul>
 *   <li>New Game   → DifficultyScreen</li>
 *   <li>Continue   → GameScreen (disabled when no save exists)</li>
 *   <li>Settings   → SettingsScreen</li>
 *   <li>Exit       → Gdx.app.exit()</li>
 * </ul>
 */
public class MainMenuScreen implements Screen {

    private static final float BTN_W   = 300f;
    private static final float BTN_H   = 80f;
    private static final float BTN_GAP = 30f;

    private OrthographicCamera camera;
    private SpriteBatch        batch;
    private ShapeRenderer      shapes;
    private Texture            background;
    private BitmapFont         font;
    private BitmapFont         titleFont;

    private List<MenuButton> buttons;

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
        font.getData().setScale(2.4f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3.5f);

        background = ScreenAssets.loadBackground("ui/backgrounds/main_menu_bg.png",
                0x08, 0x04, 0x14);

        float totalH = 4 * BTN_H + 3 * BTN_GAP;
        float startY = (sh - totalH) / 2f - 20f;
        float bx     = (sw - BTN_W)  / 2f;

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

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float wy = sh - screenY;
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

        // Background
        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        // Button backgrounds (with alpha blending)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (MenuButton btn : buttons) btn.renderBackground(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Title + button labels
        batch.begin();
        drawTitle(sw, sh);
        for (MenuButton btn : buttons) btn.renderLabel(batch, font);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
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

    // ── helpers ──────────────────────────────────────────────────────────────

    private void drawTitle(int sw, int sh) {
        String title = "ABYSSAL DESCENT";
        GlyphLayout layout = new GlyphLayout(titleFont, title);
        float tx = (sw - layout.width) / 2f;
        float ty = sh * 0.82f;
        // subtle shadow
        titleFont.setColor(0f, 0f, 0f, 0.6f);
        titleFont.draw(batch, title, tx + 3, ty - 3);
        titleFont.setColor(0.85f, 0.75f, 1f, 1f); // light purple-white
        titleFont.draw(batch, title, tx, ty);
    }

    private boolean hasSaveGame() {
        Preferences prefs = Gdx.app.getPreferences("abyssal_descent_save");
        return prefs.getBoolean("has_save", false);
    }
}
