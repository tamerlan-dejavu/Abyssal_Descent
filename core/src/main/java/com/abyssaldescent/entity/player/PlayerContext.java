package com.abyssaldescent.entity.player;

import com.badlogic.gdx.math.Vector2;


public final class PlayerContext {
    public static final float BASE_SPEED      = 600f;   // pixels/sec
    public static final float DASH_DISTANCE  = 500f;   // pixels
    public static final float DASH_DURATION  = 0.2f;
    public static final float DASH_I_FRAMES  = 0.2f;
    public static final float DASH_COOLDOWN  = 1.0f;
    public static final float ATTACK_DURATION = 0.3f;
    public static final float ATTACK_COOLDOWN = 0.15f;
    public static final float ATTACK_RANGE   = 150f;   // pixels

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 facing = new Vector2(1, 0);

    private float moveInputX;
    private float moveInputY;
    private boolean attackRequested;
    private boolean dashRequested;
    private boolean interactRequested;
    private boolean blockActive;

    private float stateTimer;
    private float dashCooldownTimer;
    private float attackCooldownTimer;
    private boolean invincible;

    private float slowMultiplier         = 1.0f;
    private float slowTimer              = 0f;
    private boolean grabbed              = false;
    private float grabTimer              = 0f;
    private int   grabDps                = 0;
    private float grabDamageAccumulator  = 0f;

    public Vector2 getPosition() { return position; }
    public void setPosition(float x, float y) { position.set(x, y); }

    public Vector2 getVelocity() { return velocity; }
    public void setVelocity(float vx, float vy) { velocity.set(vx, vy); }

    public Vector2 getFacing() { return facing; }
    public void setFacing(float fx, float fy) {
        if (fx != 0 || fy != 0) facing.set(fx, fy).nor();
    }

    public float getMoveInputX() { return moveInputX; }
    public float getMoveInputY() { return moveInputY; }
    public boolean hasMoveInput() { return moveInputX != 0 || moveInputY != 0; }

    public void setMoveInput(float x, float y) {
        this.moveInputX = x;
        this.moveInputY = y;
    }

    public boolean isAttackRequested()  { return attackRequested; }
    public void setAttackRequested(boolean v)  { this.attackRequested = v; }
    public boolean isDashRequested()    { return dashRequested; }
    public void setDashRequested(boolean v)    { this.dashRequested = v; }
    public boolean isInteractRequested(){ return interactRequested; }
    public void setInteractRequested(boolean v){ this.interactRequested = v; }
    public boolean isBlockActive()     { return blockActive; }
    public void setBlockActive(boolean v)     { this.blockActive = v; }
    public float getStateTimer()  { return stateTimer; }
    public void  setStateTimer(float t) { this.stateTimer = t; }
    public void  tickStateTimer(float dt) { this.stateTimer += dt; }
    public float getDashCooldownTimer() { return dashCooldownTimer; }
    public void  setDashCooldownTimer(float t) { this.dashCooldownTimer = t; }
    public void  tickDashCooldown(float dt) {
        if (dashCooldownTimer > 0) dashCooldownTimer = Math.max(0, dashCooldownTimer - dt);
    }
    public boolean canDash() { return dashCooldownTimer <= 0; }
    public float getAttackCooldownTimer() { return attackCooldownTimer; }
    public void  setAttackCooldownTimer(float t) { this.attackCooldownTimer = t; }
    public void  tickAttackCooldown(float dt) {
        if (attackCooldownTimer > 0) attackCooldownTimer = Math.max(0, attackCooldownTimer - dt);
    }
    public boolean canAttack() { return attackCooldownTimer <= 0; }
    public boolean isInvincible()  { return invincible; }
    public void setInvincible(boolean v) { this.invincible = v; }

    public void applyMovement(float dt) {
        position.mulAdd(velocity, dt);
    }

    public void consumeInputFlags() {
        attackRequested   = false;
        dashRequested     = false;
        interactRequested = false;
    }

    public void applySlow(float magnitude, float duration) {
        slowMultiplier = 1f - magnitude;
        slowTimer      = duration;
    }

    public void applyGrab(int dps, float duration) {
        grabbed               = true;
        grabDps               = dps;
        grabTimer             = duration;
        grabDamageAccumulator = 0f;
    }

    public int tickEffects(float dt) {
        if (slowTimer > 0) {
            slowTimer -= dt;
            if (slowTimer <= 0) {
                slowMultiplier = 1.0f;
                slowTimer      = 0f;
            }
        }
        int grabDmg = 0;
        if (grabbed) {
            grabTimer             -= dt;
            grabDamageAccumulator += grabDps * dt;
            if (grabDamageAccumulator >= 1f) {
                grabDmg               = (int) grabDamageAccumulator;
                grabDamageAccumulator -= grabDmg;
            }
            if (grabTimer <= 0) {
                grabbed  = false;
                grabTimer = 0f;
                grabDps   = 0;
            }
        }
        return grabDmg;
    }

    public float getEffectiveSpeedMultiplier() { return grabbed ? 0f : slowMultiplier; }
    public boolean isSlowed()  { return slowTimer > 0; }
    public float getSlowMultiplier() { return slowMultiplier; }
    public boolean isGrabbed() { return grabbed; }
}
