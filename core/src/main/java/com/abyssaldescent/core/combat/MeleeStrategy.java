package com.abyssaldescent.core.combat;

import com.abyssaldescent.core.entity.state.PlayerContext;

/**
 * Strategy for Karin's melee combat style.
 * Close-range, full base damage, combo-capable (series of quick strikes).
 */
public final class MeleeStrategy implements CombatStrategy {

    /** Melee combo multiplier for consecutive hits within the combo window. */
    private static final float COMBO_MULTIPLIER = 1.25f;

    /** Maximum number of hits in a combo chain. */
    private static final int MAX_COMBO = 3;

    /** Time window to chain the next hit (seconds). */
    private static final float COMBO_WINDOW = 0.6f;

    private int comboCount;
    private float timeSinceLastHit;

    @Override
    public int calculateDamage(int baseDamage) {
        if (timeSinceLastHit <= COMBO_WINDOW && comboCount < MAX_COMBO) {
            comboCount++;
        } else {
            comboCount = 1;
        }
        timeSinceLastHit = 0;

        float multiplier = 1f + (comboCount - 1) * (COMBO_MULTIPLIER - 1f);
        return Math.round(baseDamage * multiplier);
    }

    @Override
    public float getRange() {
        return PlayerContext.ATTACK_RANGE;
    }

    @Override
    public String getName() { return "Melee"; }

    /** Call every frame so the combo window can expire. */
    public void update(float deltaTime) {
        timeSinceLastHit += deltaTime;
        if (timeSinceLastHit > COMBO_WINDOW) {
            comboCount = 0;
        }
    }

    public int getComboCount() { return comboCount; }
}
