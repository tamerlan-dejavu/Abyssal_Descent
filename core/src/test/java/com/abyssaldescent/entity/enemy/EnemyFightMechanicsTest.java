package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.strategy.SwarmStrategy;
import com.abyssaldescent.event.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyFightMechanicsTest {

    @BeforeEach
    void setUp() {
        EventBus.getInstance().clear();
    }

    @Test
    void swarm_has_fast_attack_cooldown() {
        assertEquals(SwarmStrategy.ATTACK_COOLDOWN_OVERRIDE,
                new SwarmStrategy().getAttackCooldown(), 0.001f);
    }

    @Test
    void goblin_swarm_attacks_frequently() {
        EnemyContext ctx = new EnemyContext(EnemyType.SHADOW_GOBLIN, "g1", 0f, 0f);
        SwarmStrategy swarm = new SwarmStrategy();
        float cooldown = swarm.getAttackCooldown();
        assertTrue(cooldown < 1f, "Swarm cooldown should be < 1s for frequent attacks");
    }

    @Test
    void lava_snake_uses_swarm_mechanics() {
        EnemyContext ctx = new EnemyContext(EnemyType.LAVA_SNAKE, "s1", 0f, 0f);
        SwarmStrategy swarm = new SwarmStrategy();
        assertEquals(SwarmStrategy.ATTACK_COOLDOWN_OVERRIDE,
                swarm.getAttackCooldown(), 0.001f);
    }

    @Test
    void all_tier1_enemies_have_unique_mechanics() {
        EnemyFactory factory = new EnemyFactory();

        Enemy goblin = factory.create(EnemyType.SHADOW_GOBLIN, 0, 0);
        assertNotNull(goblin.getStrategy());

        Enemy crawler = factory.create(EnemyType.MOSS_CRAWLER, 0, 0);
        assertNotNull(crawler.getStrategy());

        Enemy watcher = factory.create(EnemyType.STONE_WATCHER, 0, 0);
        assertNotNull(watcher.getStrategy());
    }

    @Test
    void all_tier2_enemies_have_unique_mechanics() {
        EnemyFactory factory = new EnemyFactory();

        Enemy archer = factory.create(EnemyType.BONE_ARCHER, 0, 0);
        assertNotNull(archer.getStrategy());

        Enemy drowner = factory.create(EnemyType.DROWNER, 0, 0);
        assertNotNull(drowner.getStrategy());

        Enemy jellyfish = factory.create(EnemyType.RIFT_JELLYFISH, 0, 0);
        assertNotNull(jellyfish.getStrategy());
    }

    @Test
    void all_tier3_enemies_have_unique_mechanics() {
        EnemyFactory factory = new EnemyFactory();

        Enemy knight = factory.create(EnemyType.RIFT_KNIGHT, 0, 0);
        assertNotNull(knight.getStrategy());

        Enemy snake = factory.create(EnemyType.LAVA_SNAKE, 0, 0);
        assertNotNull(snake.getStrategy());

        Enemy elemental = factory.create(EnemyType.SLAG_ELEMENTAL, 0, 0);
        assertNotNull(elemental.getStrategy());
    }
}
