package com.abyssaldescent.entity.player;

public final class PlayerSlot {
    private final CharacterType characterType;
    private int currentHp;
    private PlayerStatus status;
    private boolean active;

    public PlayerSlot(CharacterType characterType) {
        this.characterType = characterType;
        this.currentHp = characterType.getMaxHp();
        this.status = PlayerStatus.ALIVE;
        this.active = false;
    }

    public void reset() {
        currentHp = characterType.getMaxHp();
        status = PlayerStatus.ALIVE;
    }

    public void setCurrentHp(int hp) {
        currentHp = Math.max(0, Math.min(hp, characterType.getMaxHp()));
    }

    public void applyDamage(int damage) {
        setCurrentHp(currentHp - damage);
    }

    public void heal(int amount) {
        setCurrentHp(currentHp + amount);
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CharacterType getCharacterType() { return characterType; }

    public int getCurrentHp() { return currentHp; }

    public PlayerStatus getStatus() { return status; }

    public boolean isActive() { return active; }

    public boolean isAlive() {
        return status == PlayerStatus.ALIVE || status == PlayerStatus.INVINCIBLE;
    }

    public boolean isDead() { return currentHp <= 0; }
}
