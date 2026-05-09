package com.abyssaldescent.entity.state;

import com.abyssaldescent.entity.player.PlayerContext;

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

        if (!ctx.isOnGround()) return FallingState.INSTANCE;

        if (ctx.isDashRequested() && ctx.canDash()) return DashingState.INSTANCE;
        if (ctx.isJumpRequested()) return JumpingState.INSTANCE;

        if (ctx.isAttackRequested() && ctx.canAttack()) {
            return AttackingState.INSTANCE;
        }
        if (!ctx.hasMoveInput()) {
            return IdleState.INSTANCE;
        }

        float ix = ctx.getMoveInputX();
        if (ix > 1f) ix = 1f;
        else if (ix < -1f) ix = -1f;

        ctx.setFacing(ix, 0);
        ctx.setVelocity(ix * PlayerContext.BASE_SPEED, 0);
        ctx.applyMovement(dt);

        return this;
    }

    @Override
    public String getName() { return "Walking"; }
}
