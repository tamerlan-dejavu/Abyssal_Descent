package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.config.DifficultySettings;

/**
 * Экран выбора сложности перед началом забега.
 *
 * Три кнопки:
 *   ЛЕГКО       HP 150 | Урон врагов x0.7 | Чипы +10%
 *   НОРМАЛЬНО   HP 100 | Урон врагов x1.0 | Базовый шанс
 *   СЛОЖНО      HP 75  | Урон врагов x1.4 | Чипы -10%
 *
 * Управление: вверх/вниз или мышь — выбор; ENTER — подтвердить.
 * Паттерн: State — реализует libGDX Screen.
 * Переход: DifficultySelectionScreen -> GameScreen.
 */
public class DifficultySelectionScreen implements Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final float BUTTON_W      = 320f;
    private static final float BUTTON_H      = 56f;
    private static final float BUTTON_GAP    = 20f;
    private static final float FADE_DURATION = 0.6f;

    // ── Цвета ────────────────────────────────────────────────────────────────
    private static final Color BG_COLOR     = new Color(0.05f, 0.05f, 0.10f, 1f);
    private static final Color TITLE_COLOR  = new Color(0.90f, 0.85f, 0.70f, 1f);
    private static final Color BTN_IDLE     = new Color(0.12f, 0.12f, 0.20f, 0.9f);
    private static final Color BTN_HOVER    = new Color(0.20f, 0.20f, 0.35f, 1.0f);
    private static final Color BTN_BORDER   = new Color(0.45f, 0.45f, 0.65f, 1f);
    private static final Color BTN_SELECTED = new Color(0.30f, 0.55f, 1.00f, 1f);
    private static final Color COLOR_EASY   = new Color(0.20f, 0.85f, 0.30f, 1f);
    private static final Color COLOR_NORMAL = new Color(0.90f, 0.75f, 0.10f, 1f);
    private static final Color COLOR_HARD   = new Color(0.90f, 0.20f, 0.15f, 1f);
    private static final Color COLOR_DESC   = new Color(0.60f, 0.60f, 0.65f, 1f);
    private static final Color COLOR_HINT   = new Color(0.40f, 0.40f, 0.45f, 1f);

    // ── Данные кнопок ─────────────────────────────────────────────────────────
    private static final String[] LABELS = { "ЛЕГКО", "НОРМАЛЬНО", "СЛОЖНО" };
    private static final String[] DESCRIPTIONS = {
        "HP 150  |  Урон врагов x0.7  |  Чипы +10%",
        "HP 100  |  Урон врагов x1.0  |  Базовый шанс",
        "HP 75   |  Урон врагов x1.4  |  Чипы -10%"
    };
    private static final Color[] LABEL_COLORS = {
        COLOR_EASY, COLOR_NORMAL, COLOR_HARD
    };

    // ── Зависимости ───────────────────────────────────────────────────────────
    private final GameStateManager gameStateManager;

    // ── libGDX ────────────────────────────────────────────────────────────────
    private final OrthographicCamera camera;
    private final SpriteBatch        batch;
    private final ShapeRenderer      shapeRenderer;
    private final BitmapFont         fontTitle;
    private final BitmapFont         fontLabel;
    private final BitmapFont         fontDesc;
    private final BitmapFont         fontHint;
    private final GlyphLayout        layout;

    // ── Состояние ────────────────────────────────────────────────────────────
    private int   selectedIndex = 1;   // NORMAL по умолчанию
    private float fadeTimer     = 0f;
    private float alpha         = 0f;

    // Позиции кнопок
    private final float[] btnX = new float[3];
    private final float[] btnY = new float[3];

    // ─────────────────────────────────────────────────────────────────────────

    public DifficultySelectionScreen(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;

        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        camera        = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout        = new GlyphLayout();

        fontTitle = new BitmapFont();
        fontTitle.getData().setScale(2.2f);

        fontLabel = new BitmapFont();
        fontLabel.getData().setScale(1.5f);

        fontDesc = new BitmapFont();
        fontDesc.getData().setScale(0.95f);

        fontHint = new BitmapFont();
        fontHint.getData().setScale(0.85f);

        calcButtonPositions(W, H);
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        fadeTimer     = 0f;
        alpha         = 0f;
        selectedIndex = 1;
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    // ── Логика ────────────────────────────────────────────────────────────────

    private void update(float delta) {
        fadeTimer += delta;
        alpha = Math.min(1f, Interpolation.fade.apply(fadeTimer / FADE_DURATION));

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex + 2) % 3;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % 3;
        }

        // Мышь hover
        float mx = Gdx.input.getX();
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();
        for (int i = 0; i < 3; i++) {
            if (mx >= btnX[i] && mx <= btnX[i] + BUTTON_W
             && my >= btnY[i] && my <= btnY[i] + BUTTON_H) {
                selectedIndex = i;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            confirmSelection();
        }
    }

    // ── Рендер ────────────────────────────────────────────────────────────────

    private void draw() {
        float W  = Gdx.graphics.getWidth();
        float H  = Gdx.graphics.getHeight();
        float cx = W / 2f;

        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Фоны кнопок
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 3; i++) {
            Color bg = i == selectedIndex ? BTN_HOVER : BTN_IDLE;
            shapeRenderer.setColor(new Color(bg.r, bg.g, bg.b, bg.a * alpha));
            shapeRenderer.rect(btnX[i], btnY[i], BUTTON_W, BUTTON_H);
        }
        shapeRenderer.end();

        // Границы кнопок
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < 3; i++) {
            Color border = i == selectedIndex ? BTN_SELECTED : BTN_BORDER;
            shapeRenderer.setColor(new Color(border.r, border.g, border.b, border.a * alpha));
            shapeRenderer.rect(btnX[i], btnY[i], BUTTON_W, BUTTON_H);
        }
        shapeRenderer.end();

        // Текст
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawCentered(fontTitle, "ВЫБОР СЛОЖНОСТИ", TITLE_COLOR, cx, H * 0.82f, alpha);

        for (int i = 0; i < 3; i++) {
            float labelX = btnX[i] + 20f;
            float labelY = btnY[i] + BUTTON_H - 14f;

            fontLabel.setColor(applyAlpha(LABEL_COLORS[i], alpha));
            fontLabel.draw(batch, LABELS[i], labelX, labelY);

            fontDesc.setColor(applyAlpha(COLOR_DESC, alpha));
            fontDesc.draw(batch, DESCRIPTIONS[i], labelX, btnY[i] + 18f);
        }

        drawCentered(fontHint, "вверх/вниз - выбор     ENTER - подтвердить",
                COLOR_HINT, cx, H * 0.12f, alpha);

        batch.end();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void calcButtonPositions(float W, float H) {
        float totalH = 3 * BUTTON_H + 2 * BUTTON_GAP;
        float startY = (H - totalH) / 2f;
        float startX = (W - BUTTON_W) / 2f;

        for (int i = 0; i < 3; i++) {
            btnX[i] = startX;
            btnY[i] = startY + (2 - i) * (BUTTON_H + BUTTON_GAP);
        }
    }

    private void confirmSelection() {
        DifficultySettings settings;
        switch (selectedIndex) {
            case 0:  settings = DifficultySettings.EASY;   break;
            case 2:  settings = DifficultySettings.HARD;   break;
            default: settings = DifficultySettings.NORMAL; break;
        }
        GameStateManager.setState(new GameScreen(settings));
        dispose();
    }

    private void drawCentered(BitmapFont font, String text, Color color,
                              float cx, float y, float a) {
        layout.setText(font, text);
        font.setColor(applyAlpha(color, a));
        font.draw(batch, text, cx - layout.width / 2f, y);
    }

    private Color applyAlpha(Color base, float a) {
        return new Color(base.r, base.g, base.b, base.a * a);
    }

    // ── Resize / Dispose ──────────────────────────────────────────────────────

    @Override
    public void resize(int w, int h) {
        camera.setToOrtho(false, w, h);
        calcButtonPositions(w, h);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        fontTitle.dispose();
        fontLabel.dispose();
        fontDesc.dispose();
        fontHint.dispose();
    }
}