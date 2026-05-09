package com.abyssaldescent.event;

public final class EnemyDeathEvent extends GameEvent {
    private final String enemyId;
    private final String enemyType;
    private final float x;
    private final float y;

    public EnemyDeathEvent(String enemyId, String enemyType, float x, float y) {
        this.enemyId = enemyId;
        this.enemyType = enemyType;
        this.x = x;
        this.y = y;
    }

    public String getEnemyId() { return enemyId; }
    public String getEnemyType() { return enemyType; }
    public float getX() { return x; }
    public float getY() { return y; }
}
