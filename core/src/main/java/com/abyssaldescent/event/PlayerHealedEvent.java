package com.abyssaldescent.event;

public final class PlayerHealedEvent extends GameEvent {
    private final int   amount;
    private final int   currentHp;
    private final int   maxHp;
    private final float worldX;
    private final float worldY;

    public PlayerHealedEvent(int amount, int currentHp, int maxHp,
                             float worldX, float worldY) {
        this.amount    = amount;
        this.currentHp = currentHp;
        this.maxHp     = maxHp;
        this.worldX    = worldX;
        this.worldY    = worldY;
    }

    public int   getAmount()    { return amount; }
    public int   getCurrentHp() { return currentHp; }
    public int   getMaxHp()     { return maxHp; }
    public float getWorldX()    { return worldX; }
    public float getWorldY()    { return worldY; }
}
