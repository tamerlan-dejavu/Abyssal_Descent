package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class IceArrowChip extends ChipDecorator {
    public static final int PROJECTILE_DAMAGE = 25;
    public static final float SLOW_FACTOR = 0.5f;
    public static final float SLOW_DURATION = 3.0f;
    public static final float COOLDOWN = 10.0f;
    private float cooldownTimer = 0;

    public IceArrowChip(CombatStrategy wrapped) {
        super(wrapped);
    }

    public boolean canFire() { return cooldownTimer <= 0; }

    public void fire() {
        if (canFire()) cooldownTimer = COOLDOWN;
    }

    public void update(float dt) {
        if (cooldownTimer > 0) cooldownTimer = Math.max(0, cooldownTimer - dt);
    }

    public float getCooldownTimer() { return cooldownTimer; }
    public int getProjectileDamage() { return PROJECTILE_DAMAGE; }
    public float getSlowFactor() { return SLOW_FACTOR; }
    public float getSlowDuration() { return SLOW_DURATION; }

    @Override
    public String getName() { return wrapped.getName() + "+IceArrow"; }
}
