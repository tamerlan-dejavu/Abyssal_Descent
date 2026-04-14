package com.abyssaldescent.entity;

public final class IdleState implements PlayerState {

    public static final IdleState INSTANCE = new IdleState();

    private IdleState() {}

    @Override
    public void enter(PlayerContext ctx) {
        ctx.setVelocity(0, 0);
    }

    @Override
    public void exit(PlayerContext ctx) { }

    @Override
    public PlayerState update(PlayerContext ctx, float dt) {
        ctx.tickDashCooldown(dt);
        ctx.tickAttackCooldown(dt);

        if (ctx.isDashRequested() && ctx.canDash()) {
            return DashingState.INSTANCE;
        }
        if (ctx.isAttackRequested() && ctx.canAttack()) {
            return AttackingState.INSTANCE;
        }
        if (ctx.hasMoveInput()) {
            return WalkingState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Idle"; }
}
