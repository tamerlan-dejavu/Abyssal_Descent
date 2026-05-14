package com.abyssaldescent.entity.enemy;

import com.badlogic.gdx.math.Vector2;

public final class EnemyAttackEffect {

    private final Vector2 position = new Vector2();
    private final float duration;
    private float elapsedTime;
    private boolean active;

    public EnemyAttackEffect(float x, float y, float duration) {
        this.position.set(x, y);
        this.duration = duration;
        this.elapsedTime = 0f;
        this.active = true;
    }

    public void update(float delta) {
        if (!active) return;
        elapsedTime += delta;
        if (elapsedTime >= duration) {
            active = false;
        }
    }

    public float getX()                { return position.x; }
    public float getY()                { return position.y; }
    public boolean isActive()          { return active; }
    public float getProgress()         { return Math.min(1f, elapsedTime / duration); }
    public float getAlpha()            { return 1f - getProgress(); }
}
