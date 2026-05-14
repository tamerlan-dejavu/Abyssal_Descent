package com.abyssaldescent.event;

public final class EnemyAttackEvent extends GameEvent {
    private final String enemyId;
    private final float x;
    private final float y;
    private final int damage;

    public EnemyAttackEvent(String enemyId, float x, float y, int damage) {
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.damage = damage;
    }

    public String getEnemyId() { return enemyId; }
    public float getX()        { return x; }
    public float getY()        { return y; }
    public int getDamage()     { return damage; }
}
