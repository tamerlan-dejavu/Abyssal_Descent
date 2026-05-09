package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.state.AttackState;
import com.abyssaldescent.entity.enemy.ai.strategy.*;
import com.abyssaldescent.event.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyStrategyTest {

    private EnemyContext goblinCtx;
    private EnemyContext crawlerCtx;
    private EnemyContext watcherCtx;
    private EnemyContext knightCtx;

    @BeforeEach
    void setUp() {
        EventBus.getInstance().clear();
        goblinCtx  = new EnemyContext(EnemyType.SHADOW_GOBLIN,  "goblin_1",  0f, 0f);
        crawlerCtx = new EnemyContext(EnemyType.MOSS_CRAWLER,   "crawler_1", 0f, 0f);
        watcherCtx = new EnemyContext(EnemyType.STONE_WATCHER,  "watcher_1", 0f, 0f);
        knightCtx  = new EnemyContext(EnemyType.RIFT_KNIGHT,    "knight_1",  0f, 0f);
    }

    @Test
    void swarm_moves_toward_target() {
        goblinCtx.setTargetPosition(5f, 0f);
        new SwarmStrategy().updateChase(goblinCtx, 0.1f);
        assertTrue(goblinCtx.getVelocity().x > 0f);
    }

    @Test
    void swarm_velocity_equals_type_speed() {
        goblinCtx.setTargetPosition(5f, 0f);
        new SwarmStrategy().updateChase(goblinCtx, 0.1f);
        assertEquals(EnemyType.SHADOW_GOBLIN.getSpeed(), goblinCtx.getVelocity().len(), 0.001f);
    }

    @Test
    void swarm_returns_base_damage() {
        assertEquals(EnemyType.SHADOW_GOBLIN.getDamage(), new SwarmStrategy().performAttack(goblinCtx));
    }

    @Test
    void swarm_default_cooldown_is_fast() {
        assertEquals(SwarmStrategy.ATTACK_COOLDOWN_OVERRIDE, new SwarmStrategy().getAttackCooldown(), 0.001f);
    }

    @Test
    void ranged_backs_away_when_too_close() {
        goblinCtx.setTargetPosition(1f, 0f);
        new RangedStrategy().updateChase(goblinCtx, 0.1f);
        assertTrue(goblinCtx.getVelocity().x < 0f);
    }

    @Test
    void ranged_moves_closer_when_too_far() {
        goblinCtx.setTargetPosition(10f, 0f);
        new RangedStrategy().updateChase(goblinCtx, 0.1f);
        assertTrue(goblinCtx.getVelocity().x > 0f);
    }

    @Test
    void ranged_holds_at_preferred_distance() {
        goblinCtx.setTargetPosition(RangedStrategy.PREFERRED_DISTANCE, 0f);
        new RangedStrategy().updateChase(goblinCtx, 0.1f);
        assertEquals(0f, goblinCtx.getVelocity().len(), 0.001f);
    }

    @Test
    void crawler_moves_at_seventy_percent_speed() {
        crawlerCtx.setTargetPosition(5f, 0f);
        new CrawlerStrategy().updateChase(crawlerCtx, 0.1f);
        assertEquals(EnemyType.MOSS_CRAWLER.getSpeed() * 0.7f, crawlerCtx.getVelocity().len(), 0.001f);
    }

    @Test
    void crawler_deals_one_point_five_times_damage() {
        int expected = Math.round(EnemyType.MOSS_CRAWLER.getDamage() * CrawlerStrategy.DAMAGE_MULTIPLIER);
        assertEquals(expected, new CrawlerStrategy().performAttack(crawlerCtx));
    }

    @Test
    void stationary_velocity_is_zero() {
        watcherCtx.setTargetPosition(5f, 0f);
        new StationaryStrategy().updateChase(watcherCtx, 0.1f);
        assertEquals(0f, watcherCtx.getVelocity().len(), 0.001f);
    }

    @Test
    void stationary_cooldown_is_shard_interval() {
        assertEquals(StationaryStrategy.SHARD_COOLDOWN, new StationaryStrategy().getAttackCooldown(), 0.001f);
    }

    @Test
    void stealth_moves_at_one_point_two_times_speed() {
        knightCtx.setTargetPosition(5f, 0f);
        new StealthStrategy().updateChase(knightCtx, 0.1f);
        assertEquals(EnemyType.RIFT_KNIGHT.getSpeed() * 1.2f, knightCtx.getVelocity().len(), 0.001f);
    }

    @Test
    void stealth_ambush_doubles_damage_when_fresh_and_close() {
        knightCtx.setTargetPosition(StealthStrategy.AMBUSH_RANGE - 0.1f, 0f);
        AttackState.INSTANCE.enter(knightCtx);
        int base = EnemyType.RIFT_KNIGHT.getDamage();
        assertEquals(Math.round(base * StealthStrategy.AMBUSH_MULTIPLIER),
                new StealthStrategy().performAttack(knightCtx));
    }

    @Test
    void heavy_moves_at_seventy_percent_speed() {
        EnemyContext ctx = new EnemyContext(EnemyType.SLAG_ELEMENTAL, "heavy_1", 0f, 0f);
        ctx.setTargetPosition(5f, 0f);
        new HeavyStrategy().updateChase(ctx, 0.1f);
        assertEquals(EnemyType.SLAG_ELEMENTAL.getSpeed() * 0.7f, ctx.getVelocity().len(), 0.001f);
    }

    @Test
    void heavy_deals_one_point_five_times_damage() {
        EnemyContext ctx = new EnemyContext(EnemyType.SLAG_ELEMENTAL, "heavy_1", 0f, 0f);
        int expected = Math.round(EnemyType.SLAG_ELEMENTAL.getDamage() * HeavyStrategy.HEAVY_DAMAGE_MULTIPLIER);
        assertEquals(expected, new HeavyStrategy().performAttack(ctx));
    }
}
