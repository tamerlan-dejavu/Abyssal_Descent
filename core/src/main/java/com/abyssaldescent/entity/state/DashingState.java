package com.abyssaldescent.entity.state;

import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerDashEvent;

public final class DashingState implements PlayerState {

    public static final DashingState INSTANCE = new DashingState();

    private DashingState() {}

    @Override
    public void enter(PlayerContext ctx) {
        ctx.setStateTimer(0);
        ctx.setInvincible(true);

        float speed = PlayerContext.DASH_DISTANCE / PlayerContext.DASH_DURATION;
        float fx = ctx.getFacing().x;
        float fy = ctx.getFacing().y;
        if (fx == 0 && fy == 0) fx = 1;
        ctx.setVelocity(fx * speed, fy * speed);

        EventBus.getInstance().post(new PlayerDashEvent(
                ctx.getPosition().x,
                ctx.getPosition().y,
                fx, fy
        ));
    }

    @Override
    public void exit(PlayerContext ctx) {
        ctx.setVelocity(0, 0);
        ctx.setInvincible(false);
        ctx.setDashCooldownTimer(PlayerContext.DASH_COOLDOWN);
    }

    @Override
    public PlayerState update(PlayerContext ctx, float dt) {
        ctx.tickStateTimer(dt);
        ctx.applyMovement(dt);

        if (ctx.isInvincible() && ctx.getStateTimer() >= PlayerContext.DASH_I_FRAMES) {
            ctx.setInvincible(false);
        }

        if (ctx.getStateTimer() >= PlayerContext.DASH_DURATION) {
            if (ctx.hasMoveInput()) return WalkingState.INSTANCE;
            return IdleState.INSTANCE;
        }
        return this;
    }

    @Override
    public String getName() { return "Dashing"; }
}
