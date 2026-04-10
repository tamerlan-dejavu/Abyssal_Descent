package com.abyssaldescent.world.tile;

import java.util.EnumMap;
import java.util.Map;

/**
 * Flyweight factory for tile instances.
 *
 * <p>Instead of creating a new object for every cell in the dungeon grid,
 * this factory returns shared {@link Tile} instances keyed by {@link TileType}.
 * A 50x50 floor has 2 500 cells but only as many Tile objects as there are
 * distinct TileTypes (currently 8).
 *
 * <p><b>Pattern:</b> Flyweight (GoF structural).
 */
public final class TileFactory {

    private static final TileFactory INSTANCE = new TileFactory();

    private final Map<TileType, Tile> pool = new EnumMap<>(TileType.class);

    private TileFactory() {
        for (TileType type : TileType.values()) {
            pool.put(type, new Tile(type));
        }
    }

    public static TileFactory getInstance() {
        return INSTANCE;
    }

    /** Returns the shared Tile for the given type. */
    public Tile get(TileType type) {
        return pool.get(type);
    }

    /** Number of distinct tile objects currently in the pool. */
    public int poolSize() {
        return pool.size();
    }
}
