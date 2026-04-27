package com.abyssaldescent.entity.player;

import com.badlogic.gdx.math.Vector2;


public final class PlayerContext {
    public static final float BASE_SPEED    = 5f;
    public static final float DASH_DISTANCE = 3f;
    public static final float DASH_DURATION = 0.2f;
    public static final float DASH_I_FRAMES = 0.2f;
    public static final float DASH_COOLDOWN = 1.0f;   // GDD: откат 1 с
    public static final float ATTACK_DURATION = 0.3f;
    public static final float ATTACK_COOLDOWN = 0.15f;
    public static final float ATTACK_RANGE  = 1.5f;
    public static final float JUMP_VELOCITY = 12f;
    public static final float GRAVITY       = -25f;
    public static final float GROUND_Y      = 0.5f;   // half-sprite above world floor

    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 facing = new Vector2(1, 0);

    private float moveInputX;
    private float moveInputY;
    private boolean attackRequested;
    private boolean dashRequested;
    private boolean jumpRequested;
    private boolean interactRequested;

    private float stateTimer;
    private float dashCooldownTimer;
    private float attackCooldownTimer;
    private boolean invincible;
    private boolean onGround = true;

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
    public boolean hasMoveInput() { return moveInputX != 0; }

    public void setMoveInput(float x, float y) {
        this.moveInputX = x;
        this.moveInputY = y;
    }

    public boolean isAttackRequested()  { return attackRequested; }
    public void setAttackRequested(boolean v)  { this.attackRequested = v; }
    public boolean isDashRequested()    { return dashRequested; }
    public void setDashRequested(boolean v)    { this.dashRequested = v; }
    public boolean isJumpRequested()    { return jumpRequested; }
    public void setJumpRequested(boolean v)    { this.jumpRequested = v; }
    public boolean isInteractRequested(){ return interactRequested; }
    public void setInteractRequested(boolean v){ this.interactRequested = v; }
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
    public boolean isOnGround()    { return onGround; }
    public void setOnGround(boolean v) { this.onGround = v; }
    public void applyGravity(float dt) {
        if (!onGround) velocity.y += GRAVITY * dt;
    }
    public void landOnGround() {
        position.y = GROUND_Y;
        velocity.y = 0;
        onGround   = true;
    }

    public void applyMovement(float dt) {
        position.mulAdd(velocity, dt);
    }

    public void consumeInputFlags() {
        attackRequested   = false;
        dashRequested     = false;
        jumpRequested     = false;
        interactRequested = false;
    }
}
