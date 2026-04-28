package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.strategy.CrawlerStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.DrownersStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.EnemyStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.HeavyStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.JellyfishStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.RangedStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.StationaryStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.StealthStrategy;
import com.abyssaldescent.entity.enemy.ai.strategy.SwarmStrategy;

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
        switch (type) {
            case SHADOW_GOBLIN: return new SwarmStrategy();
            case MOSS_CRAWLER: return new CrawlerStrategy();
            case STONE_WATCHER: return new StationaryStrategy();
            case BONE_ARCHER: return new RangedStrategy();
            case DROWNER: return new DrownersStrategy();
            case RIFT_JELLYFISH: return new JellyfishStrategy();
            case RIFT_KNIGHT: return new StealthStrategy();
            case LAVA_SNAKE: return new SwarmStrategy();
            case SLAG_ELEMENTAL: return new HeavyStrategy();
            case MALTARION_ECHO: return new RangedStrategy();
            default: throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
    }
}
