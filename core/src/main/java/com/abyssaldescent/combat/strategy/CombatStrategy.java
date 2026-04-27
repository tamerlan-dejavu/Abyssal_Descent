package com.abyssaldescent.combat.strategy;

public interface CombatStrategy {
    int calculateDamage(int baseDamage);
    float getRange();
    String getName();
}
