package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

public final class ChipFactory {

    public ChipDecorator create(ChipType type, CombatStrategy wrapped) {
        if (type == null) {
            throw new IllegalArgumentException("ChipType cannot be null");
        }
        switch (type) {
            case DAMAGE:      return new DamageChip(wrapped);
            case VAMPIRE:     return new VampireChip(wrapped);
            case SHIELD:      return new ShieldChip(wrapped);
            case BERSERK:     return new BerserkChip(wrapped);
            case DASH:        return new DashChip(wrapped);
            case DOUBLE_JUMP: return new DoubleJumpChip(wrapped);
            case FIRE_AURA:   return new FireAuraChip(wrapped);
            case HEALTH:      return new HealthChip(wrapped);
            case ICE_ARROW:   return new IceArrowChip(wrapped);
            case SPEED:       return new SpeedChip(wrapped);
            default: throw new IllegalArgumentException("Unknown chip type: " + type);
        }
    }
}
