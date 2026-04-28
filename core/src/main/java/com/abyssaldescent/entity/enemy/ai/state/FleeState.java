package com.abyssaldescent.entity.enemy.ai.state;

import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.entity.enemy.ai.strategy.EnemyStrategy;

public final class FleeState implements EnemyState {
    public static final FleeState INSTANCE = new FleeState();
    public static final float FLEE_DURATION = 3.0f;

    private FleeState() {}

    @Override
    public void enter(EnemyContext ctx) {
        ctx.setStateTimer(0);
    }

    @Override
    public void exit(EnemyContext ctx) {
        ctx.setVelocity(0, 0);
    }

    @Override
    public EnemyState update(EnemyContext ctx, EnemyStrategy strategy, float dt) {
        ctx.tickStateTimer(dt);
        ctx.tickAttackCooldown(dt);

        float dx = ctx.getPosition().x - ctx.getTargetPosition().x;
        float dy = ctx.getPosition().y - ctx.getTargetPosition().y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0.0001f) {
            float speed = ctx.getType().getSpeed();
            ctx.setVelocity((dx / len) * speed, (dy / len) * speed);
            ctx.setFacing(dx, dy);
        }
        ctx.applyMovement(dt);

        if (ctx.getStateTimer() >= FLEE_DURATION) {
            return IdleState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Flee"; }
}
