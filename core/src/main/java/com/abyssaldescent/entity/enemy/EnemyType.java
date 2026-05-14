package com.abyssaldescent.entity.enemy;

public enum EnemyType {
    // speeds in px/s, attackRange in px  (world = screen pixels)
    // ЯРУС 1
    SHADOW_GOBLIN("Shadow Goblin", 15, 8,  180f, 80f,  AiKind.SWARM),
    MOSS_CRAWLER ("Moss Crawler",  25, 12, 100f, 90f,  AiKind.HEAVY),
    STONE_WATCHER("Stone Watcher", 30, 15, 0f,   400f, AiKind.STATIONARY),

// ЯРУС 2
    BONE_ARCHER  ("Bone Archer",   20, 10, 80f,  500f, AiKind.RANGED),
    DROWNER      ("Drowned One",   40, 18, 90f,  90f,  AiKind.HEAVY),
    RIFT_JELLYFISH("Rift Jellyfish",15, 12, 110f, 160f, AiKind.RANGED),

// ЯРУС 3
    RIFT_KNIGHT  ("Rift Knight",   70, 25, 150f, 90f,  AiKind.STEALTH),
    LAVA_SNAKE   ("Lava Snake",    20, 20, 250f, 80f,  AiKind.SWARM),
    SLAG_ELEMENTAL("Slag Elemental",40,20, 100f, 90f,  AiKind.HEAVY),

// БОСС
    MALTARION_ECHO("Maltarion Echo",500,0, 60f,  300f, AiKind.RANGED);

    public enum AiKind { SWARM, RANGED, HEAVY, STEALTH, STATIONARY, GRAVITY }

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
