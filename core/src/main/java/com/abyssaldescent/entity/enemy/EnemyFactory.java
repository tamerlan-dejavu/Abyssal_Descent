package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.EnemyStrategy;
import com.abyssaldescent.entity.enemy.ai.HeavyStrategy;
import com.abyssaldescent.entity.enemy.ai.RangedStrategy;
import com.abyssaldescent.entity.enemy.ai.StealthStrategy;
import com.abyssaldescent.entity.enemy.ai.SwarmStrategy;
import java.util.concurrent.atomic.AtomicLong;

public final class EnemyFactory {
    private final AtomicLong idCounter = new AtomicLong();

    public Enemy create(EnemyType type, float x, float y) {
        String id = type.name().toLowerCase() + "_" + idCounter.incrementAndGet();
        EnemyContext ctx = new EnemyContext(type, id, x, y);
        EnemyStrategy strategy = createStrategy(type);
        return new Enemy(ctx, strategy);
    }

    protected EnemyStrategy createStrategy(EnemyType type) {
        switch (type.getAiKind()) {
            case SWARM:   return new SwarmStrategy();
            case RANGED:  return new RangedStrategy();
            case HEAVY:   return new HeavyStrategy();
            case STEALTH: return new StealthStrategy();
            default: throw new IllegalArgumentException("Unknown AI kind: " + type.getAiKind());
        }
    }
}
