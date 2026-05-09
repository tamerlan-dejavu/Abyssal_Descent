package com.abyssaldescent.entity.enemy.ai.strategy;

import com.abyssaldescent.entity.enemy.EnemyContext;

public final class StationaryStrategy implements EnemyStrategy {

    public static final float SHARD_COOLDOWN = 3.0f;

    @Override
    public void updateChase(EnemyContext ctx, float dt) {
        float dx = ctx.getTargetPosition().x - ctx.getPosition().x;
        float dy = ctx.getTargetPosition().y - ctx.getPosition().y;
        ctx.setVelocity(0, 0);
        ctx.setFacing(dx, dy);
    }

    @Override
    public int performAttack(EnemyContext ctx) {
        return ctx.getType().getDamage();
    }

    @Override
    public float getEngagementRange(EnemyContext ctx) {
        return ctx.getType().getAttackRange();
    }

    @Override
    public float getAttackCooldown() {
        return SHARD_COOLDOWN;
    }

    @Override
    public String getName() { return "Stationary"; }
}
