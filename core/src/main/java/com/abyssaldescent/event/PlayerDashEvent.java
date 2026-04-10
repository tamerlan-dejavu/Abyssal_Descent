package com.abyssaldescent.event;

/**
 * Fired when Karin performs a dash (dodge-roll).
 * Can be used by audio/VFX systems to play the dash effect.
 */
public final class PlayerDashEvent extends GameEvent {

    private final float originX;
    private final float originY;
    private final float directionX;
    private final float directionY;

    public PlayerDashEvent(float originX, float originY,
                           float directionX, float directionY) {
        this.originX = originX;
        this.originY = originY;
        this.directionX = directionX;
        this.directionY = directionY;
    }

    public float getOriginX() { return originX; }
    public float getOriginY() { return originY; }
    public float getDirectionX() { return directionX; }
    public float getDirectionY() { return directionY; }
}
