package com.abyssaldescent.entity.enemy.ai.state;

import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.entity.enemy.ai.strategy.EnemyStrategy;

public final class ChaseState implements EnemyState {
    public static final ChaseState INSTANCE = new ChaseState();

    private ChaseState() {}

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

        if (ctx.isLowHp()) {
            return FleeState.INSTANCE;
        }

        if (!ctx.isTargetVisible() || ctx.distanceToTarget() > EnemyContext.AGGRO_RADIUS * 1.5f) {
            return IdleState.INSTANCE;
        }

        strategy.updateChase(ctx, dt);
        ctx.applyMovement(dt);

        if (ctx.distanceToTarget() <= strategy.getEngagementRange(ctx) && ctx.canAttack()) {
            return AttackState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Chase"; }
}
