package com.abyssaldescent.config;

public final class DifficultySettings {

    public enum Level { EASY, NORMAL, HARD }

    public final Level  level;
    /** Multiplier applied to player max HP. */
    public final float  hpMultiplier;
    /** Multiplier applied to all enemy damage values. */
    public final float  enemyDamageMultiplier;
    /** Multiplier applied to chip-drop chance. */
    public final float  chipChanceMultiplier;

    private DifficultySettings(Level level, float hp, float enemyDmg, float chipChance) {
        this.level                = level;
        this.hpMultiplier         = hp;
        this.enemyDamageMultiplier = enemyDmg;
        this.chipChanceMultiplier  = chipChance;
    }

    public static final DifficultySettings EASY =
            new DifficultySettings(Level.EASY,   1.50f, 0.70f, 1.20f);
    public static final DifficultySettings NORMAL =
            new DifficultySettings(Level.NORMAL, 1.00f, 1.00f, 1.00f);
    public static final DifficultySettings HARD =
            new DifficultySettings(Level.HARD,   0.75f, 1.40f, 0.90f);

    public String getDisplayName() {
        switch (level) {
            case EASY:   return "Easy";
            case NORMAL: return "Normal";
            case HARD:   return "Hard";
            default:     return "Unknown";
        }
    }

    public String getDescription() {
        switch (level) {
            case EASY:   return "+50% HP  /  -30% enemy dmg  /  +20% chip chance";
            case NORMAL: return "Base parameters — balanced experience";
            case HARD:   return "-25% HP  /  +40% enemy dmg  /  -10% chip chance";
            default:     return "";
        }
    }
}
