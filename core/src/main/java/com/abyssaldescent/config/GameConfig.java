package com.abyssaldescent.config;


public class GameConfig {

    private static GameConfig instance;

    public static GameConfig getInstance() {
        if (instance == null) instance = new GameConfig();
        return instance;
    }

    private GameConfig() {}

    private Difficulty difficulty = Difficulty.NORMAL;

    public void       setDifficulty(Difficulty d) { this.difficulty = d; }
    public Difficulty getDifficulty()              { return difficulty; }


    /** HP Карин: Easy=150, Normal=100, Hard=75 */
    public int getPlayerMaxHp() {
        switch (difficulty) {
            case EASY:  return 150;
            case HARD:  return 75;
            default:    return 100;
        }
    }

    /** Базовая скорость м/с: Easy +20% */
    public float getPlayerBaseSpeed() {
        switch (difficulty) {
            case EASY:  return 4.8f;
            default:    return 4.0f;
        }
    }


    /** Множитель урона врагов: Easy×0.7, Normal×1.0, Hard×1.4 */
    public float getEnemyDamageMultiplier() {
        switch (difficulty) {
            case EASY:  return 0.7f;
            case HARD:  return 1.4f;
            default:    return 1.0f;
        }
    }

    public float getEnemyHpMultiplier() {
        switch (difficulty) {
            case EASY:  return 0.8f;
            case HARD:  return 1.3f;
            default:    return 1.0f;
        }
    }

    public float getEnemyRespawnTime() {
        switch (difficulty) {
            case EASY:  return 45f;
            case HARD:  return 20f;
            default:    return 30f;
        }
    }

    public float getChipDropBonus() {
        switch (difficulty) {
            case EASY:  return  0.10f;
            case HARD:  return -0.10f;
            default:    return  0.00f;
        }
    }

    public float getTierDamageMultiplier(int floor) {
        switch (floor) {
            case 2:  return 1.2f;
            case 3:  return 1.45f;
            default: return 1.0f;
        }
    }

    public float getTierHpMultiplier(int floor) {
        switch (floor) {
            case 2:  return 1.25f;
            case 3:  return 1.60f;
            default: return 1.0f;
        }
    }

    public int calcEnemyDamage(int baseEnemyDamage, int floor) {
        float result = baseEnemyDamage
                * getEnemyDamageMultiplier()
                * getTierDamageMultiplier(floor);
        return Math.round(result);
    }

    
    public int calcEnemyHp(int baseEnemyHp, int floor) {
        float result = baseEnemyHp
                * getEnemyHpMultiplier()
                * getTierHpMultiplier(floor);
        return Math.round(result);
    }

    
    public float calcChipDropChance(float baseChance) {
        return Math.max(0f, Math.min(1f, baseChance + getChipDropBonus()));
    }
}