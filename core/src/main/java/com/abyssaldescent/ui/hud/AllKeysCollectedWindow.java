package com.abyssaldescent.ui.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class AllKeysCollectedWindow {

    private static final float DURATION = 3.0f;
    private float elapsedTime = 0f;
    private boolean active = false;

    private final BitmapFont font;
    private final GlyphLayout glLayout = new GlyphLayout();
    private final Texture windowTexture;

    public AllKeysCollectedWindow(BitmapFont font) {
        this.font = font;
        this.windowTexture = loadWindowTexture();
    }

    private Texture loadWindowTexture() {
        if (Gdx.files.internal("keys_collected_window.png").exists()) {
            try {
                Texture t = new Texture(Gdx.files.internal("keys_collected_window.png"));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return t;
            } catch (Exception e) {
                Gdx.app.log("AllKeysCollectedWindow", "Could not load keys_collected_window.png");
            }
        }
        return null;
    }

    public void show() {
        this.active = true;
        this.elapsedTime = 0f;
    }

    public void update(float delta) {
        if (!active) return;
        elapsedTime += delta;
        if (elapsedTime >= DURATION) {
            active = false;
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapes, int screenWidth, int screenHeight) {
        if (!active) return;

        float progress = elapsedTime / DURATION;
        float alpha = Math.max(0f, 1f - progress);  // fade out

        // Draw semi-transparent background blur effect
        shapes.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight));
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.5f * alpha);
        shapes.rect(0, 0, screenWidth, screenHeight);
        shapes.end();

        batch.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight));
        batch.begin();

        // Draw window - use texture if available, otherwise shapes
        float windowWidth = screenWidth;
        float windowHeight = screenHeight * 0.5f;
        float windowX = 0;
        float windowY = screenHeight * 0.25f;

        if (windowTexture != null) {
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(windowTexture, windowX, windowY, windowWidth, windowHeight);
        } else {
            batch.end();
            shapes.setProjectionMatrix(new com.badlogic.gdx.math.Matrix4().setToOrtho2D(0, 0, screenWidth, screenHeight));
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.1f, 0.05f, 0.2f, 0.9f * alpha);
            shapes.rect(windowX, windowY, windowWidth, windowHeight);
            shapes.setColor(1f, 0.8f, 0.2f, alpha);
            shapes.rect(windowX, windowY, windowWidth, 4f);
            shapes.rect(windowX, windowY + windowHeight - 4f, windowWidth, 4f);
            shapes.end();
            batch.begin();
        }

        // Draw text
        font.getData().setScale(2.5f);
        font.setColor(1f, 0.8f, 0.2f, alpha);
        String message = "ALL KEYS COLLECTED!";
        glLayout.setText(font, message);
        float textX = (screenWidth - glLayout.width) * 0.5f;
        float textY = (screenHeight * 0.5f) + (glLayout.height * 0.5f);
        font.draw(batch, message, textX, textY);

        String subMessage = "You can now enter the Final Chamber";
        font.getData().setScale(1.6f);
        font.setColor(0.8f, 0.8f, 0.8f, alpha * 0.8f);
        glLayout.setText(font, subMessage);
        float subX = (screenWidth - glLayout.width) * 0.5f;
        float subY = textY - 60f;
        font.draw(batch, subMessage, subX, subY);
        batch.end();
    }

    public void dispose() {
        if (windowTexture != null) windowTexture.dispose();
    }

    public boolean isActive() {
        return active;
    }
}
