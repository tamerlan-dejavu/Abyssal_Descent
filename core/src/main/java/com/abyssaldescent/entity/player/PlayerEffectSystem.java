package com.abyssaldescent.entity.player;

import com.abyssaldescent.event.DamageEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.StatusEffect;
import com.abyssaldescent.event.StatusEffectEvent;

public final class PlayerEffectSystem {

    private static final String PLAYER_ID = "player";

    private final PlayerContext ctx;
    private final EventBus eventBus;
    private final EventListener<StatusEffectEvent> effectListener = this::onStatusEffect;

    public PlayerEffectSystem(PlayerContext ctx, EventBus eventBus) {
        this.ctx      = ctx;
        this.eventBus = eventBus;
        eventBus.subscribe(StatusEffectEvent.class, effectListener);
    }

    public void update(float dt) {
        int grabDmg = ctx.tickEffects(dt);
        if (grabDmg > 0) {
            eventBus.post(new DamageEvent(PLAYER_ID, grabDmg, "grab_dot"));
        }
    }

    public void dispose() {
        eventBus.unsubscribe(StatusEffectEvent.class, effectListener);
    }

    private void onStatusEffect(StatusEffectEvent e) {
        if (!PLAYER_ID.equals(e.getTargetId())) return;
        if (e.getEffect() == StatusEffect.SLOW) {
            ctx.applySlow(e.getMagnitude(), e.getDuration());
        } else if (e.getEffect() == StatusEffect.GRAB) {
            ctx.applyGrab((int) e.getMagnitude(), e.getDuration());
        }
    }
}
