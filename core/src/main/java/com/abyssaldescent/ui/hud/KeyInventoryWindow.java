package com.abyssaldescent.ui.hud;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.KeyPickedUpEvent;
import com.abyssaldescent.event.RoomChangedEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class KeyInventoryWindow {

    public static final float W = 250f;
    public static final float H = 250f;

    private static final String ASSET_BG   = "ui/hud/keys_window.png";
    private static final String ASSET_ICON = "ui/hud/key_icon.png";

    private static final float PAD      = 12f;
    private static final float KEY_SIZE = 28f;

    private int currentKeys = 0;
    private int totalKeys   = 1;

    private final Texture    bgTexture;
    private final Texture    keyIconTexture;
    private final BitmapFont fontTitle;
    private final BitmapFont fontBody;

    private final EventListener<KeyPickedUpEvent> keyListener  = this::onKeyPickup;
    private final EventListener<RoomChangedEvent> roomListener = this::onRoomChanged;

    public KeyInventoryWindow(BitmapFont fontTitle, BitmapFont fontBody) {
        this.fontTitle     = fontTitle;
        this.fontBody      = fontBody;
        bgTexture          = tryLoad(ASSET_BG);
        keyIconTexture     = tryLoad(ASSET_ICON);
        EventBus.getInstance().subscribe(KeyPickedUpEvent.class, keyListener);
        EventBus.getInstance().subscribe(RoomChangedEvent.class, roomListener);
    }

    private void onKeyPickup(KeyPickedUpEvent e) {
        currentKeys = e.getCurrentKeys();
        totalKeys   = e.getTotalKeys();
    }

    private void onRoomChanged(RoomChangedEvent e) {
        currentKeys = 0;
        totalKeys   = 1;
    }

    public void renderBackground(SpriteBatch batch, float x, float y) {
        if (bgTexture == null) return;
        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(bgTexture, x, y, W, H);
        batch.setColor(Color.WHITE);
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        if (bgTexture == null) {
            shapes.setColor(0.04f, 0.04f, 0.1f, 0.88f);
            shapes.rect(x, y, W, H);
            float bw = 2f;
            shapes.setColor(0.15f, 0.15f, 0.35f, 0.4f);
            shapes.rect(x, y, W, bw);
            shapes.rect(x, y + H - bw, W, bw);
            shapes.rect(x, y, bw, H);
            shapes.rect(x + W - bw, y, bw, H);
            float sepY = y + H - 55f;
            shapes.setColor(0.18f, 0.18f, 0.4f, 0.6f);
            shapes.rect(x + PAD, sepY, W - PAD * 2f, 1f);
        }

        if (keyIconTexture == null) {
            boolean hasKey = currentKeys >= totalKeys && totalKeys > 0;
            float kx = x + W * 0.5f - KEY_SIZE * 0.5f;
            float ky = y + H * 0.5f - KEY_SIZE * 0.5f - 10f;
            shapes.setColor(hasKey ? new Color(1f, 0.85f, 0f, 1f) : new Color(0.3f, 0.3f, 0.1f, 1f));
            shapes.rect(kx, ky, KEY_SIZE, KEY_SIZE * 0.35f);
            shapes.circle(kx + KEY_SIZE * 0.5f, ky + KEY_SIZE * 0.35f + KEY_SIZE * 0.2f,
                    KEY_SIZE * 0.22f, 16);
            if (hasKey) {
                shapes.setColor(0.04f, 0.04f, 0.1f, 1f);
                shapes.circle(kx + KEY_SIZE * 0.5f, ky + KEY_SIZE * 0.35f + KEY_SIZE * 0.2f,
                        KEY_SIZE * 0.1f, 12);
            }
        }
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        if (keyIconTexture != null) {
            boolean hasKey = currentKeys >= totalKeys && totalKeys > 0;
            float iconSize = 56f;
            float kx = x + W * 0.5f - iconSize * 0.5f;
            float ky = y + H * 0.5f - iconSize * 0.5f - 10f;
            batch.setColor(hasKey ? Color.YELLOW : Color.DARK_GRAY);
            batch.draw(keyIconTexture, kx, ky, iconSize, iconSize);
            batch.setColor(Color.WHITE);
        }

        fontTitle.getData().setScale(2.0f);
        fontTitle.setColor(0.55f, 0.55f, 1f, 1f);
        fontTitle.draw(batch, "KEYS", x + PAD, y + H - 20f);

        fontBody.getData().setScale(1.55f);
        boolean hasKey = currentKeys >= totalKeys && totalKeys > 0;
        fontBody.setColor(hasKey ? Color.YELLOW : Color.GRAY);
        fontBody.draw(batch, currentKeys + " / " + totalKeys,
                x + W * 0.5f - 24f, y + PAD + 30f);

        fontBody.getData().setScale(1.2f);
        fontBody.setColor(hasKey ? new Color(0.8f, 1f, 0.6f, 1f) : new Color(0.5f, 0.5f, 0.5f, 1f));
        fontBody.draw(batch, hasKey ? "Ready" : "Missing",
                x + W * 0.5f - (hasKey ? 22f : 30f), y + PAD + 14f);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(KeyPickedUpEvent.class, keyListener);
        EventBus.getInstance().unsubscribe(RoomChangedEvent.class, roomListener);
        if (bgTexture != null)     bgTexture.dispose();
        if (keyIconTexture != null) keyIconTexture.dispose();
    }

    private static Texture tryLoad(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return t;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
