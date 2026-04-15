package com.abyssaldescent.entity.enemy.ai;

import com.abyssaldescent.entity.enemy.EnemyContext;

public final class DeadState implements EnemyState {
    public static final DeadState INSTANCE = new DeadState();

    private DeadState() {}

    @Override
    public void enter(EnemyContext ctx) {
        ctx.setVelocity(0, 0);
        ctx.setStateTimer(0);
    }

    @Override
    public void exit(EnemyContext ctx) {}

    @Override
    public EnemyState update(EnemyContext ctx, EnemyStrategy strategy, float dt) {
        ctx.tickStateTimer(dt);
        return this;
    }

    @Override
    public String getName() { return "Dead"; }
}
