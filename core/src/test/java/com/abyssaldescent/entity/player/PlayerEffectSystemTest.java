package com.abyssaldescent.entity.player;

import com.abyssaldescent.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerEffectSystemTest {

    private PlayerContext ctx;
    private PlayerEffectSystem system;
    private EventBus bus;

    @BeforeEach
    void setUp() {
        bus    = EventBus.getInstance();
        bus.clear();
        ctx    = new PlayerContext();
        system = new PlayerEffectSystem(ctx, bus);
    }

    @Test
    void slow_effect_reduces_speed_multiplier() {
        bus.post(new StatusEffectEvent("player", StatusEffect.SLOW, 0.3f, 2f, "src"));
        assertEquals(0.7f, ctx.getSlowMultiplier(), 0.001f);
        assertTrue(ctx.isSlowed());
    }

    @Test
    void slow_effect_ignored_for_non_player_target() {
        bus.post(new StatusEffectEvent("enemy_1", StatusEffect.SLOW, 0.3f, 2f, "src"));
        assertFalse(ctx.isSlowed());
    }

    @Test
    void slow_expires_after_duration() {
        ctx.applySlow(0.3f, 0.5f);
        ctx.tickEffects(0.6f);
        assertFalse(ctx.isSlowed());
        assertEquals(1.0f, ctx.getSlowMultiplier(), 0.001f);
    }

    @Test
    void grab_effect_marks_player_as_grabbed() {
        bus.post(new StatusEffectEvent("player", StatusEffect.GRAB, 6f, 3f, "src"));
        assertTrue(ctx.isGrabbed());
    }

    @Test
    void grabbed_speed_multiplier_is_zero() {
        ctx.applyGrab(6, 3f);
        assertEquals(0f, ctx.getEffectiveSpeedMultiplier(), 0.001f);
    }

    @Test
    void grab_produces_dot_damage_over_time() {
        ctx.applyGrab(6, 3f);
        int total = 0;
        for (int i = 0; i < 60; i++) {
            total += ctx.tickEffects(1f / 60f);
        }
        assertTrue(total >= 5 && total <= 7, "Expected ~6 grab damage in 1s, got " + total);
    }

    @Test
    void grab_expires_after_duration() {
        ctx.applyGrab(6, 1f);
        ctx.tickEffects(1.1f);
        assertFalse(ctx.isGrabbed());
    }

    @Test
    void system_update_posts_grab_damage_event() {
        List<DamageEvent> events = new ArrayList<>();
        bus.subscribe(DamageEvent.class, events::add);

        ctx.applyGrab(60, 3f);
        system.update(1f);

        assertFalse(events.isEmpty());
        assertEquals("player", events.get(0).getTargetId());
        assertEquals("grab_dot", events.get(0).getSource());
    }

    @Test
    void system_does_not_post_damage_when_not_grabbed() {
        List<DamageEvent> events = new ArrayList<>();
        bus.subscribe(DamageEvent.class, events::add);
        system.update(1f);
        assertTrue(events.isEmpty());
    }

    @Test
    void dispose_unsubscribes_from_events() {
        system.dispose();
        bus.post(new StatusEffectEvent("player", StatusEffect.SLOW, 0.3f, 2f, "src"));
        assertFalse(ctx.isSlowed());
    }

    @Test
    void effective_speed_is_normal_when_no_effects() {
        assertEquals(1.0f, ctx.getEffectiveSpeedMultiplier(), 0.001f);
    }

    @Test
    void effective_speed_is_reduced_when_slowed() {
        ctx.applySlow(0.3f, 5f);
        assertEquals(0.7f, ctx.getEffectiveSpeedMultiplier(), 0.001f);
    }
}
