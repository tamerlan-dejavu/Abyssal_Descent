package com.abyssaldescent.entity.state;

import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerAttackEvent;

public final class AttackingState implements PlayerState {
    public static final AttackingState INSTANCE = new AttackingState();

    private AttackingState() {}

    @Override
    public void enter(PlayerContext ctx) {
        ctx.setStateTimer(0);
        // Preserve vertical velocity when attacking in air
        ctx.setVelocity(0, ctx.isOnGround() ? 0 : ctx.getVelocity().y);

        EventBus.getInstance().post(new PlayerAttackEvent(
                ctx.getPosition().x,
                ctx.getPosition().y,
                ctx.getFacing().x,
                ctx.getFacing().y,
                PlayerContext.ATTACK_RANGE
        ));
    }

    @Override
    public void exit(PlayerContext ctx) {
        ctx.setAttackCooldownTimer(PlayerContext.ATTACK_COOLDOWN);
    }

    @Override
    public PlayerState update(PlayerContext ctx, float dt) {
        ctx.tickStateTimer(dt);
        ctx.tickDashCooldown(dt);

        if (!ctx.isOnGround()) {
            ctx.applyGravity(dt);
            ctx.applyMovement(dt);
            if (ctx.getPosition().y <= PlayerContext.GROUND_Y) {
                ctx.landOnGround();
            }
        }

        if (ctx.getStateTimer() >= PlayerContext.ATTACK_DURATION) {
            if (!ctx.isOnGround()) return FallingState.INSTANCE;
            if (ctx.hasMoveInput()) return WalkingState.INSTANCE;
            return IdleState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Attacking"; }
}
