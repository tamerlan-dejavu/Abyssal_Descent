package com.abyssaldescent.event;

public final class AoEDamageEvent extends GameEvent {
    private final float originX;
    private final float originY;
    private final float radius;
    private final int damage;
    private final String sourceId;

    public AoEDamageEvent(float originX, float originY, float radius, int damage, String sourceId) {
        this.originX  = originX;
        this.originY  = originY;
        this.radius   = radius;
        this.damage   = damage;
        this.sourceId = sourceId;
    }

    public float getOriginX()  { return originX; }
    public float getOriginY()  { return originY; }
    public float getRadius()   { return radius; }
    public int getDamage()     { return damage; }
    public String getSourceId(){ return sourceId; }
}
