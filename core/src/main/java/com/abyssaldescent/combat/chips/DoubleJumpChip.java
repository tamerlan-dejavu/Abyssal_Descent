package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class DoubleJumpChip extends ChipDecorator {
    public static final float RANGE_BONUS = 0.5f;

    public DoubleJumpChip(CombatStrategy wrapped) {
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
