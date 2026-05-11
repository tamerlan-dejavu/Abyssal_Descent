package com.abyssaldescent.config;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.TierChangedEvent;


public class TierScalingManager {

    private int currentFloor = 1;

    public TierScalingManager(EventBus eventBus) {
        eventBus.subscribe(TierChangedEvent.class, e -> {
            currentFloor = e.getNewTier();
            logScaling();
        });
    }

    public int getCurrentFloor() { return currentFloor; }

    
    public int scaleEnemyDamage(int baseDamage) {
        return GameConfig.getInstance().calcEnemyDamage(baseDamage, currentFloor);
    }

    /**
     * Вернуть итоговый HP врага для текущего яруса.
     */
    public int scaleEnemyHp(int baseHp) {
        return GameConfig.getInstance().calcEnemyHp(baseHp, currentFloor);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void logScaling() {
        GameConfig cfg  = GameConfig.getInstance();
        Difficulty diff = cfg.getDifficulty();
        float dmgMult   = cfg.getEnemyDamageMultiplier() * cfg.getTierDamageMultiplier(currentFloor);
        float hpMult    = cfg.getEnemyHpMultiplier()     * cfg.getTierHpMultiplier(currentFloor);

        System.out.printf("[TierScaling] Ярус %d | Сложность: %s | " +
                          "Урон врагов: ×%.2f | HP врагов: ×%.2f%n",
                currentFloor, diff, dmgMult, hpMult);
    }
}