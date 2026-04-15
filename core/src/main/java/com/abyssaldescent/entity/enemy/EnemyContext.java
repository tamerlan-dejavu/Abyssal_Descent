package com.abyssaldescent.entity.enemy;

import com.badlogic.gdx.math.Vector2;

public final class EnemyContext {
    public static final float AGGRO_RADIUS = 7f;
    public static final float FLEE_HP_PERCENT = 0.2f;
    public static final float ATTACK_COOLDOWN = 1.0f;
    public static final float ATTACK_WINDUP = 0.3f;
    private final EnemyType type;
    private final String id;
    private final Vector2 position = new Vector2();
    private final Vector2 velocity = new Vector2();
    private final Vector2 facing = new Vector2(1, 0);
    private final Vector2 targetPosition = new Vector2();
    private int currentHp;
    private boolean targetVisible;
    private float stateTimer;
    private float attackCooldown;

    public EnemyContext(EnemyType type, String id, float x, float y) {
        this.type = type;
        this.id = id;
        this.currentHp = type.getMaxHp();
        this.position.set(x, y);
    }

    public EnemyType getType() { 
        return type; 
    }

    public String getId() { 
        return id; 
    }

    public Vector2 getPosition() { 
        return position; 
    }
    public void setPosition(float x, float y) { 
        position.set(x, y); 
    }

    public Vector2 getVelocity() { 
        return velocity; 
    }

    public void setVelocity(float vx, float vy) { 
        velocity.set(vx, vy); 
    }

    public Vector2 getFacing() {
         return facing; 
    }

    public void setFacing(float fx, float fy) {
        if (fx != 0 || fy != 0) facing.set(fx, fy).nor();
    }

    public Vector2 getTargetPosition() { 
        return targetPosition; 
    }

    public void setTargetPosition(float x, float y) {
         targetPosition.set(x, y);
     }

    public int getCurrentHp() { 
        return currentHp; 
    }

    public void setCurrentHp(int hp) {
        this.currentHp = Math.max(0, Math.min(hp, type.getMaxHp()));
    }

    public void applyDamage(int dmg) { 
        setCurrentHp(currentHp - dmg); 
    }

    public boolean isDead() { 
        return currentHp <= 0; 
    }

    public boolean isTargetVisible() { 
        return targetVisible; 
    }

    public void setTargetVisible(boolean v) { 
        this.targetVisible = v; 
    }

    public float distanceToTarget() { 
        return position.dst(targetPosition); 
    }

    public boolean isLowHp() {
        return currentHp <= type.getMaxHp() * FLEE_HP_PERCENT;
    }

    public float getStateTimer() { 
        return stateTimer; 
    }

    public void setStateTimer(float t) { 
        this.stateTimer = t; 
    }

    public void tickStateTimer(float dt) { 
        this.stateTimer += dt; 
}

    public float getAttackCooldown() { 
        return attackCooldown; 
    }

    public void setAttackCooldown(float t) { 
        this.attackCooldown = t; 
    }

    public void tickAttackCooldown(float dt) {
        if (attackCooldown > 0) attackCooldown = Math.max(0, attackCooldown - dt);
    }

    public boolean canAttack() { 
        return attackCooldown <= 0; 
    }

    public void applyMovement(float dt) {
        position.mulAdd(velocity, dt);
     }
}
