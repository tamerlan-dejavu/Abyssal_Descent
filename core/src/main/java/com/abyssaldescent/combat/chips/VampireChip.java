package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class VampireChip extends ChipDecorator {
    public static final int HEAL_PER_HIT = 3;

    public VampireChip(CombatStrategy wrapped) {
        super(wrapped);
    }

    public int getHealPerHit() { return HEAL_PER_HIT; }

    @Override
    public String getName() { return wrapped.getName() + "+Vampire"; }
}
