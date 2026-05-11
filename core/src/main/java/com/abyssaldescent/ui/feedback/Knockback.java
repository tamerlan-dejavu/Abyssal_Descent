package com.abyssaldescent.ui.feedback;

import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

public class Knockback {

    private static final float DURATION = 0.25f;
    private static final float FORCE    = 120f;
    private static final float FRICTION = 8f;

    private float velX  = 0f;
    private float velY  = 0f;
    private float timer = 0f;

    public static final class KnockbackInfo {
        public final float directionX;
        public final float directionY;

        public KnockbackInfo(float directionX, float directionY) {
            this.directionX = directionX;
            this.directionY = directionY;
        }
    }

    public Knockback(TypedEventBus eventBus) {
        eventBus.subscribe(TypedEvent.Type.PLAYER_DAMAGED, event -> {
            Object payload = event.getPayload();
            if (payload instanceof KnockbackInfo) {
                KnockbackInfo info = (KnockbackInfo) payload;
                apply(info.directionX, info.directionY);
            }
        });
    }

    public void update(float delta) {
        if (timer <= 0) return;
        timer -= delta;
        float decay = (float) Math.exp(-FRICTION * delta);
        velX *= decay;
        velY *= decay;
        if (timer <= 0) { velX = 0; velY = 0; }
    }

    public float   getOffsetX(float delta) { return velX * delta; }
    public float   getOffsetY(float delta) { return velY * delta; }
    public boolean isActive()              { return timer > 0; }

    private void apply(float dirX, float dirY) {
        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (len > 0) { dirX /= len; dirY /= len; }
        velX  = dirX * FORCE;
        velY  = dirY * FORCE * 0.5f + 30f;
        timer = DURATION;
    }
}
