package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class FireAuraChip extends ChipDecorator {
    public static final int AOE_DAMAGE = 30;
    public static final float AOE_RADIUS = 2.0f;
    public static final float DURATION = 3.0f;
    public static final float COOLDOWN = 15.0f;
    private float remaining = 0;
    private float cooldownTimer = 0;

    public FireAuraChip(CombatStrategy wrapped) {
        super(wrapped);
    }

    public void activate() {
        if (cooldownTimer <= 0) {
            remaining = DURATION;
            cooldownTimer = COOLDOWN;
        }
    }

    public void update(float dt) {
        if (remaining > 0) remaining = Math.max(0, remaining - dt);
        if (cooldownTimer > 0) cooldownTimer = Math.max(0, cooldownTimer - dt);
    }

    public boolean isActive() { return remaining > 0; }
    public float getCooldownTimer() { return cooldownTimer; }
    public int getAoeDamage() { return AOE_DAMAGE; }
    public float getAoeRadius() { return AOE_RADIUS; }

    @Override
    public String getName() { return wrapped.getName() + "+FireAura"; }
}
