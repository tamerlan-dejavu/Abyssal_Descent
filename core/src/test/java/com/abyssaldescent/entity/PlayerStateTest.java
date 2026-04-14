package com.abyssaldescent.entity;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerAttackEvent;
import com.abyssaldescent.event.PlayerDashEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStateTest {

    private PlayerContext ctx;

    @BeforeEach
    void setUp() {
        EventBus.getInstance().clear();
        ctx = new PlayerContext();
        ctx.setPosition(5, 5);
    }

    // ── IdleState ───────────────────────────────────────────────────────────

    @Test
    void idle_staysIdle_whenNoInput() {
        PlayerState state = IdleState.INSTANCE;
        state.enter(ctx);

        PlayerState next = state.update(ctx, 0.016f);
        assertSame(IdleState.INSTANCE, next);
    }

    @Test
    void idle_transitionsToWalking_whenMoveInput() {
        PlayerState state = IdleState.INSTANCE;
        state.enter(ctx);
        ctx.setMoveInput(1, 0);

        PlayerState next = state.update(ctx, 0.016f);
        assertSame(WalkingState.INSTANCE, next);
    }

    @Test
    void idle_transitionsToAttacking_whenAttackRequested() {
        PlayerState state = IdleState.INSTANCE;
        state.enter(ctx);
        ctx.setAttackRequested(true);

        PlayerState next = state.update(ctx, 0.016f);
        assertSame(AttackingState.INSTANCE, next);
    }

    @Test
    void idle_transitionsToDashing_whenDashRequested() {
        PlayerState state = IdleState.INSTANCE;
        state.enter(ctx);
        ctx.setDashRequested(true);

        PlayerState next = state.update(ctx, 0.016f);
        assertSame(DashingState.INSTANCE, next);
    }

    @Test
    void idle_setsVelocityToZero_onEnter() {
        ctx.setVelocity(5, 5);
        IdleState.INSTANCE.enter(ctx);

        assertEquals(0, ctx.getVelocity().x);
        assertEquals(0, ctx.getVelocity().y);
    }

    // ── WalkingState ────────────────────────────────────────────────────────

    @Test
    void walking_movesPlayer_whenInputPresent() {
        PlayerState state = WalkingState.INSTANCE;
        state.enter(ctx);
        ctx.setMoveInput(1, 0);
        float startX = ctx.getPosition().x;

        state.update(ctx, 0.1f);

        assertTrue(ctx.getPosition().x > startX, "Player should move right");
    }

    @Test
    void walking_updatesFacingDirection() {
        PlayerState state = WalkingState.INSTANCE;
        state.enter(ctx);
        ctx.setMoveInput(-1, 0);

        state.update(ctx, 0.016f);

        assertTrue(ctx.getFacing().x < 0, "Facing should point left");
    }

    @Test
    void walking_transitionsToIdle_whenNoInput() {
        PlayerState state = WalkingState.INSTANCE;
        state.enter(ctx);
        ctx.setMoveInput(0, 0);

        PlayerState next = state.update(ctx, 0.016f);
        assertSame(IdleState.INSTANCE, next);
    }

    @Test
    void walking_normsDiagonalMovement() {
        PlayerState state = WalkingState.INSTANCE;
        state.enter(ctx);
        ctx.setMoveInput(1, 1);

        state.update(ctx, 1f);

        // Diagonal speed should be same as cardinal (normalized)
        float dist = ctx.getPosition().dst(5, 5);
        float expectedDist = PlayerContext.BASE_SPEED;
        assertEquals(expectedDist, dist, 0.01f);
    }

    // ── AttackingState ──────────────────────────────────────────────────────

    @Test
    void attacking_firesPlayerAttackEvent_onEnter() {
        List<PlayerAttackEvent> events = new ArrayList<>();
        EventBus.getInstance().subscribe(PlayerAttackEvent.class, events::add);

        ctx.setFacing(1, 0);
        AttackingState.INSTANCE.enter(ctx);

        assertEquals(1, events.size());
        assertEquals(5, events.get(0).getOriginX());
        assertEquals(1, events.get(0).getDirectionX());
    }

    @Test
    void attacking_setsVelocityToZero() {
        ctx.setVelocity(10, 10);
        AttackingState.INSTANCE.enter(ctx);

        assertEquals(0, ctx.getVelocity().x);
        assertEquals(0, ctx.getVelocity().y);
    }

    @Test
    void attacking_returnsToIdle_afterDuration() {
        PlayerState state = AttackingState.INSTANCE;
        state.enter(ctx);

        // Still attacking before duration
        PlayerState mid = state.update(ctx, PlayerContext.ATTACK_DURATION * 0.5f);
        assertSame(AttackingState.INSTANCE, mid);

        // Done after full duration
        PlayerState next = state.update(ctx, PlayerContext.ATTACK_DURATION);
        assertSame(IdleState.INSTANCE, next);
    }

    @Test
    void attacking_setsAttackCooldown_onExit() {
        AttackingState.INSTANCE.exit(ctx);

        assertTrue(ctx.getAttackCooldownTimer() > 0);
    }

    @Test
    void idle_doesNotAttack_duringCooldown() {
        ctx.setAttackCooldownTimer(0.5f);
        ctx.setAttackRequested(true);
        IdleState.INSTANCE.enter(ctx);

        PlayerState next = IdleState.INSTANCE.update(ctx, 0.016f);
        assertSame(IdleState.INSTANCE, next, "Should stay idle while attack on cooldown");
    }

    // ── DashingState ────────────────────────────────────────────────────────

    @Test
    void dashing_firesPlayerDashEvent_onEnter() {
        List<PlayerDashEvent> events = new ArrayList<>();
        EventBus.getInstance().subscribe(PlayerDashEvent.class, events::add);

        ctx.setFacing(0, 1);
        DashingState.INSTANCE.enter(ctx);

        assertEquals(1, events.size());
    }

    @Test
    void dashing_grantsInvincibility_onEnter() {
        ctx.setFacing(1, 0);
        DashingState.INSTANCE.enter(ctx);

        assertTrue(ctx.isInvincible());
    }

    @Test
    void dashing_movesPlayerInFacingDirection() {
        ctx.setFacing(1, 0);
        float startX = ctx.getPosition().x;
        DashingState.INSTANCE.enter(ctx);

        DashingState.INSTANCE.update(ctx, PlayerContext.DASH_DURATION);

        assertTrue(ctx.getPosition().x > startX, "Dash should move the player");
    }

    @Test
    void dashing_returnsToIdle_afterDuration() {
        ctx.setFacing(1, 0);
        DashingState.INSTANCE.enter(ctx);

        PlayerState next = DashingState.INSTANCE.update(ctx, PlayerContext.DASH_DURATION);
        assertSame(IdleState.INSTANCE, next);
    }

    @Test
    void dashing_setsDashCooldown_onExit() {
        DashingState.INSTANCE.exit(ctx);

        assertTrue(ctx.getDashCooldownTimer() > 0);
    }

    @Test
    void idle_doesNotDash_duringCooldown() {
        ctx.setDashCooldownTimer(0.5f);
        ctx.setDashRequested(true);
        IdleState.INSTANCE.enter(ctx);

        PlayerState next = IdleState.INSTANCE.update(ctx, 0.016f);
        assertSame(IdleState.INSTANCE, next, "Should stay idle while dash on cooldown");
    }

    @Test
    void dashing_removesInvincibility_onExit() {
        ctx.setInvincible(true);
        DashingState.INSTANCE.exit(ctx);

        assertFalse(ctx.isInvincible());
    }
}
