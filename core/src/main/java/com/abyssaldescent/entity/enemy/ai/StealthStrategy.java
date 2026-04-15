package com.abyssaldescent.entity.enemy.ai;

import com.abyssaldescent.entity.enemy.EnemyContext;

public final class StealthStrategy implements EnemyStrategy {
    public static final float AMBUSH_RANGE = 2.5f;
    public static final float AMBUSH_MULTIPLIER = 2.0f;

    @Override
    public void updateChase(EnemyContext ctx, float dt) {
        float dx = ctx.getTargetPosition().x - ctx.getPosition().x;
        float dy = ctx.getTargetPosition().y - ctx.getPosition().y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0.0001f) {
            float speed = ctx.getType().getSpeed() * 1.2f;
            ctx.setVelocity((dx / len) * speed, (dy / len) * speed);
            ctx.setFacing(dx, dy);
        } else {
            ctx.setVelocity(0, 0);
        }
    }

    @Override
    public int performAttack(EnemyContext ctx) {
        int base = ctx.getType().getDamage();
        if (ctx.distanceToTarget() <= AMBUSH_RANGE && ctx.getStateTimer() < 0.1f) {
            return Math.round(base * AMBUSH_MULTIPLIER);
        }
        return base;
    }

    @Override
    public float getEngagementRange(EnemyContext ctx) {
        return ctx.getType().getAttackRange();
    }

    @Override
    public String getName() { return "Stealth"; }
}
