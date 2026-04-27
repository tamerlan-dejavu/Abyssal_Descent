package com.abyssaldescent.entity.state;

import com.abyssaldescent.entity.player.PlayerContext;

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

        if (!ctx.isOnGround()) return FallingState.INSTANCE;

        if (ctx.isJumpRequested()) return JumpingState.INSTANCE;

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
