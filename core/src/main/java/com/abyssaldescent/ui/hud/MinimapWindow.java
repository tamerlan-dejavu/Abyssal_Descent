package com.abyssaldescent.ui.hud;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.RoomChangedEvent;
import com.abyssaldescent.event.TierChangedEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class MinimapWindow {

    public static final float W = 300f;
    public static final float H = 300f;

    private static final String ASSET_PATH = "ui/backgrounds/minimap.png";

    @SuppressWarnings("unused") private String tierName;
    private int    floorNumber;
    @SuppressWarnings("unused") private String roomId;
    private int    roomsCompleted;

    private final Texture      bgTexture;
    private final BitmapFont   font;
    private final GlyphLayout  glLayout = new GlyphLayout();

    private final EventListener<TierChangedEvent> tierListener = this::onTierChanged;
    private final EventListener<RoomChangedEvent> roomListener = this::onRoomChanged;

    public MinimapWindow(BitmapFont fontTitle, BitmapFont fontBody) {
        bgTexture = tryLoad(ASSET_PATH);
        font      = fontBody;
        int floor        = GameStateManager.getInstance().getFloorNumber();
        this.floorNumber = floor;
        this.tierName    = tierNameFor(floor);
        this.roomId      = "R-" + String.format("%02d", floor);
        this.roomsCompleted = 0;
        EventBus.getInstance().subscribe(TierChangedEvent.class, tierListener);
        EventBus.getInstance().subscribe(RoomChangedEvent.class, roomListener);
    }

    private void onTierChanged(TierChangedEvent e) {
        floorNumber     = e.getNewTier();
        tierName        = tierNameFor(floorNumber);
        roomId          = "R-" + String.format("%02d", floorNumber);
        roomsCompleted  = 0;
    }

    private void onRoomChanged(RoomChangedEvent e) {
        tierName    = e.getTierName();
        floorNumber = e.getFloorNumber();
        roomId      = e.getRoomId();
        roomsCompleted++;
    }

    public void renderBackground(SpriteBatch batch, float x, float y) {
        if (bgTexture == null) return;
        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(bgTexture, x, y, W, H);
        batch.setColor(Color.WHITE);
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        float[] pal = tierPalette(floorNumber);

        if (bgTexture == null) {
            shapes.setColor(0.04f, 0.04f, 0.1f, 0.88f);
            shapes.rect(x, y, W, H);
            float bw = 2f;
            shapes.setColor(pal[16], pal[17], pal[18], 0.4f);
            shapes.rect(x, y, W, bw);
            shapes.rect(x, y + H - bw, W, bw);
            shapes.rect(x, y, bw, H);
            shapes.rect(x + W - bw, y, bw, H);
        }

        float cx   = x + W * 0.5f;
        float cy   = y + H * 0.5f;
        float base = Math.min(W, H) * 0.38f * 0.7f;

        float gridX = cx - base - 7f;
        float gridY = cy - base + 25f;
        float gridW = base * 2f + 14f;
        float gridH = base * 2f - 15f;

        float cellW = gridW / 3f;
        float cellH = gridH / 3f;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                boolean isActive  = (row == 1 && col == 1);
                boolean isVisited = !isActive && ((row == 0 && col == 1) || (row == 1 && col == 0));
                if (!isActive && !isVisited) continue;

                float cellX = gridX + col * cellW;
                float cellY = gridY + row * cellH;

                int base4 = isActive ? 4 : 8;
                shapes.setColor(pal[base4], pal[base4 + 1], pal[base4 + 2], pal[base4 + 3]);
                shapes.rect(cellX + 1f, cellY + 1f, cellW - 2f, cellH - 2f);

                float ba = isActive ? 0.55f : 0.35f;
                shapes.setColor(pal[16], pal[17], pal[18], ba);
                shapes.line(cellX + 1f,          cellY + 1f,          cellX + cellW - 1f, cellY + 1f);
                shapes.line(cellX + 1f,          cellY + cellH - 1f,  cellX + cellW - 1f, cellY + cellH - 1f);
                shapes.line(cellX + 1f,          cellY + 1f,          cellX + 1f,         cellY + cellH - 1f);
                shapes.line(cellX + cellW - 1f,  cellY + 1f,          cellX + cellW - 1f, cellY + cellH - 1f);
            }
        }

        float dotSize = 6f;
        float dotX = gridX + cellW * 1.5f;
        float dotY = gridY + cellH * 1.5f;
        shapes.setColor(1f, 1f, 1f, 0.95f);
        shapes.rect(dotX - dotSize * 0.5f, dotY - dotSize * 0.5f, dotSize, dotSize);
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        if (font == null) return;
        font.getData().setScale(1.3f);
        font.setColor(0.55f, 0.55f, 0.55f, 1f);
        String label = String.valueOf(roomsCompleted);
        glLayout.setText(font, label);
        font.draw(batch, label, x + W - 104f - glLayout.width, y + H - 245f);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(TierChangedEvent.class, tierListener);
        EventBus.getInstance().unsubscribe(RoomChangedEvent.class, roomListener);
        if (bgTexture != null) bgTexture.dispose();
    }

    private static float[] tierPalette(int floor) {
        switch (floor) {
            case 1:
                return new float[]{
                    0.07f, 0.09f, 0.14f, 0.65f,
                    0.18f, 0.26f, 0.48f, 0.82f,
                    0.10f, 0.14f, 0.28f, 0.70f,
                    0.05f, 0.07f, 0.12f, 0.55f,
                    0.22f, 0.34f, 0.58f, 1.00f
                };
            case 2:
                return new float[]{
                    0.05f, 0.07f, 0.14f, 0.65f,
                    0.15f, 0.26f, 0.52f, 0.82f,
                    0.08f, 0.13f, 0.30f, 0.70f,
                    0.04f, 0.06f, 0.12f, 0.55f,
                    0.20f, 0.36f, 0.62f, 1.00f
                };
            case 3:
                return new float[]{
                    0.08f, 0.06f, 0.16f, 0.65f,
                    0.28f, 0.18f, 0.52f, 0.82f,
                    0.14f, 0.09f, 0.28f, 0.70f,
                    0.06f, 0.04f, 0.12f, 0.55f,
                    0.24f, 0.28f, 0.60f, 1.00f
                };
            default:
                return new float[]{
                    0.07f, 0.08f, 0.13f, 0.65f,
                    0.18f, 0.22f, 0.44f, 0.82f,
                    0.09f, 0.11f, 0.22f, 0.70f,
                    0.05f, 0.05f, 0.10f, 0.55f,
                    0.20f, 0.30f, 0.56f, 1.00f
                };
        }
    }

    private static String tierNameFor(int floor) {
        switch (floor) {
            case 1: return "Upper Ruins";
            case 2: return "Sunken Crypts";
            case 3: return "Void Core";
            default: return "Floor " + floor;
        }
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
