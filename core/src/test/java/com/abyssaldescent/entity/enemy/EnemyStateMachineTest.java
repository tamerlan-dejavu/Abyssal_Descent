package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.state.*;
import com.abyssaldescent.entity.enemy.ai.strategy.*;
import com.abyssaldescent.event.DamageEvent;
import com.abyssaldescent.event.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemyStateMachineTest {

    private EnemyContext ctx;
    private EnemyStrategy swarm;

    @BeforeEach
    void setUp() {
        EventBus.getInstance().clear();
        ctx   = new EnemyContext(EnemyType.SHADOW_GOBLIN, "goblin_1", 0f, 0f);
        swarm = new SwarmStrategy();
    }

    @Test
    void idle_stays_idle_when_target_not_visible() {
        ctx.setTargetVisible(false);
        ctx.setTargetPosition(3f, 0f);
        assertSame(IdleState.INSTANCE, IdleState.INSTANCE.update(ctx, swarm, 0.1f));
    }

    @Test
    void idle_stays_idle_when_target_beyond_aggro_radius() {
        ctx.setTargetVisible(true);
        ctx.setTargetPosition(EnemyContext.AGGRO_RADIUS + 1f, 0f);
        assertSame(IdleState.INSTANCE, IdleState.INSTANCE.update(ctx, swarm, 0.1f));
    }

    @Test
    void idle_transitions_to_chase_when_target_visible_and_in_range() {
        ctx.setTargetVisible(true);
        ctx.setTargetPosition(3f, 0f);
        assertSame(ChaseState.INSTANCE, IdleState.INSTANCE.update(ctx, swarm, 0.1f));
    }

    @Test
    void chase_transitions_to_flee_when_low_hp() {
        ctx.setCurrentHp(1);
        ctx.setTargetVisible(true);
        ctx.setTargetPosition(2f, 0f);
        ChaseState.INSTANCE.enter(ctx);
        assertSame(FleeState.INSTANCE, ChaseState.INSTANCE.update(ctx, swarm, 0.1f));
    }

    @Test
    void chase_transitions_to_idle_when_target_lost() {
        ctx.setTargetVisible(false);
        ctx.setTargetPosition(100f, 0f);
        ChaseState.INSTANCE.enter(ctx);
        assertSame(IdleState.INSTANCE, ChaseState.INSTANCE.update(ctx, swarm, 0.1f));
    }

    @Test
    void chase_transitions_to_attack_when_in_range_and_cooldown_ready() {
        ctx.setTargetVisible(true);
        ctx.setTargetPosition(0.3f, 0f);
        ctx.setAttackCooldown(0f);
        ChaseState.INSTANCE.enter(ctx);
        assertSame(AttackState.INSTANCE, ChaseState.INSTANCE.update(ctx, swarm, 0.1f));
    }

    @Test
    void chase_stays_chasing_when_outside_engagement_range() {
        ctx.setTargetVisible(true);
        ctx.setTargetPosition(2f, 0f);
        ctx.setAttackCooldown(0f);
        ChaseState.INSTANCE.enter(ctx);
        assertSame(ChaseState.INSTANCE, ChaseState.INSTANCE.update(ctx, swarm, 0.016f));
    }

    @Test
    void attack_stays_in_windup_before_threshold() {
        AttackState.INSTANCE.enter(ctx);
        assertSame(AttackState.INSTANCE,
                AttackState.INSTANCE.update(ctx, swarm, EnemyContext.ATTACK_WINDUP * 0.5f));
    }

    @Test
    void attack_fires_damage_event_after_windup() {
        List<DamageEvent> received = new ArrayList<>();
        EventBus.getInstance().subscribe(DamageEvent.class, received::add);
        ctx.setTargetVisible(true);
        ctx.setTargetPosition(1f, 0f);
        AttackState.INSTANCE.enter(ctx);
        AttackState.INSTANCE.update(ctx, swarm, EnemyContext.ATTACK_WINDUP + 0.01f);
        assertEquals(1, received.size());
        assertEquals(AttackState.PLAYER_TARGET_ID, received.get(0).getTargetId());
        assertEquals(EnemyType.SHADOW_GOBLIN.getDamage(), received.get(0).getDamage());
    }

    @Test
    void attack_transitions_to_chase_after_windup_when_target_visible() {
        ctx.setTargetVisible(true);
        ctx.setTargetPosition(2f, 0f);
        AttackState.INSTANCE.enter(ctx);
        assertSame(ChaseState.INSTANCE,
                AttackState.INSTANCE.update(ctx, swarm, EnemyContext.ATTACK_WINDUP + 0.01f));
    }

    @Test
    void attack_transitions_to_idle_after_windup_when_target_lost() {
        ctx.setTargetVisible(false);
        ctx.setTargetPosition(100f, 0f);
        AttackState.INSTANCE.enter(ctx);
        assertSame(IdleState.INSTANCE,
                AttackState.INSTANCE.update(ctx, swarm, EnemyContext.ATTACK_WINDUP + 0.01f));
    }

    @Test
    void attack_sets_strategy_cooldown() {
        EnemyStrategy stationary = new StationaryStrategy();
        ctx.setTargetVisible(false);
        AttackState.INSTANCE.enter(ctx);
        AttackState.INSTANCE.update(ctx, stationary, EnemyContext.ATTACK_WINDUP + 0.01f);
        assertEquals(StationaryStrategy.SHARD_COOLDOWN, ctx.getAttackCooldown(), 0.001f);
    }

    @Test
    void flee_moves_away_and_returns_idle_after_duration() {
        ctx.setTargetPosition(5f, 0f);
        FleeState.INSTANCE.enter(ctx);
        EnemyState next = FleeState.INSTANCE.update(ctx, swarm, FleeState.FLEE_DURATION + 0.01f);
        assertSame(IdleState.INSTANCE, next);
        assertTrue(ctx.getPosition().x < 0f);
    }

    @Test
    void flee_stays_fleeing_before_duration_expires() {
        ctx.setTargetPosition(5f, 0f);
        FleeState.INSTANCE.enter(ctx);
        assertSame(FleeState.INSTANCE,
                FleeState.INSTANCE.update(ctx, swarm, FleeState.FLEE_DURATION * 0.5f));
    }

    @Test
    void dead_stays_dead() {
        DeadState.INSTANCE.enter(ctx);
        assertSame(DeadState.INSTANCE, DeadState.INSTANCE.update(ctx, swarm, 1f));
    }

    @Test
    void enemy_transitions_to_dead_on_lethal_damage() {
        Enemy enemy = new EnemyFactory().create(EnemyType.SHADOW_GOBLIN, 0f, 0f);
        enemy.takeDamage(EnemyType.SHADOW_GOBLIN.getMaxHp());
        assertTrue(enemy.isDead());
        assertEquals("Dead", enemy.getStateName());
    }

    @Test
    void enemy_ignores_damage_when_already_dead() {
        Enemy enemy = new EnemyFactory().create(EnemyType.SHADOW_GOBLIN, 0f, 0f);
        enemy.takeDamage(EnemyType.SHADOW_GOBLIN.getMaxHp());
        enemy.takeDamage(999);
        assertEquals(0, enemy.getCurrentHp());
    }
}
