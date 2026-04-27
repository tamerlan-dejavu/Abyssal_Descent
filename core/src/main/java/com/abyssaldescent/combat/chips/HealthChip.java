package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class HealthChip extends ChipDecorator {
    public static final int DOT_DAMAGE_PER_TICK = 2;
    public static final float DOT_DURATION = 3.0f;

    public HealthChip(CombatStrategy wrapped) {
        super(wrapped);
    }

    @Override
    public String getName() {
        return wrapped.getName() + "+Poison";
    }

    public int getDotDamage() { return DOT_DAMAGE_PER_TICK; }

    public float getDotDuration() { return DOT_DURATION; }
}
