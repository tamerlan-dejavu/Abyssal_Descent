package com.abyssaldescent.core.entity.state;

import com.badlogic.gdx.math.Vector2;

/**
 * Shared context accessible by all {@link PlayerState} implementations.
 * Holds position, velocity, facing direction, and timing data
 * that states need to read and mutate.
 */
public final class PlayerContext {

    /** Karin's base movement speed in tiles/second. */
    public static final float BASE_SPEED = 5f;

    /** Dash distance in tiles. */
    public static final float DASH_DISTANCE = 3f;

    /** Duration of the dash in seconds. */
    public static final float DASH_DURATION = 0.2f;

    /** I-frames duration during dash (invincibility). */
    public static final float DASH_I_FRAMES = 0.2f;

    /** Cooldown between consecutive dashes. */
    public static final float DASH_COOLDOWN = 0.6f;

    /** Duration of a melee attack swing. */
    public static final float ATTACK_DURATION = 0.3f;

    /** Cooldown between consecutive attacks. */
    public static final float ATTACK_COOLDOWN = 0.15f;

    /** Melee attack range in tiles. */
    public static final float ATTACK_RANGE = 1.5f;

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 facing = new Vector2(1, 0);

    private float moveInputX;
    private float moveInputY;
    private boolean attackRequested;
    private boolean dashRequested;

    private float stateTimer;
    private float dashCooldownTimer;
    private float attackCooldownTimer;
    private boolean invincible;

    // ── Position / velocity ─────────────────────────────────────────────────

    public Vector2 getPosition() { return position; }

    public void setPosition(float x, float y) { position.set(x, y); }

    public Vector2 getVelocity() { return velocity; }

    public void setVelocity(float vx, float vy) { velocity.set(vx, vy); }

    // ── Facing direction ────────────────────────────────────────────────────

    public Vector2 getFacing() { return facing; }

    public void setFacing(float fx, float fy) {
        if (fx != 0 || fy != 0) {
            facing.set(fx, fy).nor();
        }
    }

    // ── Input flags ─────────────────────────────────────────────────────────

    public float getMoveInputX() { return moveInputX; }
    public float getMoveInputY() { return moveInputY; }
    public boolean hasMoveInput() { return moveInputX != 0 || moveInputY != 0; }

    public void setMoveInput(float x, float y) {
        this.moveInputX = x;
        this.moveInputY = y;
    }

    public boolean isAttackRequested() { return attackRequested; }
    public void setAttackRequested(boolean requested) { this.attackRequested = requested; }

    public boolean isDashRequested() { return dashRequested; }
    public void setDashRequested(boolean requested) { this.dashRequested = requested; }

    // ── Timers ──────────────────────────────────────────────────────────────

    public float getStateTimer() { return stateTimer; }
    public void setStateTimer(float t) { this.stateTimer = t; }
    public void tickStateTimer(float dt) { this.stateTimer += dt; }

    public float getDashCooldownTimer() { return dashCooldownTimer; }

    public void setDashCooldownTimer(float t) { this.dashCooldownTimer = t; }

    public void tickDashCooldown(float dt) {
        if (dashCooldownTimer > 0) dashCooldownTimer = Math.max(0, dashCooldownTimer - dt);
    }

    public boolean canDash() { return dashCooldownTimer <= 0; }

    public float getAttackCooldownTimer() { return attackCooldownTimer; }

    public void setAttackCooldownTimer(float t) { this.attackCooldownTimer = t; }

    public void tickAttackCooldown(float dt) {
        if (attackCooldownTimer > 0) attackCooldownTimer = Math.max(0, attackCooldownTimer - dt);
    }

    public boolean canAttack() { return attackCooldownTimer <= 0; }

    // ── Invincibility (i-frames) ────────────────────────────────────────────

    public boolean isInvincible() { return invincible; }
    public void setInvincible(boolean invincible) { this.invincible = invincible; }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Applies velocity to position for the given delta. */
    public void applyMovement(float dt) {
        position.mulAdd(velocity, dt);
    }

    /** Clears one-shot input flags (attack, dash). Call at end of frame. */
    public void consumeInputFlags() {
        attackRequested = false;
        dashRequested = false;
    }
}
