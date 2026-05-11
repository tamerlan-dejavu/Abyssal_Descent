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
import com.badlogic.gdx.math.MathUtils;

public class EndingScreen implements Screen {

    private static final float FADE_IN_DURATION  = 1.5f;
    private static final int   PARTICLE_COUNT    = 60;
    private static final float PARTICLE_LIFETIME = 3.0f;

    private static final Color BG_TOP      = new Color(0.03f, 0.02f, 0.08f, 1f);
    private static final Color TITLE_COLOR = new Color(1.00f, 0.85f, 0.10f, 1f);
    private static final Color SUB_COLOR   = new Color(0.75f, 0.65f, 0.90f, 1f);
    private static final Color LABEL_COLOR = new Color(0.55f, 0.55f, 0.65f, 1f);
    private static final Color VALUE_COLOR = new Color(0.95f, 0.95f, 1.00f, 1f);
    private static final Color LINE_COLOR  = new Color(0.40f, 0.30f, 0.10f, 1f);
    private static final Color HINT_COLOR  = new Color(0.50f, 0.50f, 0.50f, 1f);

    private final RunStats stats;

    private OrthographicCamera camera;
    private SpriteBatch        batch;
    private ShapeRenderer      shapeRenderer;
    private BitmapFont         fontTitle;
    private BitmapFont         fontSub;
    private BitmapFont         fontBody;
    private BitmapFont         fontHint;
    private GlyphLayout        layout;

    private float   fadeTimer  = 0f;
    private float   alpha      = 0f;
    private float   pulseTimer = 0f;
    private boolean inputReady = false;

    private final float[] px, py, pvx, pvy, plife, psize;
    private final Color[] pcolor;

    public EndingScreen(RunStats stats) {
        this.stats = stats;

        px    = new float[PARTICLE_COUNT];
        py    = new float[PARTICLE_COUNT];
        pvx   = new float[PARTICLE_COUNT];
        pvy   = new float[PARTICLE_COUNT];
        plife = new float[PARTICLE_COUNT];
        psize = new float[PARTICLE_COUNT];
        pcolor = new Color[PARTICLE_COUNT];
    }

    @Override
    public void show() {
        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        camera        = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        batch         = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        layout        = new GlyphLayout();

        fontTitle = new BitmapFont(); fontTitle.getData().setScale(3.8f);
        fontSub   = new BitmapFont(); fontSub.getData().setScale(1.5f);
        fontBody  = new BitmapFont(); fontBody.getData().setScale(1.3f);
        fontHint  = new BitmapFont(); fontHint.getData().setScale(0.9f);

        fadeTimer  = 0f;
        pulseTimer = 0f;
        alpha      = 0f;
        inputReady = false;

        spawnAllParticles(W, H);
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        fadeTimer  += delta;
        pulseTimer += delta;
        alpha = Math.min(1f, Interpolation.fade.apply(fadeTimer / FADE_IN_DURATION));
        if (fadeTimer >= FADE_IN_DURATION) inputReady = true;

        updateParticles(delta);

        if (inputReady) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
             || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
             || Gdx.input.justTouched()) {
                goToMainMenu();
            }
        }
    }

    private void spawnAllParticles(float W, float H) {
        Color[] colors = {
            new Color(1f, 0.9f, 0.2f, 1f),
            new Color(1f, 1f,   1f,   1f),
            new Color(0.7f, 0.5f, 1f, 1f),
            new Color(0.3f, 0.9f, 1f, 1f),
        };
        for (int i = 0; i < PARTICLE_COUNT; i++) spawnParticle(i, W, H, colors);
    }

    private void spawnParticle(int i, float W, float H, Color[] colors) {
        px[i]    = MathUtils.random(0f, W);
        py[i]    = MathUtils.random(-20f, 0f);
        pvx[i]   = MathUtils.random(-30f, 30f);
        pvy[i]   = MathUtils.random(60f, 160f);
        plife[i] = MathUtils.random(0.5f, PARTICLE_LIFETIME);
        psize[i] = MathUtils.random(2f, 5f);
        pcolor[i] = colors[MathUtils.random(0, colors.length - 1)];
    }

    private void updateParticles(float delta) {
        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();
        Color[] colors = {
            new Color(1f, 0.9f, 0.2f, 1f),
            new Color(1f, 1f,   1f,   1f),
            new Color(0.7f, 0.5f, 1f, 1f),
            new Color(0.3f, 0.9f, 1f, 1f),
        };
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            plife[i] -= delta;
            px[i]    += pvx[i] * delta;
            py[i]    += pvy[i] * delta;
            pvy[i]   -= 20f * delta;
            if (plife[i] <= 0 || py[i] > H + 10) spawnParticle(i, W, H, colors);
        }
    }

    private void draw() {
        float W  = Gdx.graphics.getWidth();
        float H  = Gdx.graphics.getHeight();
        float cx = W / 2f;

        Gdx.gl.glClearColor(BG_TOP.r, BG_TOP.g, BG_TOP.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float lifeRatio = Math.max(0, plife[i] / PARTICLE_LIFETIME);
            Color c = pcolor[i];
            shapeRenderer.setColor(c.r, c.g, c.b, lifeRatio * alpha);
            float s = psize[i] * lifeRatio;
            shapeRenderer.rect(px[i] - s / 2, py[i] - s / 2, s, s);
        }

        Color lineC = new Color(LINE_COLOR.r, LINE_COLOR.g, LINE_COLOR.b, alpha * 0.8f);
        shapeRenderer.setColor(lineC);
        shapeRenderer.rect(cx - 130f, H * 0.54f, 260f, 1.5f);
        shapeRenderer.rect(cx - 130f, H * 0.33f, 260f, 1.5f);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float pulse = 1f + (float) Math.sin(pulseTimer * 3f) * 0.03f;
        fontTitle.getData().setScale(3.8f * pulse);
        drawCentered(fontTitle, "ПОБЕДА!", TITLE_COLOR, cx, H * 0.80f, alpha);
        fontTitle.getData().setScale(3.8f);

        drawCentered(fontSub, "Малтарион-Эхо повержен", SUB_COLOR, cx, H * 0.67f, alpha * 0.9f);

        float statY    = H * 0.51f;
        float lineStep = 32f;
        float labelX   = cx - 120f;
        float valueX   = cx + 120f;

        if (stats != null) {
            drawStatRow("Время",  stats.getFormattedTime(),                  labelX, valueX, statY,               alpha);
            drawStatRow("Ярус",   String.valueOf(stats.getCurrentFloor()),    labelX, valueX, statY - lineStep,     alpha);
            drawStatRow("Врагов", String.valueOf(stats.getEnemiesKilled()),   labelX, valueX, statY - lineStep * 2, alpha);
            drawStatRow("Чипов",  String.valueOf(stats.getChipsCollected()),  labelX, valueX, statY - lineStep * 3, alpha);
        }

        String hint = inputReady ? "[ ENTER ]  Главное меню" : "";
        drawCentered(fontHint, hint, HINT_COLOR, cx, H * 0.18f, alpha);

        batch.end();
    }

    private void drawStatRow(String label, String value,
                             float labelX, float valueX, float y, float a) {
        fontBody.setColor(applyAlpha(LABEL_COLOR, a));
        fontBody.draw(batch, label, labelX, y);
        layout.setText(fontBody, value);
        fontBody.setColor(applyAlpha(VALUE_COLOR, a));
        fontBody.draw(batch, value, valueX - layout.width, y);
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

    private void goToMainMenu() {
        UiManager.getInstance().showMainMenu();
    }

    @Override public void resize(int w, int h) {
        if (camera != null) camera.setToOrtho(false, w, h);
    }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (batch         != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fontTitle     != null) fontTitle.dispose();
        if (fontSub       != null) fontSub.dispose();
        if (fontBody      != null) fontBody.dispose();
        if (fontHint      != null) fontHint.dispose();
    }
}
