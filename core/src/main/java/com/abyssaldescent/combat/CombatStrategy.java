package com.abyssaldescent.combat;

public interface CombatStrategy {
    int calculateDamage(int baseDamage);
    float getRange();
    String getName();
}
