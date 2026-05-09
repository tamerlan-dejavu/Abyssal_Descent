package com.abyssaldescent.entity.state;

import com.abyssaldescent.entity.player.PlayerContext;

public final class FallingState implements PlayerState {

    public static final FallingState INSTANCE = new FallingState();

    private FallingState() {}

    @Override
    public void enter(PlayerContext ctx) {
        ctx.setOnGround(false);
    }

    @Override
    public void exit(PlayerContext ctx) { }

    @Override
    public PlayerState update(PlayerContext ctx, float dt) {
        ctx.tickDashCooldown(dt);
        ctx.tickAttackCooldown(dt);

        float ix = ctx.getMoveInputX();
        float vx = ix != 0 ? Math.signum(ix) * PlayerContext.BASE_SPEED : 0;
        ctx.setVelocity(vx, ctx.getVelocity().y);
        if (ix != 0) ctx.setFacing(ix, 0);

        ctx.applyGravity(dt);
        ctx.applyMovement(dt);

        if (ctx.getPosition().y <= PlayerContext.GROUND_Y) {
            ctx.landOnGround();
            return ctx.hasMoveInput() ? WalkingState.INSTANCE : IdleState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Falling"; }
}
