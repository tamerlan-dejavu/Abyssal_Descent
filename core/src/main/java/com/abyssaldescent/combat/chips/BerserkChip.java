package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class BerserkChip extends ChipDecorator {
    public static final float DAMAGE_MULTIPLIER = 2.0f;
    public static final float DURATION = 8.0f;
    public static final float COOLDOWN = 30.0f;
    private float remaining = 0;
    private float cooldownTimer = 0;

    public BerserkChip(CombatStrategy wrapped) {
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

    @Override
    public int calculateDamage(int baseDamage) {
        int base = wrapped.calculateDamage(baseDamage);
        return isActive() ? Math.round(base * DAMAGE_MULTIPLIER) : base;
    }

    @Override
    public String getName() { return wrapped.getName() + "+Berserk"; }
}
