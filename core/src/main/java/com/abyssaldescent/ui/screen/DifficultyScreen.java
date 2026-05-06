package com.abyssaldescent.ui.screen;

import com.abyssaldescent.config.DifficultySettings;
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

import java.util.Arrays;
import java.util.List;

/**
 * Difficulty selection screen (GDD FR11 §2.2).
 *
 * <p>Three difficulty cards are arranged in a horizontal row at screen centre.
 * Each card shows the difficulty name and its stat modifiers.
 * A small Back button returns to {@link MainMenuScreen}.
 */
public class DifficultyScreen implements Screen {

    private static final float CARD_W   = 280f;
    private static final float CARD_H   = 80f;
    private static final float CARD_GAP = 60f;
    private static final float BACK_W   = 150f;
    private static final float BACK_H   = 50f;
    private static final float DESC_SCALE = 0.9f;

    private OrthographicCamera camera;
    private SpriteBatch        batch;
    private ShapeRenderer      shapes;
    private Texture            background;
    private BitmapFont         font;
    private BitmapFont         smallFont;
    private BitmapFont         titleFont;

    private List<MenuButton> diffButtons;
    private MenuButton       backButton;

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

        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.2f);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        background = ScreenAssets.loadBackground("ui/backgrounds/main_menu_bg.png",
                0x06, 0x04, 0x10);

        float totalW = 3 * CARD_W + 2 * CARD_GAP;
        float startX = (sw - totalW) / 2f;
        float cardY  = sh / 2f - CARD_H / 2f;

        MenuButton easyBtn = new MenuButton(
                "Easy",
                startX, cardY, CARD_W, CARD_H,
                () -> UiManager.getInstance().startNewGame(DifficultySettings.EASY));

        MenuButton normalBtn = new MenuButton(
                "Normal",
                startX + CARD_W + CARD_GAP, cardY, CARD_W, CARD_H,
                () -> UiManager.getInstance().startNewGame(DifficultySettings.NORMAL));

        MenuButton hardBtn = new MenuButton(
                "Hard",
                startX + 2 * (CARD_W + CARD_GAP), cardY, CARD_W, CARD_H,
                () -> UiManager.getInstance().startNewGame(DifficultySettings.HARD));

        backButton = new MenuButton(
                "Back",
                30f, 30f, BACK_W, BACK_H,
                () -> UiManager.getInstance().showMainMenu());

        diffButtons = Arrays.asList(easyBtn, normalBtn, hardBtn);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float wy = sh - screenY;
                for (MenuButton btn : diffButtons) {
                    if (btn.handleClick(screenX, wy)) return true;
                }
                return backButton.handleClick(screenX, wy);
            }
        });
    }

    @Override
    public void render(float delta) {
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();
        for (MenuButton btn : diffButtons) btn.update(mx, my);
        backButton.update(mx, my);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, sw, sh);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (MenuButton btn : diffButtons) btn.renderBackground(shapes);
        backButton.renderBackground(shapes);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        drawTitle(sw, sh);
        drawDescriptions(sw, sh);
        for (MenuButton btn : diffButtons) btn.renderLabel(batch, font);
        backButton.renderLabel(batch, font);
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
        smallFont.dispose();
        titleFont.dispose();
        background.dispose();
    }

    // ── rendering helpers ─────────────────────────────────────────────────────

    private void drawTitle(int sw, int sh) {
        String text = "SELECT DIFFICULTY";
        GlyphLayout layout = new GlyphLayout(titleFont, text);
        titleFont.setColor(0.85f, 0.75f, 1f, 1f);
        titleFont.draw(batch, text, (sw - layout.width) / 2f, sh * 0.80f);
    }

    private void drawDescriptions(int sw, int sh) {
        DifficultySettings[] presets = {
            DifficultySettings.EASY,
            DifficultySettings.NORMAL,
            DifficultySettings.HARD
        };

        float totalW = 3 * CARD_W + 2 * CARD_GAP;
        float startX = (sw - totalW) / 2f;
        float cardY  = sh / 2f - CARD_H / 2f;
        float descY  = cardY - 20f;

        smallFont.getData().setScale(DESC_SCALE);
        smallFont.setColor(0.75f, 0.75f, 0.75f, 1f);

        for (int i = 0; i < presets.length; i++) {
            float cx = startX + i * (CARD_W + CARD_GAP);
            String desc = presets[i].getDescription();
            GlyphLayout layout = new GlyphLayout(smallFont, desc);
            float tx = cx + (CARD_W - layout.width) / 2f;
            smallFont.draw(batch, desc, tx, descY);
        }
    }
}
