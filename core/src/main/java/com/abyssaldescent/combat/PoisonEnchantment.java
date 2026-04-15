package com.abyssaldescent.combat;

public final class PoisonEnchantment extends WeaponDecorator {
    public static final int DOT_DAMAGE_PER_TICK = 2;
    public static final float DOT_DURATION = 3.0f;

    public PoisonEnchantment(CombatStrategy wrapped) {
        super(wrapped);
    }

    @Override
    public String getName() {
        return wrapped.getName() + "+Poison";
    }

    public int getDotDamage() { return DOT_DAMAGE_PER_TICK; }

    public float getDotDuration() { return DOT_DURATION; }
}
