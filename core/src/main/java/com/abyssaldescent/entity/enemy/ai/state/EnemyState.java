package com.abyssaldescent.entity.enemy.ai.state;

import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.entity.enemy.ai.strategy.EnemyStrategy;

public interface EnemyState {
    void enter(EnemyContext ctx);
    void exit(EnemyContext ctx);
    EnemyState update(EnemyContext ctx, EnemyStrategy strategy, float deltaTime);
    String getName();
}
