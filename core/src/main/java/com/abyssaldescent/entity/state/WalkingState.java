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

        if (ctx.isDashRequested() && ctx.canDash()) return DashingState.INSTANCE;

        if (ctx.isAttackRequested() && ctx.canAttack()) {
            return AttackingState.INSTANCE;
        }
        if (!ctx.hasMoveInput()) {
            return IdleState.INSTANCE;
        }

        float ix = Math.max(-1f, Math.min(1f, ctx.getMoveInputX()));
        float iy = Math.max(-1f, Math.min(1f, ctx.getMoveInputY()));

        float len = (float) Math.sqrt(ix * ix + iy * iy);
        if (len > 1f) { ix /= len; iy /= len; }

        ctx.setFacing(ix, iy);
        float speed = PlayerContext.BASE_SPEED * ctx.getEffectiveSpeedMultiplier();
        ctx.setVelocity(ix * speed, iy * speed);
        ctx.applyMovement(dt);

        return this;
    }

    @Override
    public String getName() { return "Walking"; }
}
