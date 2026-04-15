package com.abyssaldescent.entity.enemy.ai;

import com.abyssaldescent.entity.enemy.EnemyContext;

public interface EnemyState {
    void enter(EnemyContext ctx);
    void exit(EnemyContext ctx);
    EnemyState update(EnemyContext ctx, EnemyStrategy strategy, float deltaTime);
    String getName();
}
