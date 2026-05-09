package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class ShieldChip extends ChipDecorator {
    public static final float SHIELD_DURATION = 5.0f;
    public static final float COOLDOWN = 20.0f;
    private boolean shieldActive = false;
    private float shieldTimer = 0;
    private float cooldownTimer = 0;

    public ShieldChip(CombatStrategy wrapped) {
        super(wrapped);
    }

    public void activate() {
        if (cooldownTimer <= 0) {
            shieldActive = true;
            shieldTimer = SHIELD_DURATION;
            cooldownTimer = COOLDOWN;
        }
    }

    public void update(float dt) {
        if (shieldActive) {
            shieldTimer = Math.max(0, shieldTimer - dt);
            if (shieldTimer <= 0) shieldActive = false;
        }
        if (cooldownTimer > 0) cooldownTimer = Math.max(0, cooldownTimer - dt);
    }

    public boolean consumeShield() {
        if (shieldActive) {
            shieldActive = false;
            shieldTimer = 0;
            return true;
        }
        return false;
    }

    public boolean isShieldActive() { return shieldActive; }
    public float getCooldownTimer() { return cooldownTimer; }

    @Override
    public String getName() { return wrapped.getName() + "+Shield"; }
}
