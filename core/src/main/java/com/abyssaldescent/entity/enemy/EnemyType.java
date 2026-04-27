package com.abyssaldescent.entity.enemy;

public enum EnemyType {
    // ЯРУС 1
    SHADOW_GOBLIN("Shadow Goblin", 15, 8,  1.5f, 0.5f, AiKind.SWARM),
    MOSS_CRAWLER ("Moss Crawler",  25, 12, 0.8f, 1.2f, AiKind.HEAVY),
    STONE_WATCHER("Stone Watcher", 30, 15, 0f,   5f,   AiKind.RANGED),

// ЯРУС 2
    BONE_ARCHER  ("Bone Archer",   20, 10, 0.6f, 6f,   AiKind.RANGED),
    DROWNER      ("Drowned One",   40, 18, 0.7f, 1.0f, AiKind.HEAVY),
    RIFT_JELLYFISH("Rift Jellyfish",15, 12, 0.9f, 2f,  AiKind.RANGED),

// ЯРУС 3
    RIFT_KNIGHT  ("Rift Knight",   70, 25, 1.2f, 1.0f, AiKind.STEALTH),
    LAVA_SNAKE   ("Lava Snake",    20, 20, 2.0f, 0.5f, AiKind.SWARM),
    SLAG_ELEMENTAL("Slag Elemental",40,20, 0.8f, 1.2f, AiKind.HEAVY),

// БОСС
    MALTARION_ECHO("Maltarion Echo",500,0, 0.5f, 3f,  AiKind.RANGED);

    public enum AiKind { SWARM, RANGED, HEAVY, STEALTH, GRAVITY }

    private final String displayName;
    private final int maxHp;
    private final int damage;
    private final float speed;
    private final float attackRange;
    private final AiKind aiKind;

    EnemyType(String displayName, int maxHp, int damage, float speed, float attackRange, AiKind aiKind) {
        this.displayName = displayName;
        this.maxHp = maxHp;
        this.damage = damage;
        this.speed = speed;
        this.attackRange = attackRange;
        this.aiKind = aiKind;
    }

    public String getDisplayName() { return displayName; }
    public int getMaxHp() { return maxHp; }
    public int getDamage() { return damage; }
    public float getSpeed() { return speed; }
    public float getAttackRange() { return attackRange; }
    public AiKind getAiKind() { return aiKind; }
}
