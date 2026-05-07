package com.abyssaldescent.ui.hud;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.RoomChangedEvent;
import com.abyssaldescent.event.TierChangedEvent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class MinimapWindow {

    public static final float W = 500f;
    public static final float H = 500f;

    private static final float PAD = 14f;

    private String tierName;
    private int    floorNumber;
    private String roomId;

    private final BitmapFont fontTitle;
    private final BitmapFont fontBody;

    private final EventListener<TierChangedEvent>  tierListener = this::onTierChanged;
    private final EventListener<RoomChangedEvent>  roomListener = this::onRoomChanged;

    public MinimapWindow(BitmapFont fontTitle, BitmapFont fontBody) {
        this.fontTitle   = fontTitle;
        this.fontBody    = fontBody;
        int floor        = GameStateManager.getInstance().getFloorNumber();
        this.floorNumber = floor;
        this.tierName    = tierNameFor(floor);
        this.roomId      = "R-" + String.format("%02d", floor);
        EventBus.getInstance().subscribe(TierChangedEvent.class, tierListener);
        EventBus.getInstance().subscribe(RoomChangedEvent.class, roomListener);
    }

    private void onTierChanged(TierChangedEvent e) {
        floorNumber = e.getNewTier();
        tierName    = tierNameFor(floorNumber);
        roomId      = "R-" + String.format("%02d", floorNumber);
    }

    private void onRoomChanged(RoomChangedEvent e) {
        tierName    = e.getTierName();
        floorNumber = e.getFloorNumber();
        roomId      = e.getRoomId();
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        shapes.setColor(0.04f, 0.04f, 0.1f, 0.88f);
        shapes.rect(x, y, W, H);

        float bw = 2f;
        shapes.setColor(0.15f, 0.15f, 0.35f, 0.4f);
        shapes.rect(x, y, W, bw);
        shapes.rect(x, y + H - bw, W, bw);
        shapes.rect(x, y, bw, H);
        shapes.rect(x + W - bw, y, bw, H);

        float cx    = x + W * 0.5f;
        float cy    = y + H * 0.5f - 30f;
        float inner = Math.min(W, H) * 0.3f;

        shapes.setColor(0.08f, 0.08f, 0.18f, 1f);
        shapes.rect(cx - inner, cy - inner, inner * 2f, inner * 2f);

        shapes.setColor(0.12f, 0.12f, 0.28f, 1f);
        float cellW = inner * 2f / 3f;
        float cellH = inner * 2f / 3f;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if ((row == 1 && col == 1) || (row == 0 && col == 1) || (row == 1 && col == 0)) {
                    shapes.setColor(0.18f, 0.18f, 0.42f, 1f);
                } else {
                    shapes.setColor(0.09f, 0.09f, 0.2f, 1f);
                }
                shapes.rect(cx - inner + col * cellW + 1f, cy - inner + row * cellH + 1f,
                        cellW - 2f, cellH - 2f);
            }
        }

        float dotSize = 8f;
        shapes.setColor(0f, 0.9f, 1f, 1f);
        shapes.rect(cx - dotSize * 0.5f, cy - dotSize * 0.5f, dotSize, dotSize);

        float sepY = y + H - 155f;
        shapes.setColor(0.18f, 0.18f, 0.4f, 0.6f);
        shapes.rect(x + PAD, sepY, W - PAD * 2f, 1f);
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        fontTitle.getData().setScale(2.0f);
        fontTitle.setColor(0.55f, 0.55f, 1f, 1f);
        fontTitle.draw(batch, "MAP", x + PAD, y + H - 20f);

        fontBody.getData().setScale(1.55f);
        fontBody.setColor(0f, 0.85f, 1f, 1f);
        fontBody.draw(batch, tierName, x + PAD, y + H - 68f);

        fontBody.getData().setScale(1.4f);
        fontBody.setColor(Color.LIGHT_GRAY);
        fontBody.draw(batch, "Floor:  " + floorNumber, x + PAD, y + H - 110f);
        fontBody.draw(batch, "Room:  " + roomId, x + PAD, y + H - 138f);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(TierChangedEvent.class, tierListener);
        EventBus.getInstance().unsubscribe(RoomChangedEvent.class, roomListener);
    }

    private static String tierNameFor(int floor) {
        switch (floor) {
            case 1: return "Upper Ruins";
            case 2: return "Sunken Crypts";
            case 3: return "Void Core";
            default: return "Floor " + floor;
        }
    }
}
