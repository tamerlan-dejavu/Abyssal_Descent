package com.abyssaldescent.event;

public final class PlayerDamagedEvent extends GameEvent {
    private final int   amount;
    private final int   currentHp;
    private final int   maxHp;
    private final float worldX;
    private final float worldY;
    private final float directionX;

    public PlayerDamagedEvent(int amount, int currentHp, int maxHp,
                              float worldX, float worldY, float directionX) {
        this.amount     = amount;
        this.currentHp  = currentHp;
        this.maxHp      = maxHp;
        this.worldX     = worldX;
        this.worldY     = worldY;
        this.directionX = directionX;
    }

    public int   getAmount()     { return amount; }
    public int   getCurrentHp()  { return currentHp; }
    public int   getMaxHp()      { return maxHp; }
    public float getWorldX()     { return worldX; }
    public float getWorldY()     { return worldY; }
    public float getDirectionX() { return directionX; }
}
