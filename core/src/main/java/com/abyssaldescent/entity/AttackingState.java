package com.abyssaldescent.entity;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerAttackEvent;

public final class AttackingState implements PlayerState {

    public static final AttackingState INSTANCE = new AttackingState();

    private AttackingState() {}

    @Override
    public void enter(PlayerContext ctx) {
        ctx.setStateTimer(0);
        ctx.setVelocity(0, 0);

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

        if (ctx.getStateTimer() >= PlayerContext.ATTACK_DURATION) {
            if (ctx.hasMoveInput()) {
                return WalkingState.INSTANCE;
            }
            return IdleState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Attacking"; }
}
