package com.abyssaldescent.ui.hud;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.dungeon.Door;
import com.abyssaldescent.dungeon.DungeonManager;
import com.abyssaldescent.dungeon.Room;
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

import java.util.List;

public final class MinimapWindow {

    public static final float W = 300f;
    public static final float H = 300f;

    private static final String ASSET_PATH  = "ui/backgrounds/minimap.png";
    private static final float  CELL_SIZE   = 20f;
    private static final float  CELL_GAP    = 4f;
    private static final float  CELL_STEP   = CELL_SIZE + CELL_GAP;

    @SuppressWarnings("unused") private String tierName;
    private int    floorNumber;
    private int    roomsVisited;
    private String currentRoomId = "";

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
        this.roomsVisited = 1;  // start room is already visited
        EventBus.getInstance().subscribe(TierChangedEvent.class, tierListener);
        EventBus.getInstance().subscribe(RoomChangedEvent.class, roomListener);
    }

    private void onTierChanged(TierChangedEvent e) {
        floorNumber     = e.getNewTier();
        tierName        = tierNameFor(floorNumber);
        roomsVisited    = 1;  // start room is already visited
        currentRoomId   = "";
    }

    private void onRoomChanged(RoomChangedEvent e) {
        tierName      = e.getTierName();
        floorNumber   = e.getFloorNumber();
        currentRoomId = e.getRoomId();

        // Only increment if this is a new room
        DungeonManager mgr = DungeonManager.getInstance();
        Room room = mgr.getGraph().getRoom(currentRoomId);
        if (room != null && !room.isVisited()) {
            room.setVisited(true);
            roomsVisited++;
        }
    }

    public void renderBackground(SpriteBatch batch, float x, float y) {
        if (bgTexture == null) return;
        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(bgTexture, x, y, W, H);
        batch.setColor(Color.WHITE);
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        float[] pal = tierPalette(floorNumber);

        // Background panel
        if (bgTexture == null) {
            shapes.setColor(0.04f, 0.04f, 0.1f, 0.88f);
            shapes.rect(x, y, W, H);
            float bw = 2f;
            shapes.setColor(pal[16], pal[17], pal[18], 0.4f);
            shapes.rect(x, y,       W,  bw);
            shapes.rect(x, y+H-bw,  W,  bw);
            shapes.rect(x, y,       bw, H);
            shapes.rect(x+W-bw, y,  bw, H);
        }

        DungeonManager mgr = DungeonManager.getInstance();
        if (mgr.getGraph() == null) return;

        List<Room> allRooms = mgr.getGraph().getAllRooms();

        // Center of minimap panel
        float cx = x + W * 0.5f;
        float cy = y + H * 0.5f;

        // Find current room grid pos to center map on it
        int originGX = 0, originGY = 0;
        Room current = mgr.getGraph().getRoom(currentRoomId);
        if (current != null) {
            originGX = current.getGridX();
            originGY = current.getGridY();
        }

        // Draw connection lines first (under room cells)
        for (Room room : allRooms) {
            boolean isCurrent = room.getId().equals(currentRoomId);
            boolean isVisible = isCurrent || room.isVisited();
            if (!isVisible) continue;

            float rx = cx + (room.getGridX() - originGX) * CELL_STEP;
            float ry = cy + (room.getGridY() - originGY) * CELL_STEP;

            for (Door door : room.getDoors()) {
                Room target = mgr.getGraph().getRoom(door.getToRoomId());
                if (target == null) continue;

                boolean targetCurrent = target.getId().equals(currentRoomId);
                boolean targetVisible = targetCurrent || target.isVisited();
                if (!targetVisible) continue;

                float tx = cx + (target.getGridX() - originGX) * CELL_STEP;
                float ty = cy + (target.getGridY() - originGY) * CELL_STEP;
                shapes.setColor(pal[16], pal[17], pal[18], 0.4f);
                shapes.line(rx, ry, tx, ty);
            }
        }

        // Draw room cells
        for (Room room : allRooms) {
            boolean isCurrent = room.getId().equals(currentRoomId);
            boolean isVisible = isCurrent || room.isVisited();
            if (!isVisible) continue;

            float rx = cx + (room.getGridX() - originGX) * CELL_STEP;
            float ry = cy + (room.getGridY() - originGY) * CELL_STEP;

            // Clip rooms outside panel
            if (rx < x + 4f || rx > x + W - 4f || ry < y + 4f || ry > y + H - 4f) continue;

            float half = CELL_SIZE * 0.5f;
            if (isCurrent) {
                shapes.setColor(1f, 1f, 0.2f, 1f);
                shapes.rect(rx - half - 2f, ry - half - 2f, CELL_SIZE + 4f, CELL_SIZE + 4f);
                shapes.setColor(pal[4], pal[5], pal[6], 1f);
            } else {
                shapes.setColor(pal[8], pal[9], pal[10], 0.8f);
            }
            shapes.rect(rx - half, ry - half, CELL_SIZE, CELL_SIZE);

            // Door icon for FINAL room
            if (room.getType() == com.abyssaldescent.dungeon.RoomType.FINAL) {
                shapes.setColor(1f, 0.4f, 0.1f, 0.9f);
                shapes.rect(rx - half + 3f, ry - half + 3f, CELL_SIZE - 6f, CELL_SIZE - 6f);
            }
        }
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        if (font == null) return;
        font.getData().setScale(1.3f);
        font.setColor(0.55f, 0.55f, 0.55f, 1f);
        String label = String.valueOf(roomsVisited);
        glLayout.setText(font, label);
        font.draw(batch, label, x + W - 104f - glLayout.width, y + H - 245f);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(TierChangedEvent.class, tierListener);
        EventBus.getInstance().unsubscribe(RoomChangedEvent.class, roomListener);
        if (bgTexture != null) bgTexture.dispose();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

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
            case 2: return "Flooded Catacombs";
            case 3: return "Maltarion's Abyss";
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
