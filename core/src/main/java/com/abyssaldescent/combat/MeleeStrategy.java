package com.abyssaldescent.combat;

import com.abyssaldescent.entity.PlayerContext;

public final class MeleeStrategy implements CombatStrategy {
    private static final float COMBO_MULTIPLIER = 1.25f;
    private static final int MAX_COMBO = 3;
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

    public void update(float deltaTime) {
        timeSinceLastHit += deltaTime;
        if (timeSinceLastHit > COMBO_WINDOW) {
            comboCount = 0;
        }
    }

    public int getComboCount() { return comboCount; }
}
