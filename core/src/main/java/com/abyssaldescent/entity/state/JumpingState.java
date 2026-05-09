package com.abyssaldescent.entity.state;

import com.abyssaldescent.entity.player.PlayerContext;

public final class JumpingState implements PlayerState {

    public static final JumpingState INSTANCE = new JumpingState();

    private JumpingState() {}

    @Override
    public void enter(PlayerContext ctx) {
        ctx.setOnGround(false);
        float ix = ctx.getMoveInputX();
        float vx = ix != 0 ? Math.signum(ix) * PlayerContext.BASE_SPEED : ctx.getVelocity().x;
        ctx.setVelocity(vx, PlayerContext.JUMP_VELOCITY);
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
        if (ctx.getVelocity().y <= 0) {
            return FallingState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Jumping"; }
}
