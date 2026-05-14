package com.abyssaldescent.entity.enemy.ai.strategy;

import com.abyssaldescent.entity.enemy.EnemyContext;

public final class RangedStrategy implements EnemyStrategy {
    public static final float PREFERRED_DISTANCE = 350f;  // px
    public static final float KITE_MARGIN        = 60f;   // px

    @Override
    public void updateChase(EnemyContext ctx, float dt) {
        float dx = ctx.getTargetPosition().x - ctx.getPosition().x;
        float dy = ctx.getTargetPosition().y - ctx.getPosition().y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        ctx.setFacing(dx, dy);
        float speed = ctx.getType().getSpeed();

        if (dist < PREFERRED_DISTANCE - KITE_MARGIN && dist > 0.0001f) {
            ctx.setVelocity(-(dx / dist) * speed, -(dy / dist) * speed);
        } else if (dist > PREFERRED_DISTANCE + KITE_MARGIN && dist > 0.0001f) {
            ctx.setVelocity((dx / dist) * speed, (dy / dist) * speed);
        } else {
            ctx.setVelocity(0, 0);
        }
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
    public String getName() { return "Ranged"; }
}
