package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.strategy.*;
import com.abyssaldescent.event.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyCreationTest {

    private EnemyFactory factory;

    @BeforeEach
    void setUp() {
        EventBus.getInstance().clear();
        factory = new EnemyFactory();
    }

    @Test
    void shadow_goblin_uses_swarm_strategy() {
        assertInstanceOf(SwarmStrategy.class, factory.create(EnemyType.SHADOW_GOBLIN, 0, 0).getStrategy());
    }

    @Test
    void moss_crawler_uses_crawler_strategy() {
        assertInstanceOf(CrawlerStrategy.class, factory.create(EnemyType.MOSS_CRAWLER, 0, 0).getStrategy());
    }

    @Test
    void stone_watcher_uses_stationary_strategy() {
        assertInstanceOf(StationaryStrategy.class, factory.create(EnemyType.STONE_WATCHER, 0, 0).getStrategy());
    }

    @Test
    void bone_archer_uses_ranged_strategy() {
        assertInstanceOf(RangedStrategy.class, factory.create(EnemyType.BONE_ARCHER, 0, 0).getStrategy());
    }

    @Test
    void drowner_uses_drowners_strategy() {
        assertInstanceOf(DrownersStrategy.class, factory.create(EnemyType.DROWNER, 0, 0).getStrategy());
    }

    @Test
    void rift_jellyfish_uses_jellyfish_strategy() {
        assertInstanceOf(JellyfishStrategy.class, factory.create(EnemyType.RIFT_JELLYFISH, 0, 0).getStrategy());
    }

    @Test
    void rift_knight_uses_stealth_strategy() {
        assertInstanceOf(StealthStrategy.class, factory.create(EnemyType.RIFT_KNIGHT, 0, 0).getStrategy());
    }

    @Test
    void lava_snake_uses_swarm_strategy() {
        assertInstanceOf(SwarmStrategy.class, factory.create(EnemyType.LAVA_SNAKE, 0, 0).getStrategy());
    }

    @Test
    void slag_elemental_uses_heavy_strategy() {
        assertInstanceOf(HeavyStrategy.class, factory.create(EnemyType.SLAG_ELEMENTAL, 0, 0).getStrategy());
    }

    @Test
    void all_tier1_tier2_spawn_at_correct_position() {
        EnemyType[] tier12 = {
            EnemyType.SHADOW_GOBLIN, EnemyType.MOSS_CRAWLER, EnemyType.STONE_WATCHER,
            EnemyType.BONE_ARCHER,   EnemyType.DROWNER,       EnemyType.RIFT_JELLYFISH
        };
        for (EnemyType t : tier12) {
            Enemy e = factory.create(t, 3f, 7f);
            assertEquals(3f, e.getX(), 0.001f, t + " x");
            assertEquals(7f, e.getY(), 0.001f, t + " y");
        }
    }

    @Test
    void all_tier1_tier2_spawn_with_full_hp() {
        EnemyType[] tier12 = {
            EnemyType.SHADOW_GOBLIN, EnemyType.MOSS_CRAWLER, EnemyType.STONE_WATCHER,
            EnemyType.BONE_ARCHER,   EnemyType.DROWNER,       EnemyType.RIFT_JELLYFISH
        };
        for (EnemyType t : tier12) {
            Enemy e = factory.create(t, 0, 0);
            assertEquals(t.getMaxHp(), e.getCurrentHp(), t + " hp");
        }
    }

    @Test
    void all_tier1_tier2_spawn_in_idle_state() {
        EnemyType[] tier12 = {
            EnemyType.SHADOW_GOBLIN, EnemyType.MOSS_CRAWLER, EnemyType.STONE_WATCHER,
            EnemyType.BONE_ARCHER,   EnemyType.DROWNER,       EnemyType.RIFT_JELLYFISH
        };
        for (EnemyType t : tier12) {
            assertEquals("Idle", factory.create(t, 0, 0).getStateName(), t + " state");
        }
    }

    @Test
    void factory_generates_unique_ids_per_type() {
        Enemy a = factory.create(EnemyType.SHADOW_GOBLIN, 0, 0);
        Enemy b = factory.create(EnemyType.SHADOW_GOBLIN, 0, 0);
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void factory_id_contains_type_name_lowercase() {
        assertTrue(factory.create(EnemyType.MOSS_CRAWLER,   0, 0).getId().startsWith("moss_crawler_"));
        assertTrue(factory.create(EnemyType.RIFT_JELLYFISH, 0, 0).getId().startsWith("rift_jellyfish_"));
        assertTrue(factory.create(EnemyType.DROWNER,        0, 0).getId().startsWith("drowner_"));
    }
}
