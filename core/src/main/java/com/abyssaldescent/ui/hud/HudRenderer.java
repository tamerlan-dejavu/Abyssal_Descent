package com.abyssaldescent.ui.hud;

import com.abyssaldescent.combat.chips.ChipInventory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Facade that owns and renders every HUD widget in screen-space.
 * Uses a dedicated OrthographicCamera mapped 1:1 to physical pixels so widgets
 * remain fixed regardless of how the world camera moves.
 */
public final class HudRenderer {

    private final OrthographicCamera  hudCamera;
    private final BitmapFont          fontMedium;
    private final BitmapFont          fontSmall;
    private final HealthBarWidget     healthBar;
    private final HudChipSlotWidget   chipSlots;

    public HudRenderer(ChipInventory chipInventory) {
        hudCamera  = new OrthographicCamera();

        fontMedium = new BitmapFont();
        fontMedium.getData().setScale(1.6f);

        fontSmall  = new BitmapFont();
        fontSmall.getData().setScale(0.9f);

        healthBar = new HealthBarWidget(fontMedium);
        chipSlots = new HudChipSlotWidget(fontSmall);
        chipInventory.addObserver(chipSlots);

        updateCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        updateCamera(width, height);
    }

    private void updateCamera(int w, int h) {
        hudCamera.setToOrtho(false, w, h);
        hudCamera.update();
    }

    public void render(SpriteBatch batch, ShapeRenderer shapes, float dt) {
        float sw = hudCamera.viewportWidth;
        float sh = hudCamera.viewportHeight;

        healthBar.syncFromGameState();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(hudCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.renderShapes(shapes, 20f, sh - 60f);
        chipSlots.renderShapes(shapes, sw, sh);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        healthBar.renderText(batch, 20f, sh - 60f);
        chipSlots.renderText(batch, sw, sh);
        batch.end();
    }

    /** Trigger an on-demand chip in the given 0-based slot (keys 1–4). */
    public void activateChipSlot(int slot) {
        chipSlots.activateSlot(slot);
    }

    public void dispose() {
        fontMedium.dispose();
        fontSmall.dispose();
        healthBar.dispose();
        chipSlots.dispose();
    }
}
