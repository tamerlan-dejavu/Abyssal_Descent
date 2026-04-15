package com.abyssaldescent.entity.enemy.ai;

import com.abyssaldescent.entity.enemy.EnemyContext;

public final class GravityStrategy implements EnemyStrategy {
    public static final float PULL_RADIUS = 3f;
    public static final float PULL_STRENGTH = 0.6f;

    @Override
    public void updateChase(EnemyContext ctx, float dt) {
        ctx.setVelocity(0, 0);
    }

    @Override
    public int performAttack(EnemyContext ctx) {
        return ctx.getType().getDamage();
    }

    @Override
    public float getEngagementRange(EnemyContext ctx) {
        return PULL_RADIUS;
    }

    @Override
    public String getName() { return "Gravity"; }

    public float getPullStrength() { return PULL_STRENGTH; }
}
