package com.abyssaldescent.combat;

public final class FireEnchantment extends WeaponDecorator {
    public static final int BONUS_DAMAGE = 5;

    public FireEnchantment(CombatStrategy wrapped) {
        super(wrapped);
    }

    @Override
    public int calculateDamage(int baseDamage) {
        return wrapped.calculateDamage(baseDamage) + BONUS_DAMAGE;
    }

    @Override
    public String getName() {
        return wrapped.getName() + "+Fire";
    }
}
