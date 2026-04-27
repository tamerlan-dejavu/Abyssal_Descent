package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class DamageChip extends ChipDecorator {
    public static final int BONUS_DAMAGE = 5;

    public DamageChip(CombatStrategy wrapped) {
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
