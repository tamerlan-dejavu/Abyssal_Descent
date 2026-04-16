package com.abyssaldescent.world;

import java.util.EnumMap;
import java.util.Map;

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

    public Tile get(TileType type) {
        return pool.get(type);
    }

    public int poolSize() {
        return pool.size();
    }
}
