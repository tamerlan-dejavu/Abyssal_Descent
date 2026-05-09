package com.abyssaldescent.entity.enemy.ai.strategy;

import com.abyssaldescent.entity.enemy.EnemyContext;

public interface EnemyStrategy {
    void updateChase(EnemyContext ctx, float deltaTime);
    int performAttack(EnemyContext ctx);
    float getEngagementRange(EnemyContext ctx);
    String getName();

    default float getAttackCooldown() {
        return EnemyContext.ATTACK_COOLDOWN;
    }
}
