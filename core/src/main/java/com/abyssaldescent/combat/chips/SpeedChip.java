package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class SpeedChip extends ChipDecorator {
    public static final float SLOW_FACTOR = 0.5f;
    public static final float SLOW_DURATION = 2.0f;

    public SpeedChip(CombatStrategy wrapped) {
        super(wrapped);
    }

    @Override
    public String getName() {
        return wrapped.getName() + "+Slow";
    }

    public float getSlowFactor() { return SLOW_FACTOR; }

    public float getSlowDuration() { return SLOW_DURATION; }
}
