package com.abyssaldescent.ui.hud;

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
    private final HealthBarWidget    healthBar;

    public HudRenderer() {
        hudCamera  = new OrthographicCamera();
        fontMedium = new BitmapFont();
        fontMedium.getData().setScale(1.6f);
        healthBar  = new HealthBarWidget(fontMedium);
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
        float sh = hudCamera.viewportHeight;
        healthBar.syncFromGameState();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(hudCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        healthBar.renderShapes(shapes, 20f, sh - 60f);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        healthBar.renderText(batch, 20f, sh - 60f);
        batch.end();
    }

    public void dispose() {
        fontMedium.dispose();
        healthBar.dispose();
    }
}
