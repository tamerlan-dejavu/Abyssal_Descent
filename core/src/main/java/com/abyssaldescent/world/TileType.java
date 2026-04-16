package com.abyssaldescent.world;


public enum TileType {
    FLOOR("floor", true, false),
    WALL("wall", false, false),
    CORRIDOR("corridor", true, false),
    SPAWN("spawn", true, false),
    EXIT("exit", true, false),
    WATER("water", true, true),
    LAVA("lava", true, true),
    VOID("void", false, false);

    private final String name;
    private final boolean walkable;
    private final boolean hazard;

    TileType(String name, boolean walkable, boolean hazard) {
        this.name = name;
        this.walkable = walkable;
        this.hazard = hazard;
    }

    public String getName() { return name; }
    public boolean isWalkable() { return walkable; }
    public boolean isHazard() { return hazard; }
}
