package com.abyssaldescent.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.BossEnteredEvent;

public class BossRoomIndicator {

    private static final float SHOW_DURATION  = 3.0f;
    private static final float FADE_DURATION  = 0.5f;

    // Цвета
    private static final Color COLOR_BG       = new Color(0.10f, 0.00f, 0.00f, 0.75f);
    private static final Color COLOR_WARNING  = new Color(0.95f, 0.15f, 0.10f, 1.00f);
    private static final Color COLOR_NAME     = new Color(1.00f, 0.80f, 0.20f, 1.00f);
    private static final Color COLOR_BORDER   = new Color(0.85f, 0.10f, 0.10f, 1.00f);

    // Состояние
    private float timer   = 0f;
    private boolean shown = false;
    private float alpha   = 0f;

    // Мигание рамки
    private float blinkTimer = 0f;

    private final ShapeRenderer shapeRenderer;
    private final BitmapFont    fontWarning;
    private final BitmapFont    fontName;
    private final GlyphLayout   layout;
    private final float         screenWidth;
    private final float         screenHeight;

    // ─────────────────────────────────────────────────────────────────────────

    public BossRoomIndicator(EventBus eventBus,
                             ShapeRenderer shapeRenderer,
                             float screenWidth, float screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.screenWidth   = screenWidth;
        this.screenHeight  = screenHeight;
        this.layout        = new GlyphLayout();

        fontWarning = new BitmapFont();
        fontWarning.getData().setScale(2.0f);

        fontName = new BitmapFont();
        fontName.getData().setScale(1.4f);

        eventBus.subscribe(BossEnteredEvent.class, e -> {
            shown      = true;
            timer      = 0f;
            alpha      = 0f;
            blinkTimer = 0f;
        });
    }

    // ── Обновление ────────────────────────────────────────────────────────────

    public void update(float delta) {
        if (!shown) return;

        timer      += delta;
        blinkTimer += delta;

        // Fade-in
        if (timer < FADE_DURATION) {
            alpha = timer / FADE_DURATION;
        }
        // Показ
        else if (timer < SHOW_DURATION - FADE_DURATION) {
            alpha = 1f;
        }
        // Fade-out
        else if (timer < SHOW_DURATION) {
            alpha = 1f - (timer - (SHOW_DURATION - FADE_DURATION)) / FADE_DURATION;
        }
        // Конец
        else {
            shown = false;
            alpha = 0f;
        }
    }

    // ── Рендер (ShapeRenderer) ────────────────────────────────────────────────

    public void renderShapes() {
        if (!shown || alpha <= 0f) return;

        float cx = screenWidth  / 2f;
        float cy = screenHeight / 2f;

        // Фоновый прямоугольник по центру
        float bgW = 380f;
        float bgH = 90f;
        shapeRenderer.setColor(applyAlpha(COLOR_BG, alpha));
        shapeRenderer.rect(cx - bgW / 2f, cy - bgH / 2f, bgW, bgH);

        // Граница баннера
        shapeRenderer.setColor(applyAlpha(COLOR_WARNING, alpha));
        drawRectBorder(cx - bgW / 2f, cy - bgH / 2f, bgW, bgH, 2f);

        // Мигающая красная рамка по краям экрана
        float blinkAlpha = (float)(Math.sin(blinkTimer * 8f) * 0.5f + 0.5f) * alpha * 0.6f;
        Color edgeColor = new Color(COLOR_BORDER.r, COLOR_BORDER.g, COLOR_BORDER.b, blinkAlpha);
        shapeRenderer.setColor(edgeColor);
        float edgeT = 8f;
        shapeRenderer.rect(0,                        0,                         screenWidth,  edgeT);
        shapeRenderer.rect(0,                        screenHeight - edgeT,      screenWidth,  edgeT);
        shapeRenderer.rect(0,                        0,                         edgeT,        screenHeight);
        shapeRenderer.rect(screenWidth - edgeT,      0,                         edgeT,        screenHeight);
    }

    // ── Рендер (SpriteBatch) ──────────────────────────────────────────────────

    public void renderText(SpriteBatch batch) {
        if (!shown || alpha <= 0f) return;

        float cx = screenWidth  / 2f;
        float cy = screenHeight / 2f;

        // "⚠ БОСС ВПЕРЕДИ ⚠"
        String warning = "! БОСС ВПЕРЕДИ !";
        layout.setText(fontWarning, warning);
        fontWarning.setColor(applyAlpha(COLOR_WARNING, alpha));
        fontWarning.draw(batch, warning, cx - layout.width / 2f, cy + 30f);

        // "МАЛТАРИОН-ЭХО"
        String name = "МАЛТАРИОН-ЭХО";
        layout.setText(fontName, name);
        fontName.setColor(applyAlpha(COLOR_NAME, alpha));
        fontName.draw(batch, name, cx - layout.width / 2f, cy - 8f);
    }

    public void dispose() {
        fontWarning.dispose();
        fontName.dispose();
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void drawRectBorder(float x, float y, float w, float h, float t) {
        shapeRenderer.rect(x,         y,         w, t);
        shapeRenderer.rect(x,         y + h - t, w, t);
        shapeRenderer.rect(x,         y,         t, h);
        shapeRenderer.rect(x + w - t, y,         t, h);
    }

    private Color applyAlpha(Color base, float a) {
        return new Color(base.r, base.g, base.b, base.a * a);
    }
}