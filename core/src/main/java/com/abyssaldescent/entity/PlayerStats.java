package com.abyssaldescent.entity;

import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

public class PlayerStats {

    public static final int BASE_DAMAGE  = 15;
    public static final int MAX_RESPAWNS = 3;

    public static final class DamageInfo {
        public final int   amount;
        public final int   currentHp;
        public final int   maxHp;
        public final float worldX;
        public final float worldY;

        public DamageInfo(int amount, int currentHp, int maxHp, float worldX, float worldY) {
            this.amount    = amount;
            this.currentHp = currentHp;
            this.maxHp     = maxHp;
            this.worldX    = worldX;
            this.worldY    = worldY;
        }
    }

    private int   maxHp;
    private int   currentHp;
    private int   baseDamage;
    private float speedMultiplier;
    private int   respawnsLeft;
    private boolean dead;

    private static final float I_FRAME_DURATION = 0.5f;
    private float iFrameTimer = 0f;

    private final TypedEventBus eventBus;

    public PlayerStats(int maxHp, TypedEventBus eventBus) {
        this.eventBus        = eventBus;
        this.maxHp           = maxHp;
        this.currentHp       = maxHp;
        this.baseDamage      = BASE_DAMAGE;
        this.speedMultiplier = 1.0f;
        this.respawnsLeft    = MAX_RESPAWNS;
        this.dead            = false;
    }

    public void update(float delta) {
        if (iFrameTimer > 0) iFrameTimer -= delta;
    }

    public void takeDamage(int amount, float worldX, float worldY) {
        if (dead || iFrameTimer > 0) return;
        currentHp   = Math.max(0, currentHp - amount);
        iFrameTimer = I_FRAME_DURATION;
        eventBus.post(TypedEvent.Type.PLAYER_DAMAGED,
                new DamageInfo(amount, currentHp, maxHp, worldX, worldY));
        if (currentHp == 0) handleDeath();
    }

    public void takeDamage(int amount) {
        takeDamage(amount, 0f, 0f);
    }

    public void heal(int amount, float worldX, float worldY) {
        if (dead) return;
        currentHp = Math.min(maxHp, currentHp + amount);
        eventBus.post(TypedEvent.Type.PLAYER_HEALED,
                new DamageInfo(amount, currentHp, maxHp, worldX, worldY));
    }

    public void heal(int amount) {
        heal(amount, 0f, 0f);
    }

    private void handleDeath() {
        if (respawnsLeft > 0) {
            eventBus.post(TypedEvent.Type.PLAYER_DIED, respawnsLeft);
        } else {
            dead = true;
            eventBus.post(TypedEvent.Type.GAME_OVER, null);
        }
    }

    public void respawn() {
        if (respawnsLeft <= 0) return;
        respawnsLeft--;
        currentHp   = maxHp;
        dead        = false;
        iFrameTimer = 1.5f;
        eventBus.post(TypedEvent.Type.PLAYER_RESPAWNED, respawnsLeft);
    }

    public void addDamageMultiplier(float bonus) {
        baseDamage = Math.round(baseDamage * (1f + bonus));
    }

    public void addSpeedMultiplier(float bonus) {
        speedMultiplier += bonus;
    }

    public void increaseMaxHp(int bonus, int healAmount) {
        maxHp     += bonus;
        currentHp  = Math.min(maxHp, currentHp + healAmount);
        eventBus.post(TypedEvent.Type.PLAYER_HEALED,
                new DamageInfo(healAmount, currentHp, maxHp, 0f, 0f));
    }

    public int     getCurrentHp()       { return currentHp; }
    public int     getMaxHp()           { return maxHp; }
    public int     getBaseDamage()      { return baseDamage; }
    public float   getSpeedMultiplier() { return speedMultiplier; }
    public int     getRespawnsLeft()    { return respawnsLeft; }
    public boolean isDead()             { return dead; }
    public boolean isInvincible()       { return iFrameTimer > 0; }
}
