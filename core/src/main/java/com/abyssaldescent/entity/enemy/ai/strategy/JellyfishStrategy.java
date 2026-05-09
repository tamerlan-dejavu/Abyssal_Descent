package com.abyssaldescent.entity.enemy.ai.strategy;

import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.event.AoEDamageEvent;
import com.abyssaldescent.event.EventBus;

public final class JellyfishStrategy implements EnemyStrategy {

    public static final float AOE_RADIUS        = 2.0f;
    public static final float PREFERRED_DISTANCE = 3.0f;
    public static final float KITE_MARGIN        = 0.8f;
    public static final float ATTACK_COOLDOWN_OVERRIDE = 2.0f;

    @Override
    public void updateChase(EnemyContext ctx, float dt) {
        float dx = ctx.getTargetPosition().x - ctx.getPosition().x;
        float dy = ctx.getTargetPosition().y - ctx.getPosition().y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        ctx.setFacing(dx, dy);
        float speed = ctx.getType().getSpeed();

        if (dist < PREFERRED_DISTANCE - KITE_MARGIN && dist > 0.0001f) {
            ctx.setVelocity(-(dx / dist) * speed, -(dy / dist) * speed);
        } else if (dist > PREFERRED_DISTANCE + KITE_MARGIN && dist > 0.0001f) {
            ctx.setVelocity((dx / dist) * speed, (dy / dist) * speed);
        } else {
            ctx.setVelocity(0, 0);
        }
    }

    @Override
    public int performAttack(EnemyContext ctx) {
        EventBus.getInstance().post(new AoEDamageEvent(
                ctx.getPosition().x, ctx.getPosition().y,
                AOE_RADIUS, ctx.getType().getDamage(), ctx.getId()
        ));
        return 0;
    }

    @Override
    public float getEngagementRange(EnemyContext ctx) {
        return ctx.getType().getAttackRange();
    }

    @Override
    public float getAttackCooldown() {
        return ATTACK_COOLDOWN_OVERRIDE;
    }

    @Override
    public String getName() { return "Jellyfish"; }
}
