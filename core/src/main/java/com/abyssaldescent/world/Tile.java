package com.abyssaldescent.world;


public final class Tile {

    private final TileType type;

    Tile(TileType type) {
        this.type = type;
    }

    public TileType getType() { return type; }
    public String getName() { return type.getName(); }
    public boolean isWalkable() { return type.isWalkable(); }
    public boolean isHazard() { return type.isHazard(); }

    @Override
    public String toString() {
        return "Tile{" + type.getName() + "}";
    }
}
