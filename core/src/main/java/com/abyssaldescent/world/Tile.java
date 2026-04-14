package com.abyssaldescent.world;

/**
 * Flyweight object representing a single tile definition.
 * Instances are shared via {@link TileFactory} — the intrinsic state
 * (type, walkability, hazard flag) is immutable and identical for all
 * cells of the same kind.
 */
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
