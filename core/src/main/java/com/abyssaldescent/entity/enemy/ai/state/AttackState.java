package com.abyssaldescent.entity.enemy.ai.state;

import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.entity.enemy.ai.strategy.EnemyStrategy;
import com.abyssaldescent.event.DamageEvent;
import com.abyssaldescent.event.EventBus;

public final class AttackState implements EnemyState {
    public static final AttackState INSTANCE = new AttackState();
    public static final String PLAYER_TARGET_ID = "player";

    private AttackState() {}

    @Override
    public void enter(EnemyContext ctx) {
        ctx.setStateTimer(0);
        ctx.setVelocity(0, 0);
        float dx = ctx.getTargetPosition().x - ctx.getPosition().x;
        float dy = ctx.getTargetPosition().y - ctx.getPosition().y;
        ctx.setFacing(dx, dy);
    }

    @Override
    public void exit(EnemyContext ctx) {}

    @Override
    public EnemyState update(EnemyContext ctx, EnemyStrategy strategy, float dt) {
        ctx.tickStateTimer(dt);

        if (ctx.getStateTimer() >= EnemyContext.ATTACK_WINDUP) {
            int dmg = strategy.performAttack(ctx);
            if (dmg > 0) {
                EventBus.getInstance().post(
                        new DamageEvent(PLAYER_TARGET_ID, dmg, ctx.getType().name())
                );
            }
            ctx.setAttackCooldown(strategy.getAttackCooldown());
            if (ctx.isLowHp()) {
                return FleeState.INSTANCE;
            }
            if (ctx.isTargetVisible() && ctx.distanceToTarget() <= EnemyContext.AGGRO_RADIUS) {
                return ChaseState.INSTANCE;
            }
            return IdleState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Attack"; }
}
