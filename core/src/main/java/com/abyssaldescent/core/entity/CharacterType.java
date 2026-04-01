package com.abyssaldescent.core.entity;


public enum CharacterType {

    KARIN("Karin Veil",  100, 15, true),
    RAYN ("Rayn Alten",   80, 10, false);

    private final String displayName;
    private final int maxHp;
    private final int baseDamage;
    private final boolean melee;

    CharacterType(String displayName, int maxHp, int baseDamage, boolean melee) {
        this.displayName = displayName;
        this.maxHp       = maxHp;
        this.baseDamage  = baseDamage;
        this.melee       = melee;
    }

    public String getDisplayName() { return displayName; }

    public int getMaxHp() { return maxHp; }

    public int getBaseDamage() { return baseDamage; }

    public boolean isMelee() { return melee; }
}
