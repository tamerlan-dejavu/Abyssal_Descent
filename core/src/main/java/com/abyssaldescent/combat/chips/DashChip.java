package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class DashChip extends ChipDecorator {
    public static final float REDUCED_COOLDOWN = 0.5f;

    public DashChip(CombatStrategy wrapped) {
        super(wrapped);
    }

    public float getReducedCooldown() { return REDUCED_COOLDOWN; }

    @Override
    public String getName() { return wrapped.getName() + "+QuickDash"; }
}
