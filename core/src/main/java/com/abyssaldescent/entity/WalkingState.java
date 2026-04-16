package com.abyssaldescent.entity;

public final class WalkingState implements PlayerState {
    public static final WalkingState INSTANCE = new WalkingState();

    private WalkingState() {}

    @Override
    public void enter(PlayerContext ctx) { }

    @Override
    public void exit(PlayerContext ctx) {
        ctx.setVelocity(0, 0);
    }

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
        if (!ctx.hasMoveInput()) {
            return IdleState.INSTANCE;
        }

        float ix = ctx.getMoveInputX();
        float iy = ctx.getMoveInputY();
        float len = (float) Math.sqrt(ix * ix + iy * iy);
        if (len > 1f) {
            ix /= len;
            iy /= len;
        }

        ctx.setFacing(ix, iy);
        ctx.setVelocity(ix * PlayerContext.BASE_SPEED, iy * PlayerContext.BASE_SPEED);
        ctx.applyMovement(dt);

        return this;
    }

    @Override
    public String getName() { return "Walking"; }
}
