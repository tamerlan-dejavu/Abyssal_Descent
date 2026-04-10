package com.abyssaldescent.core.combat;

/**
 * Strategy pattern — defines how an entity deals damage.
 * Karin uses {@link MeleeStrategy}, Rayn will use RangedStrategy.
 */
public interface CombatStrategy {

    /** Calculate the damage dealt by an attack. */
    int calculateDamage(int baseDamage);

    /** The maximum range of this combat style (in tiles). */
    float getRange();

    /** Human-readable name. */
    String getName();
}
