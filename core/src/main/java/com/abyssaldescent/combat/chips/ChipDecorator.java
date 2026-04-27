package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public abstract class ChipDecorator implements CombatStrategy {
    protected final CombatStrategy wrapped;

    protected ChipDecorator(CombatStrategy wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped strategy must not be null");
        }
        this.wrapped = wrapped;
    }

    @Override
    public int calculateDamage(int baseDamage) {
        return wrapped.calculateDamage(baseDamage);
    }

    @Override
    public float getRange() {
        return wrapped.getRange();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    public CombatStrategy unwrap() {
        return wrapped;
    }
}
