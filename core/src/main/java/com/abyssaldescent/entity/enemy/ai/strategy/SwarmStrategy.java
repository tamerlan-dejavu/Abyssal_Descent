package com.abyssaldescent.entity.enemy.ai.strategy;

import com.abyssaldescent.entity.enemy.EnemyContext;

public final class SwarmStrategy implements EnemyStrategy {
    @Override
    public void updateChase(EnemyContext ctx, float dt) {
        float dx = ctx.getTargetPosition().x - ctx.getPosition().x;
        float dy = ctx.getTargetPosition().y - ctx.getPosition().y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0.0001f) {
            float speed = ctx.getType().getSpeed();
            ctx.setVelocity((dx / len) * speed, (dy / len) * speed);
            ctx.setFacing(dx, dy);
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
    public String getName() { return "Swarm"; }
}
