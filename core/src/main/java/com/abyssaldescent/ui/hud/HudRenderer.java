package com.abyssaldescent.ui.hud;

import com.abyssaldescent.combat.chips.ChipInventory;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.InventoryToggleEvent;
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

    private final OrthographicCamera hudCamera;
    private final BitmapFont         fontMedium;
    private final BitmapFont         fontSmall;
    private final StatusWindow        statusWindow;
    private final MinimapWindow       minimapWindow;
    private final KeyInventoryWindow  keyInventory;
    private final HudChipSlotWidget   chipSlots;
    private final InventoryScreen     inventoryScreen;

    public HudRenderer(ChipInventory chipInventory) {
        hudCamera  = new OrthographicCamera();

        fontMedium = new BitmapFont();
        fontMedium.getData().setScale(1.6f);

        fontSmall  = new BitmapFont();
        fontSmall.getData().setScale(0.9f);

        statusWindow    = new StatusWindow(fontMedium, fontSmall);
        minimapWindow   = new MinimapWindow(fontMedium, fontSmall);
        keyInventory    = new KeyInventoryWindow(fontMedium, fontSmall);
        chipSlots       = new HudChipSlotWidget(fontSmall);
        chipInventory.addObserver(chipSlots);
        inventoryScreen = new InventoryScreen(chipInventory, fontMedium, fontSmall);

        updateCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int width, int height) {
        updateCamera(width, height);
        inventoryScreen.resize(width, height);
    }

    private void updateCamera(int w, int h) {
        hudCamera.setToOrtho(false, w, h);
        hudCamera.update();
    }

    public void render(SpriteBatch batch, ShapeRenderer shapes, float dt) {
        float sw = hudCamera.viewportWidth;
        float sh = hudCamera.viewportHeight;

        statusWindow.update(dt);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ── texture background pass (behind shapes) ───────────────────────────
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        statusWindow.renderBackground(batch, 10f, sh - StatusWindow.H - 10f);
        minimapWindow.renderBackground(batch, sw - MinimapWindow.W - 10f, sh - MinimapWindow.H - 10f);
        keyInventory.renderBackground(batch, sw - KeyInventoryWindow.W - 10f, 10f);
        batch.end();

        // ── shape pass (procedural backgrounds + bars + icons) ────────────────
        shapes.setProjectionMatrix(hudCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        statusWindow.renderShapes(shapes, 10f, sh - StatusWindow.H - 10f);
        minimapWindow.renderShapes(shapes, sw - MinimapWindow.W - 10f, sh - MinimapWindow.H - 10f);
        keyInventory.renderShapes(shapes, sw - KeyInventoryWindow.W - 10f, 10f);
        chipSlots.renderShapes(shapes, sw, sh);

        shapes.end();

        // ── text + icon texture pass ──────────────────────────────────────────
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        statusWindow.renderText(batch, 10f, sh - StatusWindow.H - 10f);
        minimapWindow.renderText(batch, sw - MinimapWindow.W - 10f, sh - MinimapWindow.H - 10f);
        keyInventory.renderText(batch, sw - KeyInventoryWindow.W - 10f, 10f);
        chipSlots.renderText(batch, sw, sh);

        batch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── inventory overlay (own begin/end inside) ─────────────────────────
        if (inventoryScreen.isOpen()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.setProjectionMatrix(hudCamera.combined);
            batch.setProjectionMatrix(hudCamera.combined);
            inventoryScreen.render(batch, shapes);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    // ── inventory control ────────────────────────────────────────────────────

    public void toggleInventory() {
        inventoryScreen.toggle();
        EventBus.getInstance().post(new InventoryToggleEvent(inventoryScreen.isOpen()));
    }

    public void closeInventory() {
        inventoryScreen.close();
        EventBus.getInstance().post(new InventoryToggleEvent(false));
    }

    public boolean isInventoryOpen() {
        return inventoryScreen.isOpen();
    }

    public void handleInventoryClick(float rawX, float rawY) {
        inventoryScreen.handleClick(rawX, rawY);
    }

    /** Trigger an on-demand chip in the given 0-based slot (keys 1–4). */
    public void activateChipSlot(int slot) {
        chipSlots.activateSlot(slot);
    }

    public void dispose() {
        fontMedium.dispose();
        fontSmall.dispose();
        statusWindow.dispose();
        minimapWindow.dispose();
        keyInventory.dispose();
        chipSlots.dispose();
        inventoryScreen.dispose();
    }
}
