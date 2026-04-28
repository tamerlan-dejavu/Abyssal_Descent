package com.abyssaldescent.entity.enemy.ai.strategy;

import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.StatusEffect;
import com.abyssaldescent.event.StatusEffectEvent;

public final class DrownersStrategy implements EnemyStrategy {

    public static final float GRAB_DPS      = 6f;
    public static final float GRAB_DURATION = 3.0f;
    public static final float ATTACK_COOLDOWN_OVERRIDE = 4.0f;

    @Override
    public void updateChase(EnemyContext ctx, float dt) {
        float dx = ctx.getTargetPosition().x - ctx.getPosition().x;
        float dy = ctx.getTargetPosition().y - ctx.getPosition().y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0.0001f) {
            float speed = ctx.getType().getSpeed() * 0.7f;
            ctx.setVelocity((dx / len) * speed, (dy / len) * speed);
            ctx.setFacing(dx, dy);
        } else {
            ctx.setVelocity(0, 0);
        }
    }

    @Override
    public int performAttack(EnemyContext ctx) {
        EventBus.getInstance().post(new StatusEffectEvent(
                "player", StatusEffect.GRAB, GRAB_DPS, GRAB_DURATION, ctx.getId()
        ));
        return ctx.getType().getDamage();
    }

    @Override
    public float getEngagementRange(EnemyContext ctx) {
        return ctx.getType().getAttackRange() + 0.3f;
    }

    @Override
    public float getAttackCooldown() {
        return ATTACK_COOLDOWN_OVERRIDE;
    }

    @Override
    public String getName() { return "Drowners"; }
}
