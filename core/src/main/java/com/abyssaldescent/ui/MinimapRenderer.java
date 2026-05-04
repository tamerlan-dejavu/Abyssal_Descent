package com.abyssaldescent.ui;

import com.abyssaldescent.world.Dungeon;
import com.abyssaldescent.world.Room;
import com.abyssaldescent.world.RoomType;
import com.abyssaldescent.world.Tier;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders a small top-left minimap showing visited rooms in the current tier.
 *
 * Each room is placed on a fixed grid defined by a layout map keyed on room ID.
 * Current room → yellow, visited → room-type colour, unvisited → very dark.
 */
public final class MinimapRenderer {

    private static final int MARGIN   = 8;
    private static final int CELL_W   = 20;
    private static final int CELL_H   = 11;
    private static final int GAP      = 2;
    private static final int PADDING  = 4;

    private final Dungeon dungeon;
    private final Map<String, int[]> layout = new HashMap<>();

    public MinimapRenderer(Dungeon dungeon) {
        this.dungeon = dungeon;
        buildLayouts();
    }

    private void buildLayouts() {
        // Tier 1
        layout.put("t1_start",     new int[]{0, 1});
        layout.put("t1_treasure1", new int[]{0, 0});
        layout.put("t1_combat1",   new int[]{1, 1});
        layout.put("t1_combat2",   new int[]{2, 1});
        layout.put("t1_rest1",     new int[]{3, 1});
        layout.put("t1_combat3",   new int[]{1, 2});
        layout.put("t1_treasure2", new int[]{2, 2});
        layout.put("t1_combat4",   new int[]{3, 2});
        layout.put("t1_secret1",   new int[]{1, 3});
        layout.put("t1_rest2",     new int[]{3, 3});
        layout.put("t1_boss",      new int[]{4, 2});

        // Tier 2
        layout.put("t2_entrance",  new int[]{0, 0});
        layout.put("t2_combat1",   new int[]{1, 0});
        layout.put("t2_puzzle1",   new int[]{2, 0});
        layout.put("t2_combat2",   new int[]{3, 0});
        layout.put("t2_rest1",     new int[]{4, 0});
        layout.put("t2_combat3",   new int[]{1, 1});
        layout.put("t2_shop1",     new int[]{2, 1});
        layout.put("t2_treasure1", new int[]{3, 1});
        layout.put("t2_combat4",   new int[]{4, 1});
        layout.put("t2_combat5",   new int[]{5, 1});
        layout.put("t2_rest2",     new int[]{5, 2});
        layout.put("t2_boss",      new int[]{6, 1});

        // Tier 3
        layout.put("t3_entrance",  new int[]{0, 0});
        layout.put("t3_combat1",   new int[]{1, 0});
        layout.put("t3_combat2",   new int[]{2, 0});
        layout.put("t3_rest1",     new int[]{3, 0});
        layout.put("t3_combat3",   new int[]{4, 0});
        layout.put("t3_combat4",   new int[]{1, 1});
        layout.put("t3_shop1",     new int[]{2, 1});
        layout.put("t3_combat5",   new int[]{4, 1});
        layout.put("t3_combat6",   new int[]{4, 2});
        layout.put("t3_rest2",     new int[]{1, 2});
        layout.put("t3_treasure1", new int[]{2, 2});
        layout.put("t3_secret2",   new int[]{2, 3});
        layout.put("t3_boss",      new int[]{5, 1});
    }

    /**
     * Draws the minimap. Must be called with shapes using the UI (screen-space) camera.
     * Does not call shapes.begin/end — caller manages that if batching other UI draws.
     * Actually this method manages its own begin/end pairs for each ShapeType needed.
     */
    public void render(ShapeRenderer shapes, OrthographicCamera uiCamera, Room currentRoom) {
        if (currentRoom == null) return;

        shapes.setProjectionMatrix(uiCamera.combined);

        Tier tier = currentRoom.getTier();
        List<Room> tierRooms = dungeon.getRoomsByTier(tier);

        int maxCol = 0, maxRow = 0;
        for (Room r : tierRooms) {
            int[] pos = layout.get(r.getId());
            if (pos != null) {
                if (pos[0] > maxCol) maxCol = pos[0];
                if (pos[1] > maxRow) maxRow = pos[1];
            }
        }

        int panelW = (maxCol + 1) * (CELL_W + GAP) - GAP + PADDING * 2;
        int panelH = (maxRow + 1) * (CELL_H + GAP) - GAP + PADDING * 2;
        int screenH = Gdx.graphics.getHeight();

        // Background panel
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.70f);
        shapes.rect(MARGIN - PADDING, screenH - MARGIN - panelH, panelW, panelH);

        // Room fills
        for (Room r : tierRooms) {
            int[] pos = layout.get(r.getId());
            if (pos == null) continue;
            int cx = MARGIN + pos[0] * (CELL_W + GAP);
            int cy = screenH - MARGIN - (pos[1] + 1) * (CELL_H + GAP) + GAP;
            if (r.getId().equals(currentRoom.getId())) {
                shapes.setColor(0.95f, 0.88f, 0.20f, 1f);
            } else if (r.isVisited()) {
                Color c = colorFor(r.getType());
                shapes.setColor(c);
            } else {
                shapes.setColor(0.12f, 0.12f, 0.12f, 1f);
            }
            shapes.rect(cx, cy, CELL_W, CELL_H);
        }
        shapes.end();

        // Room outlines + bright border + player dot for current room
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (Room r : tierRooms) {
            int[] pos = layout.get(r.getId());
            if (pos == null) continue;
            int cx = MARGIN + pos[0] * (CELL_W + GAP);
            int cy = screenH - MARGIN - (pos[1] + 1) * (CELL_H + GAP) + GAP;
            if (r.getId().equals(currentRoom.getId())) {
                shapes.setColor(1f, 1f, 1f, 1f);
                shapes.rect(cx - 1, cy - 1, CELL_W + 2, CELL_H + 2);
            } else {
                shapes.setColor(0.45f, 0.45f, 0.45f, 1f);
            }
            shapes.rect(cx, cy, CELL_W, CELL_H);
        }
        shapes.end();

        // Player dot in center of current room cell
        int[] curPos = layout.get(currentRoom.getId());
        if (curPos != null) {
            int cx = MARGIN + curPos[0] * (CELL_W + GAP) + CELL_W / 2;
            int cy = screenH - MARGIN - (curPos[1] + 1) * (CELL_H + GAP) + GAP + CELL_H / 2;
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 1f, 1f, 1f);
            shapes.circle(cx, cy, 3f, 8);
            shapes.end();
        }
    }

    private static Color colorFor(RoomType type) {
        switch (type) {
            case START:    return new Color(0.30f, 0.60f, 0.30f, 1f);
            case COMBAT:   return new Color(0.50f, 0.20f, 0.20f, 1f);
            case BOSS:     return new Color(0.70f, 0.10f, 0.10f, 1f);
            case REST:     return new Color(0.20f, 0.40f, 0.60f, 1f);
            case TREASURE: return new Color(0.60f, 0.50f, 0.10f, 1f);
            case SHOP:     return new Color(0.40f, 0.30f, 0.60f, 1f);
            case PUZZLE:   return new Color(0.30f, 0.40f, 0.50f, 1f);
            case CORRIDOR: return new Color(0.25f, 0.25f, 0.25f, 1f);
            default:       return new Color(0.30f, 0.30f, 0.30f, 1f);
        }
    }
}
