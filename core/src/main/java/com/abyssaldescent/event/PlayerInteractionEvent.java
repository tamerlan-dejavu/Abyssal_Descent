package com.abyssaldescent.event;

public final class PlayerInteractionEvent extends GameEvent {
    private final float playerX;
    private final float playerY;
    private final float interactionRange;

    public PlayerInteractionEvent(float playerX, float playerY, float interactionRange) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.interactionRange = interactionRange;
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public float getInteractionRange() {
        return interactionRange;
    }
}
