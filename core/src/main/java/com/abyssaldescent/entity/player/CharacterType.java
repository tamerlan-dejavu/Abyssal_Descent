package com.abyssaldescent.entity.player;

public enum CharacterType {
    KARIN(100, 15);

    private final int maxHp;
    private final int baseDamage;

    CharacterType(int maxHp, int baseDamage) {
        this.maxHp = maxHp;
        this.baseDamage = baseDamage;
    }

    public int getMaxHp() { return maxHp; }
    public int getBaseDamage() { return baseDamage; }
}
