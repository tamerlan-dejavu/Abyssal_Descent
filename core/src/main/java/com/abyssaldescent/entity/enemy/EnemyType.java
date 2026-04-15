package com.abyssaldescent.entity.enemy;

public enum EnemyType {
    SHADOW_GOBLIN("Shadow Goblin", 15, 3, 1.5f, 0.5f, AiKind.SWARM),
    MOSS_CRAWLER("Moss Crawler", 25, 4, 0.8f, 1.2f, AiKind.HEAVY),
    RATLING("Ratling", 10, 2, 1.0f, 0.4f, AiKind.SWARM),
    STONE_WATCHER("Stone Watcher", 30, 5, 0f, 5f, AiKind.RANGED),
    BONE_ARCHER("Bone Archer", 20, 4, 0.6f, 6f, AiKind.RANGED),
    CAVE_COLOSSUS("Cave Colossus", 120, 12, 1.0f, 1.8f, AiKind.HEAVY),
    LIVING_SHADOW("Living Shadow", 18, 4, 1.4f, 1.0f, AiKind.STEALTH),
    GRAVITY_PARASITE("Gravity Parasite", 30, 3, 0.9f, 3f, AiKind.GRAVITY);

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
