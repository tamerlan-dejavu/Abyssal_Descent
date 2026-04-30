package com.abyssaldescent.world;

public enum RoomType {
    START("Start", false, false),
    COMBAT("Combat", true, false),
    TREASURE("Treasure", false, false),
    REST("Rest", false, false),
    PUZZLE("Puzzle", false, false),
    SHOP("Shop", false, false),
    CORRIDOR("Corridor", false, false),
    BOSS("Boss", true, true),
    EXIT("Exit", false, false);

    private final String displayName;
    private final boolean spawnsEnemies;
    private final boolean isBoss;

    RoomType(String displayName, boolean spawnsEnemies, boolean isBoss) {
        this.displayName = displayName;
        this.spawnsEnemies = spawnsEnemies;
        this.isBoss = isBoss;
    }

    public String getDisplayName() { return displayName; }
    public boolean spawnsEnemies() { return spawnsEnemies; }
    public boolean isBoss() { return isBoss; }
}
