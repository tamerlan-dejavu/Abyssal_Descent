package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.state.AttackState;
import com.abyssaldescent.entity.enemy.ai.strategy.*;
import com.abyssaldescent.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnemySpecialAttackTest {

    @BeforeEach
    void setUp() {
        EventBus.getInstance().clear();
    }

    @Test
    void crawler_posts_slow_effect_on_attack() {
        List<StatusEffectEvent> events = new ArrayList<>();
        EventBus.getInstance().subscribe(StatusEffectEvent.class, events::add);

        EnemyContext ctx = new EnemyContext(EnemyType.MOSS_CRAWLER, "c1", 0f, 0f);
        new CrawlerStrategy().performAttack(ctx);

        assertEquals(1, events.size());
        StatusEffectEvent e = events.get(0);
        assertEquals("player",        e.getTargetId());
        assertEquals(StatusEffect.SLOW, e.getEffect());
        assertEquals(CrawlerStrategy.SLOW_MAGNITUDE, e.getMagnitude(), 0.001f);
        assertEquals(CrawlerStrategy.SLOW_DURATION,  e.getDuration(),  0.001f);
    }

    @Test
    void crawler_deals_one_point_five_times_damage() {
        EventBus.getInstance().subscribe(StatusEffectEvent.class, e -> {});
        EnemyContext ctx = new EnemyContext(EnemyType.MOSS_CRAWLER, "c1", 0f, 0f);
        int expected = Math.round(EnemyType.MOSS_CRAWLER.getDamage() * CrawlerStrategy.DAMAGE_MULTIPLIER);
        assertEquals(expected, new CrawlerStrategy().performAttack(ctx));
    }

    @Test
    void drowner_posts_grab_effect_on_attack() {
        List<StatusEffectEvent> events = new ArrayList<>();
        EventBus.getInstance().subscribe(StatusEffectEvent.class, events::add);

        EnemyContext ctx = new EnemyContext(EnemyType.DROWNER, "d1", 0f, 0f);
        new DrownersStrategy().performAttack(ctx);

        assertEquals(1, events.size());
        StatusEffectEvent e = events.get(0);
        assertEquals("player",        e.getTargetId());
        assertEquals(StatusEffect.GRAB, e.getEffect());
        assertEquals(DrownersStrategy.GRAB_DPS,      e.getMagnitude(), 0.001f);
        assertEquals(DrownersStrategy.GRAB_DURATION,  e.getDuration(),  0.001f);
    }

    @Test
    void drowner_deals_base_damage_on_attack() {
        EventBus.getInstance().subscribe(StatusEffectEvent.class, e -> {});
        EnemyContext ctx = new EnemyContext(EnemyType.DROWNER, "d1", 0f, 0f);
        assertEquals(EnemyType.DROWNER.getDamage(), new DrownersStrategy().performAttack(ctx));
    }

    @Test
    void drowner_has_extended_attack_cooldown() {
        assertEquals(DrownersStrategy.ATTACK_COOLDOWN_OVERRIDE,
                new DrownersStrategy().getAttackCooldown(), 0.001f);
    }

    @Test
    void jellyfish_posts_aoe_event_on_attack() {
        List<AoEDamageEvent> events = new ArrayList<>();
        EventBus.getInstance().subscribe(AoEDamageEvent.class, events::add);

        EnemyContext ctx = new EnemyContext(EnemyType.RIFT_JELLYFISH, "j1", 3f, 5f);
        new JellyfishStrategy().performAttack(ctx);

        assertEquals(1, events.size());
        AoEDamageEvent e = events.get(0);
        assertEquals(3f, e.getOriginX(), 0.001f);
        assertEquals(5f, e.getOriginY(), 0.001f);
        assertEquals(JellyfishStrategy.AOE_RADIUS, e.getRadius(), 0.001f);
        assertEquals(EnemyType.RIFT_JELLYFISH.getDamage(), e.getDamage());
    }

    @Test
    void jellyfish_returns_zero_direct_damage() {
        EventBus.getInstance().subscribe(AoEDamageEvent.class, e -> {});
        EnemyContext ctx = new EnemyContext(EnemyType.RIFT_JELLYFISH, "j1", 0f, 0f);
        assertEquals(0, new JellyfishStrategy().performAttack(ctx));
    }

    @Test
    void jellyfish_has_extended_attack_cooldown() {
        assertEquals(JellyfishStrategy.ATTACK_COOLDOWN_OVERRIDE,
                new JellyfishStrategy().getAttackCooldown(), 0.001f);
    }

    @Test
    void attack_state_does_not_post_damage_event_for_jellyfish() {
        List<DamageEvent> dmgEvents = new ArrayList<>();
        EventBus.getInstance().subscribe(DamageEvent.class, dmgEvents::add);
        EventBus.getInstance().subscribe(AoEDamageEvent.class, e -> {});

        EnemyContext ctx = new EnemyContext(EnemyType.RIFT_JELLYFISH, "j1", 0f, 0f);
        AttackState.INSTANCE.enter(ctx);
        AttackState.INSTANCE.update(ctx, new JellyfishStrategy(), EnemyContext.ATTACK_WINDUP + 0.01f);

        assertTrue(dmgEvents.isEmpty(), "DamageEvent must not be posted when performAttack returns 0");
    }

    @Test
    void attack_state_sets_drowners_extended_cooldown() {
        EnemyContext ctx = new EnemyContext(EnemyType.DROWNER, "d1", 0f, 0f);
        EventBus.getInstance().subscribe(StatusEffectEvent.class, e -> {});
        AttackState.INSTANCE.enter(ctx);
        AttackState.INSTANCE.update(ctx, new DrownersStrategy(), EnemyContext.ATTACK_WINDUP + 0.01f);

        assertEquals(DrownersStrategy.ATTACK_COOLDOWN_OVERRIDE, ctx.getAttackCooldown(), 0.001f);
    }

    @Test
    void stealth_ambush_doubles_damage() {
        EnemyContext ctx = new EnemyContext(EnemyType.RIFT_KNIGHT, "k1", 0f, 0f);
        ctx.setTargetPosition(StealthStrategy.AMBUSH_RANGE - 0.1f, 0f);
        AttackState.INSTANCE.enter(ctx);

        int base = EnemyType.RIFT_KNIGHT.getDamage();
        int ambushDamage = new StealthStrategy().performAttack(ctx);

        assertEquals(Math.round(base * StealthStrategy.AMBUSH_MULTIPLIER), ambushDamage);
    }

    @Test
    void stealth_normal_damage_when_far() {
        EnemyContext ctx = new EnemyContext(EnemyType.RIFT_KNIGHT, "k1", 0f, 0f);
        ctx.setTargetPosition(10f, 0f);
        AttackState.INSTANCE.enter(ctx);

        int base = EnemyType.RIFT_KNIGHT.getDamage();
        int damage = new StealthStrategy().performAttack(ctx);

        assertEquals(base, damage);
    }
}
