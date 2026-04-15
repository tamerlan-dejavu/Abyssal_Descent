package com.abyssaldescent.entity.enemy.ai;

import com.abyssaldescent.entity.enemy.EnemyContext;

public final class IdleState implements EnemyState {
    public static final IdleState INSTANCE = new IdleState();

    private IdleState() {}

    @Override
    public void enter(EnemyContext ctx) {
        ctx.setStateTimer(0);
        ctx.setVelocity(0, 0);
    }

    @Override
    public void exit(EnemyContext ctx) {}

    @Override
    public EnemyState update(EnemyContext ctx, EnemyStrategy strategy, float dt) {
        ctx.tickStateTimer(dt);
        ctx.tickAttackCooldown(dt);

        if (ctx.isTargetVisible() && ctx.distanceToTarget() <= EnemyContext.AGGRO_RADIUS) {
            return ChaseState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Idle"; }
}
