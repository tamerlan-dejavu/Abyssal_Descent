package com.abyssaldescent.event;


public final class PlayerAttackEvent extends GameEvent {

    private final float originX;
    private final float originY;
    private final float directionX;
    private final float directionY;
    private final float range;

    public PlayerAttackEvent(float originX, float originY, float directionX, float directionY, float range) {
        this.originX = originX;
        this.originY = originY;
        this.directionX = directionX;
        this.directionY = directionY;
        this.range = range;
    }

    public float getOriginX() { return originX; }
    public float getOriginY() { return originY; }
    public float getDirectionX() { return directionX; }
    public float getDirectionY() { return directionY; }
    public float getRange() { return range; }
}
