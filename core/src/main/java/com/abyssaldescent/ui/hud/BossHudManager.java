package com.abyssaldescent.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.abyssaldescent.boss.BossStats;
import com.abyssaldescent.event.EventBus;

/**
 * Менеджер HUD для комнаты босса.
 * Объединяет BossHealthBar и BossRoomIndicator.
 *
 * Подключить в GameScreen рядом с GameHud:
 *
 *   // show():
 *   bossHud = new BossHudManager(bossStats, eventBus);
 *
 *   // render() — ПОСЛЕ основного рендера уровня:
 *   bossHud.update(delta);
 *   bossHud.render();
 *
 *   // dispose():
 *   bossHud.dispose();
 *
 * Запустить при входе в комнату босса:
 *   eventBus.post(new BossEnteredEvent());
 *   bossStats.activate();
 */
public class BossHudManager {

    private final BossHealthBar     healthBar;
    private final BossRoomIndicator roomIndicator;

    private final ShapeRenderer      shapeRenderer;
    private final SpriteBatch        hudBatch;
    private final OrthographicCamera hudCamera;

    // ─────────────────────────────────────────────────────────────────────────

    public BossHudManager(BossStats bossStats, EventBus eventBus) {
        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, W, H);

        shapeRenderer = new ShapeRenderer();
        hudBatch      = new SpriteBatch();

        healthBar     = new BossHealthBar(eventBus, shapeRenderer, W, H);
        roomIndicator = new BossRoomIndicator(eventBus, shapeRenderer, W, H);
    }

    // ── Обновление ────────────────────────────────────────────────────────────

    public void update(float delta) {
        healthBar.update(delta);
        roomIndicator.update(delta);
    }

    // ── Рендер ────────────────────────────────────────────────────────────────

    public void render() {
        hudCamera.update();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // --- Shapes ----------------------------------------------------------
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            healthBar.renderShapes();
            roomIndicator.renderShapes();

        shapeRenderer.end();

        // --- Text ------------------------------------------------------------
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();

            healthBar.renderText(hudBatch);
            roomIndicator.renderText(hudBatch);

        hudBatch.end();
    }

    // ── Resize / Dispose ──────────────────────────────────────────────────────

    public void resize(int w, int h) {
        hudCamera.setToOrtho(false, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
        hudBatch.dispose();
        healthBar.dispose();
        roomIndicator.dispose();
    }
}