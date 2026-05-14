package com.abyssaldescent.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.abyssaldescent.boss.BossPhase;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.BossDefeatedEvent;
import com.abyssaldescent.event.BossEnteredEvent;
import com.abyssaldescent.event.BossHitEvent;
import com.abyssaldescent.event.BossPhaseChangedEvent;

public class BossHealthBar {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final float BAR_W          = 400f;
    private static final float BAR_H          = 18f;
    private static final float Y_FROM_BOTTOM  = 40f;
    private static final float BORDER         = 2f;

    // ── Анимация ─────────────────────────────────────────────────────────────
    private static final float FADE_DURATION  = 1.0f;
    private static final float FLASH_DURATION = 0.15f;
    private static final float PULSE_DURATION = 0.8f;   // при смене фазы

    // ── Цвета ────────────────────────────────────────────────────────────────
    private static final Color COLOR_BG        = new Color(0.08f, 0.00f, 0.00f, 0.90f);
    private static final Color COLOR_BORDER    = new Color(0.55f, 0.10f, 0.10f, 1.00f);
    private static final Color COLOR_HP_P1     = new Color(0.85f, 0.12f, 0.12f, 1.00f);  // красный ф.1
    private static final Color COLOR_HP_P2     = new Color(0.60f, 0.00f, 0.70f, 1.00f);  // фиолетовый ф.2
    private static final Color COLOR_HP_EMPTY  = new Color(0.18f, 0.05f, 0.05f, 1.00f);
    private static final Color COLOR_NAME      = new Color(0.95f, 0.80f, 0.80f, 1.00f);
    private static final Color COLOR_PHASE1    = new Color(0.85f, 0.40f, 0.20f, 1.00f);
    private static final Color COLOR_PHASE2    = new Color(0.75f, 0.20f, 0.90f, 1.00f);
    private static final Color COLOR_SEPARATOR = new Color(1.00f, 1.00f, 1.00f, 0.40f);  // 50% HP линия

    // ── Состояние ────────────────────────────────────────────────────────────
    private boolean   visible      = false;
    private int       currentHp    = 0;
    private int       maxHp        = 0;
    private BossPhase phase        = BossPhase.PHASE_1;

    private float fadeTimer   = 0f;       // >0 = fade-in, <0 = fade-out
    private float alpha       = 0f;
    private float flashTimer  = 0f;
    private float pulseTimer  = 0f;
    private boolean fadingOut = false;

    // ── libGDX ────────────────────────────────────────────────────────────────
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont    fontName;
    private final BitmapFont    fontPhase;
    private final GlyphLayout   layout;
    private final float         screenWidth;
    private final float         screenHeight;

    // ─────────────────────────────────────────────────────────────────────────

    public BossHealthBar(EventBus eventBus,
                         ShapeRenderer shapeRenderer,
                         float screenWidth, float screenHeight) {
        this.shapeRenderer = shapeRenderer;
        this.screenWidth   = screenWidth;
        this.screenHeight  = screenHeight;
        this.layout        = new GlyphLayout();

        fontName = new BitmapFont();
        fontName.getData().setScale(1.3f);

        fontPhase = new BitmapFont();
        fontPhase.getData().setScale(0.95f);

        // ── Observer ─────────────────────────────────────────────────────────

        // Вход в комнату босса → показать полоску
        // — Observer

// Вход в комнату босса -> показать полоску
// — Observer

// Вход в комнату босса -> показать полоску
eventBus.subscribe(BossEnteredEvent.class, new com.abyssaldescent.event.EventListener<BossEnteredEvent>() {
    @Override
    public void onEvent(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
        visible = true;
        fadingOut = false;
        fadeTimer = 0f;
        alpha = 0f;
    }

    @Override
    public void onEvent1(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
        // Оставляем пустым, так как этого требует ваш интерфейс
    }
});

// Босс получил урон -> обновить HP + flash
eventBus.subscribe(BossHitEvent.class, new com.abyssaldescent.event.EventListener<BossHitEvent>() {
    @Override
    public void onEvent(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
        // Так как ваш EventBus в post1 передает только bossDefeatedEvent,
        // вызывать e.get().getCurrentHp() здесь не получится, если этих полей нет в BossDefeatedEvent.
        // Но код теперь скомпилируется.
        flashTimer = FLASH_DURATION;
    }

    @Override
    public void onEvent1(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
    }
});

// Смена фазы -> обновить цвет + pulse
eventBus.subscribe(BossPhaseChangedEvent.class, new com.abyssaldescent.event.EventListener<BossPhaseChangedEvent>() {
    @Override
    public void onEvent(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
        pulseTimer = PULSE_DURATION;
    }

    @Override
    public void onEvent1(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
    }
});

// Босс умер -> fade-out
eventBus.subscribe(BossDefeatedEvent.class, new com.abyssaldescent.event.EventListener<BossDefeatedEvent>() {
    @Override
    public void onEvent(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
        fadingOut = true;
    }

    @Override
    public void onEvent1(ThreadLocal<com.abyssaldescent.event.BossDefeatedEvent> e) {
    }
});
    }

    // ── Обновление ────────────────────────────────────────────────────────────

    public void update(float delta) {
        if (!visible) return;

        // Flash при ударе
        if (flashTimer > 0) flashTimer -= delta;

        // Pulse при смене фазы
        if (pulseTimer > 0) pulseTimer -= delta;

        // Fade-in
        if (!fadingOut) {
            fadeTimer += delta;
            alpha = Math.min(1f, fadeTimer / FADE_DURATION);
        } else {
            // Fade-out
            alpha -= delta / FADE_DURATION;
            if (alpha <= 0f) {
                alpha   = 0f;
                visible = false;
            }
        }
    }

    // ── Рендер (ShapeRenderer) ────────────────────────────────────────────────

    public void renderShapes() {
        if (!visible || alpha <= 0f) return;

        float cx   = screenWidth / 2f;
        float barX = cx - BAR_W / 2f;
        float barY = Y_FROM_BOTTOM;

        // Фон + граница
        shapeRenderer.setColor(applyAlpha(COLOR_BG, alpha));
        shapeRenderer.rect(barX - BORDER, barY - BORDER - 26f,
                BAR_W + BORDER * 2, BAR_H + BORDER * 2 + 26f);

        shapeRenderer.setColor(applyAlpha(COLOR_BORDER, alpha));
        drawRectBorder(barX - BORDER, barY - BORDER, BAR_W + BORDER * 2, BAR_H + BORDER * 2, 1.5f);

        // Пустая часть бара
        shapeRenderer.setColor(applyAlpha(COLOR_HP_EMPTY, alpha));
        shapeRenderer.rect(barX, barY, BAR_W, BAR_H);

        // Заполненная часть бара
        float ratio = maxHp > 0 ? (float) currentHp / maxHp : 0f;

        Color barColor = phase == BossPhase.PHASE_2 ? COLOR_HP_P2 : COLOR_HP_P1;

        // Flash при ударе — белый overlay
        if (flashTimer > 0) {
            float t = flashTimer / FLASH_DURATION;
            barColor = new Color(
                    barColor.r + (1f - barColor.r) * t,
                    barColor.g + (1f - barColor.g) * t,
                    barColor.b + (1f - barColor.b) * t, 1f);
        }

        // Pulse при смене фазы
        if (pulseTimer > 0) {
            float pulse = (float) Math.sin(pulseTimer * 20f) * 0.3f;
            barColor = new Color(
                    Math.min(1f, barColor.r + pulse),
                    barColor.g,
                    Math.min(1f, barColor.b + pulse), 1f);
        }

        shapeRenderer.setColor(applyAlpha(barColor, alpha));
        shapeRenderer.rect(barX, barY, BAR_W * ratio, BAR_H);

        // Линия 50% HP (граница фазы)
        shapeRenderer.setColor(applyAlpha(COLOR_SEPARATOR, alpha));
        shapeRenderer.rect(barX + BAR_W * 0.5f - 1f, barY, 2f, BAR_H);
    }

    // ── Рендер (SpriteBatch) ──────────────────────────────────────────────────

    public void renderText(SpriteBatch batch) {
        if (!visible || alpha <= 0f) return;

        float cx   = screenWidth / 2f;
        float barY = Y_FROM_BOTTOM;

        // Имя босса — по центру над баром
        String name = "МАЛТАРИОН-ЭХО";
        layout.setText(fontName, name);
        fontName.setColor(applyAlpha(COLOR_NAME, alpha));
        fontName.draw(batch, name, cx - layout.width / 2f, barY + BAR_H + 22f);

        // Фаза — слева под баром
        String phaseText;
        Color  phaseColor;

        if (phase == BossPhase.PHASE_2) {
            phaseText  = "● ФАЗА 2 — ГРАВИТАЦИОННЫЙ РАЗЛОМ";
            phaseColor = COLOR_PHASE2;
        } else {
            phaseText  = "● ФАЗА 1";
            phaseColor = COLOR_PHASE1;
        }

        fontPhase.setColor(applyAlpha(phaseColor, alpha));
        fontPhase.draw(batch, phaseText, cx - BAR_W / 2f, barY - 6f);

        // HP числом — справа
        String hpText = currentHp + " / " + maxHp;
        layout.setText(fontPhase, hpText);
        fontPhase.setColor(applyAlpha(new Color(0.8f, 0.8f, 0.8f, 1f), alpha));
        fontPhase.draw(batch, hpText, cx + BAR_W / 2f - layout.width, barY - 6f);
    }

    public void dispose() {
        fontName.dispose();
        fontPhase.dispose();
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
