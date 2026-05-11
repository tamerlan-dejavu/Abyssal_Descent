package com.abyssaldescent.config;

public enum EnemyStats {

    //                          baseHp  baseDamage  floor  chipChance
    GOBLIN_SHADOW     (15,   8,   1,  0.30f),
    MOSS_CRAWLER      (25,  12,   1,  0.25f),
    STONE_WATCHER     (30,  15,   1,  0.20f),
    BONE_SHOOTER      (20,  10,   2,  0.30f),
    DROWNED           (40,  18,   2,  0.20f),
    RIFT_MEDUSA       (15,  12,   2,  0.25f),
    RIFT_KNIGHT       (70,  25,   3,  0.30f),
    LAVA_SERPENT      (20,  20,   3,  0.25f),
    SLAG_ELEMENTAL    (40,  20,   3,  0.20f),
    MALTARION_ECHO    (500,  0,   3,  0.00f);   // босс — урон задаётся фазами

    private final int   baseHp;
    private final int   baseDamage;
    private final int   floor;          // ярус появления
    private final float baseChipChance; // базовый шанс дропа чипа

    EnemyStats(int baseHp, int baseDamage, int floor, float baseChipChance) {
        this.baseHp         = baseHp;
        this.baseDamage     = baseDamage;
        this.floor          = floor;
        this.baseChipChance = baseChipChance;
    }

    
    public int getScaledHp() {
        return GameConfig.getInstance().calcEnemyHp(baseHp, floor);
    }

  
    public int getScaledDamage() {
        return GameConfig.getInstance().calcEnemyDamage(baseDamage, floor);
    }

    public float getScaledChipChance() {
        return GameConfig.getInstance().calcChipDropChance(baseChipChance);
    }


    public int   getBaseHp()         { return baseHp; }
    public int   getBaseDamage()     { return baseDamage; }
    public int   getFloor()          { return floor; }
    public float getBaseChipChance() { return baseChipChance; }
}
