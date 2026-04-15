package com.abyssaldescent.combat;

public abstract class WeaponDecorator implements CombatStrategy {
    protected final CombatStrategy wrapped;

    protected WeaponDecorator(CombatStrategy wrapped) {
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
