package com.abyssaldescent.combat;

public final class SlowEnchantment extends WeaponDecorator {
    public static final float SLOW_FACTOR = 0.5f;
    public static final float SLOW_DURATION = 2.0f;

    public SlowEnchantment(CombatStrategy wrapped) {
        super(wrapped);
    }

    @Override
    public String getName() {
        return wrapped.getName() + "+Slow";
    }

    public float getSlowFactor() { return SLOW_FACTOR; }

    public float getSlowDuration() { return SLOW_DURATION; }
}
