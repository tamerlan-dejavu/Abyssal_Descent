package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Shared asset utilities for menu screens.
 */
final class ScreenAssets {

    private ScreenAssets() {}

    /**
     * Loads a background texture from {@code path} inside the assets folder.
     * Falls back to a solid 1×1 texture using the given RGB byte values if the
     * file does not exist or fails to load.
     */
    static Texture loadBackground(String path, int r, int g, int b) {
        if (Gdx.files.internal(path).exists()) {
            try {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return t;
            } catch (Exception e) {
                Gdx.app.error("ScreenAssets", "Failed to load background: " + path, e);
            }
        } else {
            Gdx.app.log("ScreenAssets", "Background not found: " + path + " — using solid colour.");
        }
        return solidColour(r, g, b);
    }

    private static Texture solidColour(int r, int g, int b) {
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGB888);
        px.setColor(r / 255f, g / 255f, b / 255f, 1f);
        px.fill();
        Texture t = new Texture(px);
        px.dispose();
        return t;
    }
}
