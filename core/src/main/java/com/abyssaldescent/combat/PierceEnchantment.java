package com.abyssaldescent.combat;

public final class PierceEnchantment extends WeaponDecorator {
    public static final float RANGE_BONUS = 0.5f;

    public PierceEnchantment(CombatStrategy wrapped) {
        super(wrapped);
    }

    @Override
    public float getRange() {
        return wrapped.getRange() + RANGE_BONUS;
    }

    @Override
    public String getName() {
        return wrapped.getName() + "+Pierce";
    }

    public boolean piercesMultipleTargets() {
        return true;
    }
}
